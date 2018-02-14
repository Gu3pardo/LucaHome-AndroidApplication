package guepardoapps.lucahome.common.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.LucaSecurity;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToLucaSecurityConverter;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"WeakerAccess"})
public class LucaSecurityService implements ILucaSecurityService {
    private static final String Tag = LucaSecurityService.class.getSimpleName();

    private static final LucaSecurityService Singleton = new LucaSecurityService();

    private static final int MinTimeoutMin = 15;
    private static final int MaxTimeoutMin = 24 * 60;

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private NotificationController _notificationController;
    private ReceiverController _receiverController;

    private ArrayList<LucaSecurity> _lucaSecurityList = new ArrayList<>();

    private Calendar _lastUpdate;

    private Class<?> _receiverActivity;
    private boolean _displayNotification;

    private boolean _isInitialized;

    private boolean _reloadEnabled;
    private int _reloadTimeout;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            LoadData();
            if (_reloadEnabled && _networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private class AsyncConverterTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            for (String contentResponse : strings) {
                ArrayList<LucaSecurity> lucaSecurityList = JsonDataToLucaSecurityConverter.getInstance().GetList(contentResponse);
                if (lucaSecurityList == null) {
                    Logger.getInstance().Error(Tag, "Converted lucaSecurityList is null!");
                    _broadcastController.SendBooleanBroadcast(LucaSecurityDownloadFinishedBroadcast, LucaSecurityDownloadFinishedBundle, false);
                    return "";
                }

                _lucaSecurityList = lucaSecurityList;
                if (_lucaSecurityList.size() > 0) {
                    LucaSecurity lucaSecurity = _lucaSecurityList.get(0);
                    if (lucaSecurity.IsCameraActive()) {
                        ShowNotification();
                    } else {
                        CloseNotification();
                    }
                }

                _lastUpdate = Calendar.getInstance();
                _broadcastController.SendBooleanBroadcast(LucaSecurityDownloadFinishedBroadcast, LucaSecurityDownloadFinishedBundle, true);
            }
            return "Success";
        }
    }

    private BroadcastReceiver _lucaSecurityFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            if (content.CurrentDownloadType != DownloadController.DownloadType.LucaSecurity) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));
            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(LucaSecurityDownloadFinishedBroadcast, LucaSecurityDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(LucaSecurityDownloadFinishedBroadcast, LucaSecurityDownloadFinishedBundle, false);
                return;
            }

            new AsyncConverterTask().execute(contentResponse);
        }
    };

    private BroadcastReceiver _lucaSecurityCameraFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            if (content.CurrentDownloadType != DownloadController.DownloadType.LucaSecurityCamera) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));
            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(LucaSecurityCameraStateFinishedBroadcast, LucaSecurityCameraStateFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(LucaSecurityCameraStateFinishedBroadcast, LucaSecurityCameraStateFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(LucaSecurityCameraStateFinishedBroadcast, LucaSecurityCameraStateFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _lucaSecurityCameraControlFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            if (content.CurrentDownloadType != DownloadController.DownloadType.LucaSecurityCameraControl) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));
            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(LucaSecurityMotionStateFinishedBroadcast, LucaSecurityMotionStateFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(LucaSecurityMotionStateFinishedBroadcast, LucaSecurityMotionStateFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(LucaSecurityMotionStateFinishedBroadcast, LucaSecurityMotionStateFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _homeNetworkAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            if (_reloadEnabled && _networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
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

    private LucaSecurityService() {
    }

    public static LucaSecurityService getInstance() {
        return Singleton;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout, boolean displayNotification, Class<?> receiverActivity) {
        if (_isInitialized) {
            Logger.getInstance().Warning(Tag, "Already initialized!");
            return;
        }

        _lastUpdate = Calendar.getInstance();

        _receiverActivity = receiverActivity;
        _displayNotification = displayNotification;
        _reloadEnabled = reloadEnabled;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _notificationController = new NotificationController(context);
        _receiverController = new ReceiverController(context);

        _receiverController.RegisterReceiver(_lucaSecurityFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_lucaSecurityCameraFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_lucaSecurityCameraControlFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

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
    public ArrayList<LucaSecurity> GetDataList() {
        return _lucaSecurityList;
    }

    @Override
    public LucaSecurity GetByUuid(@NonNull UUID uuid) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetByUuid not implemented for" + Tag);
    }

    @Override
    public ArrayList<LucaSecurity> SearchDataList(@NonNull String searchKey) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SearchDataList not implemented for" + Tag);
    }

    @Override
    public void LoadData() {
        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(LucaSecurityDownloadFinishedBroadcast, LucaSecurityDownloadFinishedBundle, false);
            return;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_MOTION_DATA.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.LucaSecurity, true);
    }

    @Override
    public void AddEntry(@NonNull LucaSecurity newEntry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method AddEntry not implemented for" + Tag);
    }

    @Override
    public void UpdateEntry(@NonNull LucaSecurity updateEntry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method UpdateEntry not implemented for" + Tag);
    }

    @Override
    public void DeleteEntry(@NonNull LucaSecurity deleteEntry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method DeleteEntry not implemented for" + Tag);
    }

    @Override
    public void SetCameraState(boolean state) {
        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(LucaSecurityCameraStateFinishedBroadcast, LucaSecurityCameraStateFinishedBundle, false);
            return;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + (state ? LucaServerActionTypes.START_MOTION.toString() : LucaServerActionTypes.STOP_MOTION.toString());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.LucaSecurityCamera, true);
    }

    @Override
    public void SetMotionState(boolean state) {
        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(LucaSecurityMotionStateFinishedBroadcast, LucaSecurityMotionStateFinishedBundle, false);
            return;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.SET_MOTION_CONTROL_TASK.toString() + (state ? "1" : "0");

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.LucaSecurityCameraControl, true);
    }

    @Override
    public void ShowNotification() {
        if (!_displayNotification) {
            Logger.getInstance().Warning(Tag, "_displayNotification is false!");
            return;
        }
        _notificationController.CreateCameraNotification(NotificationId, _receiverActivity);
    }

    @Override
    public void CloseNotification() {
        _notificationController.CloseNotification(NotificationId);
    }

    @Override
    public void SetDisplayNotification(boolean displayNotification) {
        _displayNotification = displayNotification;

        if (!_displayNotification) {
            CloseNotification();
        } else {
            if (_lucaSecurityList != null) {
                if (_lucaSecurityList.size() > 0) {
                    LucaSecurity lucaSecurity = _lucaSecurityList.get(0);
                    if (lucaSecurity.IsCameraActive()) {
                        ShowNotification();
                    }
                }
            }
        }
    }

    @Override
    public boolean GetDisplayNotification() {
        return _displayNotification;
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
    public void SetReloadEnabled(boolean reloadEnabled) {
        _reloadEnabled = reloadEnabled;
        if (_reloadEnabled) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
        }
    }

    @Override
    public boolean GetReloadEnabled() {
        return _reloadEnabled;
    }

    @Override
    public void SetReloadTimeout(int reloadTimeout) {
        if (reloadTimeout < MinTimeoutMin) {
            reloadTimeout = MinTimeoutMin;
        }
        if (reloadTimeout > MaxTimeoutMin) {
            reloadTimeout = MaxTimeoutMin;
        }

        _reloadTimeout = reloadTimeout * 60 * 1000;
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
    public Calendar GetLastUpdate() {
        return _lastUpdate;
    }
}
