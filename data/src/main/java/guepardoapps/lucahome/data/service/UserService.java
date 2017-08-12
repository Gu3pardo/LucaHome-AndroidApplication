package guepardoapps.lucahome.data.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.data.controller.DownloadController;
import guepardoapps.lucahome.data.controller.SettingsController;
import guepardoapps.lucahome.data.service.broadcasts.content.ObjectChangeFinishedContent;

public class UserService {
    public static final String UserCheckedFinishedBroadcast = "guepardoapps.lucahome.data.service.user.checked.finished";
    public static final String UserCheckedFinishedBundle = "UserCheckedFinishedBundle";

    public static final int MIN_USER_LENGTH = 4;
    public static final int MIN_PASSWORD_LENGTH = 6;

    private static final UserService SINGLETON = new UserService();
    private boolean _isInitialized;

    private static final String TAG = UserService.class.getSimpleName();
    private Logger _logger;

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private LucaUser _tempUser = null;

    private BroadcastReceiver _userCheckedFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_userCheckedFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.User) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                _tempUser = null;
                sendFailedCheckBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                _tempUser = null;
                sendFailedCheckBroadcast(contentResponse);
                return;
            }

            _settingsController.SetUser(_tempUser);
            _tempUser = null;

            _broadcastController.SendSerializableBroadcast(
                    UserCheckedFinishedBroadcast,
                    UserCheckedFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));
        }
    };

    private UserService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static UserService getInstance() {
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

        _receiverController.RegisterReceiver(_userCheckedFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _isInitialized = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public boolean IsEnteredUserValid(@NonNull String user) {
        return (user.length() >= MIN_USER_LENGTH) && (!user.contains(" "));
    }

    public boolean IsEnteredPasswordValid(@NonNull String password) {
        return password.length() >= MIN_PASSWORD_LENGTH;
    }

    public boolean ValidatingUser() {
        return _tempUser != null;
    }

    public LucaUser GetUser() {
        return _settingsController.GetUser();
    }

    public void SetUser(@NonNull LucaUser user) {
        _settingsController.SetUser(user);
    }

    public boolean IsAnUserSaved() {
        LucaUser user = _settingsController.GetUser();
        return !(user.GetName() == null || user.GetPassphrase() == null
                || user.GetName().contentEquals("NA") || user.GetPassphrase().contentEquals("NA")
                || user.GetName().contentEquals("Dummy") || user.GetPassphrase().contentEquals("Dummy")
                || user.GetName().contentEquals("DUMMY") || user.GetPassphrase().contentEquals("DUMMY"));
    }

    public void ValidateUser() {
        validateUser(_settingsController.GetUser());
    }

    public void ValidateUser(@NonNull LucaUser user) {
        validateUser(user);
    }

    public void ValidateUserName(@NonNull String userName) {
        if (!IsAnUserSaved()) {
            sendFailedCheckBroadcast("Missing passphrase");
            return;
        }

        if (userName.contentEquals(_settingsController.GetUser().GetName())) {
            return;
        }

        validateUser(new LucaUser(userName, _settingsController.GetUser().GetPassphrase()));
    }

    public void ValidateUserPassphrase(@NonNull String userPassphrase) {
        if (!IsAnUserSaved()) {
            sendFailedCheckBroadcast("Missing user name");
            return;
        }

        if (userPassphrase.contentEquals(_settingsController.GetUser().GetPassphrase())) {
            return;
        }

        validateUser(new LucaUser(_settingsController.GetUser().GetName(), userPassphrase));
    }

    private void validateUser(@NonNull LucaUser user) {
        _logger.Debug("validateUser");

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.VALIDATE_USER.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _tempUser = user;
        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.User, true);
    }

    private void sendFailedCheckBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                UserCheckedFinishedBroadcast,
                UserCheckedFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }
}
