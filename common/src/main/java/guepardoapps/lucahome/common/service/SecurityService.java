package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.Security;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToSecurityConverter;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.services.IDataNotificationService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class SecurityService implements IDataNotificationService {
    public static class SecurityDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<Security> SecurityList;

        SecurityDownloadFinishedContent(SerializableList<Security> securityList, boolean succcess) {
            super(succcess, new byte[]{});
            SecurityList = securityList;
        }
    }

    public static final int NOTIFICATION_ID = 94802738;

    public static final String SecurityDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.security.download.finished";
    public static final String SecurityDownloadFinishedBundle = "SecurityDownloadFinishedBundle";

    public static final String SecurityCameraStateFinishedBroadcast = "guepardoapps.lucahome.data.service.security.camera.state.finished";
    public static final String SecurityCameraStateFinishedBundle = "SecurityCameraStateFinishedBundle";

    public static final String SecurityMotionStateFinishedBroadcast = "guepardoapps.lucahome.data.service.security.motion.state.finished";
    public static final String SecurityMotionStateFinishedBundle = "SecurityMotionStateFinishedBundle";

    private static final SecurityService SINGLETON = new SecurityService();
    private boolean _isInitialized;

    private static final String TAG = SecurityService.class.getSimpleName();

    private boolean _displayNotification;
    private Class<?> _receiverActivity;

    private static final int MIN_TIMEOUT_MIN = 15;
    private static final int MAX_TIMEOUT_MIN = 24 * 60;

    private SerializableDate _lastUpdate;

    private boolean _reloadEnabled;
    private int _reloadTimeout;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            LoadData();
            if (_reloadEnabled && _networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private NotificationController _notificationController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private SerializableList<Security> _securityList = new SerializableList<>();

    private BroadcastReceiver _securityDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Security) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedDownloadBroadcast();
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedDownloadBroadcast();
                return;
            }

            SerializableList<Security> securityList = JsonDataToSecurityConverter.getInstance().GetList(contentResponse);
            if (securityList == null) {
                Logger.getInstance().Error(TAG, "Converted birthdayList is null!");
                sendFailedDownloadBroadcast();
                return;
            }

            _lastUpdate = new SerializableDate();

            _securityList = securityList;

            if (_securityList.getSize() > 0) {
                Security security = _securityList.getValue(0);
                if (security.IsCameraActive()) {
                    ShowNotification();
                } else {
                    CloseNotification();
                }
            }

            _broadcastController.SendSerializableBroadcast(
                    SecurityDownloadFinishedBroadcast,
                    SecurityDownloadFinishedBundle,
                    new SecurityDownloadFinishedContent(_securityList, true));
        }
    };

    private BroadcastReceiver _cameraStateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.SecurityCamera) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedCameraStateBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedCameraStateBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new SerializableDate();

            _broadcastController.SendSerializableBroadcast(
                    SecurityCameraStateFinishedBroadcast,
                    SecurityCameraStateFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _motionStateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.SecurityCameraControl) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedMotionStateBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedMotionStateBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new SerializableDate();

            _broadcastController.SendSerializableBroadcast(
                    SecurityMotionStateFinishedBroadcast,
                    SecurityMotionStateFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _homeNetworkAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            if (_reloadEnabled && _networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private BroadcastReceiver _homeNetworkNotAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
        }
    };

    private SecurityService() {
    }

    public static SecurityService getInstance() {
        return SINGLETON;
    }

    @Override
    public void Initialize(@NonNull Context context, @NonNull Class<?> receiverActivity, boolean displayNotification, boolean reloadEnabled, int reloadTimeout) {
        if (_isInitialized) {
            Logger.getInstance().Warning(TAG, "Already initialized!");
            return;
        }

        _lastUpdate = new SerializableDate();

        _receiverActivity = receiverActivity;
        _displayNotification = displayNotification;
        _reloadEnabled = reloadEnabled;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _notificationController = new NotificationController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _receiverController.RegisterReceiver(_securityDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_cameraStateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_motionStateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _isInitialized = false;
    }

    @Override
    public SerializableList<Security> GetDataList() {
        return _securityList;
    }

    @Override
    public SerializableList<?> SearchDataList(@NonNull String searchKey) {
        Logger.getInstance().Error(TAG, "Not available!");
        return null;
    }

    @Override
    public int GetHighestId() {
        return -1;
    }

    @Override
    public void LoadData() {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast();
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_MOTION_DATA.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Security, true);
    }

    public void SetCameraState(boolean state) {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast();
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + (state ? LucaServerAction.START_MOTION.toString() : LucaServerAction.STOP_MOTION.toString());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.SecurityCamera, true);
    }

    public void SetMotionState(boolean state) {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast();
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.SET_MOTION_CONTROL_TASK.toString() + (state ? "1" : "0");

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.SecurityCameraControl, true);
    }

    @Override
    public void ShowNotification() {
        if (!_displayNotification) {
            Logger.getInstance().Warning(TAG, "_displayNotification is false!");
            return;
        }
        _notificationController.CreateCameraNotification(NOTIFICATION_ID, _receiverActivity);
    }

    @Override
    public void CloseNotification() {
        _notificationController.CloseNotification(NOTIFICATION_ID);
    }

    @Override
    public boolean GetDisplayNotification() {
        return _displayNotification;
    }

    @Override
    public void SetDisplayNotification(boolean displayNotification) {
        _displayNotification = displayNotification;

        if (!_displayNotification) {
            CloseNotification();
        } else {

            if (_securityList != null) {
                if (_securityList.getSize() > 0) {
                    Security security = _securityList.getValue(0);
                    if (security.IsCameraActive()) {
                        ShowNotification();
                    }
                }
            }
        }
    }

    @Override
    public void SetReceiverActivity(@NonNull Class<?> receiverActivity) {
        _receiverActivity = receiverActivity;
    }

    @Override
    public Class<?> GetReceiverActivity() {
        return _receiverActivity;
    }

    @Override
    public boolean GetReloadEnabled() {
        return _reloadEnabled;
    }

    @Override
    public void SetReloadEnabled(boolean reloadEnabled) {
        _reloadEnabled = reloadEnabled;
        if (_reloadEnabled) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
        }
    }

    @Override
    public int GetReloadTimeout() {
        return _reloadTimeout;
    }

    @Override
    public void SetReloadTimeout(int reloadTimeout) {
        if (reloadTimeout < MIN_TIMEOUT_MIN) {
            reloadTimeout = MIN_TIMEOUT_MIN;
        }
        if (reloadTimeout > MAX_TIMEOUT_MIN) {
            reloadTimeout = MAX_TIMEOUT_MIN;
        }

        _reloadTimeout = reloadTimeout * 60 * 1000;
        if (_reloadEnabled) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
        }
    }

    public SerializableDate GetLastUpdate() {
        return _lastUpdate;
    }

    private void sendFailedDownloadBroadcast() {
        _broadcastController.SendSerializableBroadcast(
                SecurityDownloadFinishedBroadcast,
                SecurityDownloadFinishedBundle,
                new SecurityDownloadFinishedContent(null, false));
    }

    private void sendFailedCameraStateBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                SecurityCameraStateFinishedBroadcast,
                SecurityCameraStateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedMotionStateBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                SecurityMotionStateFinishedBroadcast,
                SecurityMotionStateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }
}
