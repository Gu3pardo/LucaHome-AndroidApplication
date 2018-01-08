package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

@SuppressWarnings("unused")
public class UserService {
    public static final String UserCheckedFinishedBroadcast = "guepardoapps.lucahome.data.service.user.checked.finished";
    public static final String UserCheckedFinishedBundle = "UserCheckedFinishedBundle";

    private static final int MIN_USER_LENGTH = 4;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private static final UserService SINGLETON = new UserService();
    private boolean _isInitialized;

    private static final String TAG = UserService.class.getSimpleName();

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private ReceiverController _receiverController;

    private LucaUser _tempUser = null;

    private BroadcastReceiver _userCheckedFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.User) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                _tempUser = null;
                sendFailedCheckBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                _tempUser = null;
                sendFailedCheckBroadcast(contentResponse);
                return;
            }

            SettingsController.getInstance().SetUser(_tempUser);
            _tempUser = null;

            _broadcastController.SendSerializableBroadcast(
                    UserCheckedFinishedBroadcast,
                    UserCheckedFinishedBundle,
                    new ObjectChangeFinishedContent(true, Tools.CompressStringToByteArray("Download finished")));
        }
    };

    private UserService() {
    }

    public static UserService getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        if (_isInitialized) {
            Logger.getInstance().Warning(TAG, "Already initialized!");
            return;
        }

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _receiverController = new ReceiverController(context);

        _receiverController.RegisterReceiver(_userCheckedFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _isInitialized = true;
    }

    public void Dispose() {
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
        return SettingsController.getInstance().GetUser();
    }

    public void SetUser(@NonNull LucaUser user) {
        SettingsController.getInstance().SetUser(user);
    }

    public boolean IsAnUserSaved() {
        LucaUser user = SettingsController.getInstance().GetUser();
        return !(user.GetName() == null || user.GetPassphrase() == null
                || user.GetName().length() == 0 || user.GetPassphrase().length() == 0
                || user.GetName().contentEquals("NA") || user.GetPassphrase().contentEquals("NA")
                || user.GetName().contentEquals("Dummy") || user.GetPassphrase().contentEquals("Dummy")
                || user.GetName().contentEquals("DUMMY") || user.GetPassphrase().contentEquals("DUMMY"));
    }

    public void ValidateUser() {
        validateUser(SettingsController.getInstance().GetUser());
    }

    public void ValidateUser(@NonNull LucaUser user) {
        validateUser(user);
    }

    public void ValidateUserName(@NonNull String userName) {
        if (!IsAnUserSaved()) {
            sendFailedCheckBroadcast("Missing passphrase");
            return;
        }

        if (userName.contentEquals(SettingsController.getInstance().GetUser().GetName())) {
            return;
        }

        validateUser(new LucaUser(userName, SettingsController.getInstance().GetUser().GetPassphrase()));
    }

    public void ValidateUserPassphrase(@NonNull String userPassphrase) {
        if (!IsAnUserSaved()) {
            sendFailedCheckBroadcast("Missing user name");
            return;
        }

        if (userPassphrase.contentEquals(SettingsController.getInstance().GetUser().GetPassphrase())) {
            return;
        }

        validateUser(new LucaUser(SettingsController.getInstance().GetUser().GetName(), userPassphrase));
    }

    private void validateUser(@NonNull LucaUser user) {
        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.VALIDATE_USER.toString();

        _tempUser = user;
        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.User, true);
    }

    private void sendFailedCheckBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Check failed";
        }

        _broadcastController.SendSerializableBroadcast(
                UserCheckedFinishedBroadcast,
                UserCheckedFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }
}
