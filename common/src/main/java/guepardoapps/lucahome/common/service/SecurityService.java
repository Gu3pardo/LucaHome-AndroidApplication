package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.Security;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToSecurityConverter;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class SecurityService {
    public static class SecurityDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<Security> SecurityList;

        public SecurityDownloadFinishedContent(SerializableList<Security> securityList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            SecurityList = securityList;
        }
    }

    public static final String SecurityDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.security.download.finished";
    public static final String SecurityDownloadFinishedBundle = "SecurityDownloadFinishedBundle";

    public static final String SecurityCameraStateFinishedBroadcast = "guepardoapps.lucahome.data.service.security.camera.state.finished";
    public static final String SecurityCameraStateFinishedBundle = "SecurityCameraStateFinishedBundle";

    public static final String SecurityMotionStateFinishedBroadcast = "guepardoapps.lucahome.data.service.security.motion.state.finished";
    public static final String SecurityMotionStateFinishedBundle = "SecurityMotionStateFinishedBundle";

    private static final SecurityService SINGLETON = new SecurityService();
    private boolean _isInitialized;

    private static final String TAG = SecurityService.class.getSimpleName();
    private Logger _logger;

    private static final int TIMEOUT_MS = 15 * 60 * 1000;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_reloadListRunnable run");
            LoadSecurityList();
            _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);
        }
    };

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private JsonDataToSecurityConverter _jsonDataToSecurityConverter;

    private SerializableList<Security> _securityList = new SerializableList<>();

    private BroadcastReceiver _securityDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_securityDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Security) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<Security> securityList = _jsonDataToSecurityConverter.GetList(contentResponse);
            if (securityList == null) {
                _logger.Error("Converted birthdayList is null!");
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _securityList = securityList;

            _broadcastController.SendSerializableBroadcast(
                    SecurityDownloadFinishedBroadcast,
                    SecurityDownloadFinishedBundle,
                    new SecurityDownloadFinishedContent(_securityList, true, content.Response));
        }
    };

    private BroadcastReceiver _cameraStateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_cameraStateFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.SecurityCamera) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedCameraStateBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedCameraStateBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    SecurityCameraStateFinishedBroadcast,
                    SecurityCameraStateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadSecurityList();
        }
    };

    private BroadcastReceiver _motionStateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_motionStateFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.SecurityMotionControl) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedMotionStateBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedMotionStateBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    SecurityMotionStateFinishedBroadcast,
                    SecurityMotionStateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadSecurityList();
        }
    };

    private SecurityService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static SecurityService getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _receiverController.RegisterReceiver(_securityDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_cameraStateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_motionStateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _jsonDataToSecurityConverter = new JsonDataToSecurityConverter();

        _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);

        _isInitialized = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public SerializableList<Security> GetSecurityList() {
        return _securityList;
    }

    public void LoadSecurityList() {
        _logger.Debug("LoadSecurityList");

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast("No user");
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_MOTION_DATA.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Security, true);
    }

    public void SetCameraState(boolean state) {
        _logger.Debug(String.format(Locale.getDefault(), "SetCameraState: %s", state));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast("No user");
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + (state ? LucaServerAction.START_MOTION.toString() : LucaServerAction.STOP_MOTION.toString());
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.SecurityCamera, true);
    }

    public void SetMotionState(boolean state) {
        _logger.Debug(String.format(Locale.getDefault(), "SetMotionState: %s", state));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast("No user");
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.SET_MOTION_CONTROL_TASK.toString() + (state ? "1" : "0");
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.SecurityMotionControl, true);
    }

    private void sendFailedDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                SecurityDownloadFinishedBroadcast,
                SecurityDownloadFinishedBundle,
                new SecurityDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
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
