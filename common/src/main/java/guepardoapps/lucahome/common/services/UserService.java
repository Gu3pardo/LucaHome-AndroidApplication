package guepardoapps.lucahome.common.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"unused", "WeakerAccess"})
public class UserService implements IUserService {
    private static final String Tag = UserService.class.getSimpleName();

    private static final UserService Singleton = new UserService();

    private static final int MinUserNameLength = 4;
    private static final int MinPasswordLength = 6;

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private ReceiverController _receiverController;

    private boolean _isInitialized;

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
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(UserCheckedFinishedBroadcast, UserCheckedFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(UserCheckedFinishedBroadcast, UserCheckedFinishedBundle, false);
                return;
            }

            User user = (User) content.Additional;
            if (user == null) {
                Logger.getInstance().Error(Tag, "Provided user is null!");
                _broadcastController.SendBooleanBroadcast(UserCheckedFinishedBroadcast, UserCheckedFinishedBundle, false);
                return;
            }

            SettingsController.getInstance().SetUser(user);
            _broadcastController.SendBooleanBroadcast(UserCheckedFinishedBroadcast, UserCheckedFinishedBundle, true);
        }
    };

    private UserService() {
    }

    public static UserService getInstance() {
        return Singleton;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout, boolean displayNotification, Class<?> receiverActivity) {
        if (_isInitialized) {
            Logger.getInstance().Warning(Tag, "Already initialized!");
            return;
        }

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _receiverController = new ReceiverController(context);

        _receiverController.RegisterReceiver(_userCheckedFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _receiverController.Dispose();
        _isInitialized = false;
    }

    @Override
    public void SetUser(@NonNull User user) {
        SettingsController.getInstance().SetUser(user);
    }

    @Override
    public User GetUser() {
        return SettingsController.getInstance().GetUser();
    }

    @Override
    public void ValidateUser() {
        if (!IsAnUserSaved()) {
            Logger.getInstance().Error(Tag, "No user saved!");
            _broadcastController.SendBooleanBroadcast(UserCheckedFinishedBroadcast, UserCheckedFinishedBundle, false);
            return;
        }
        User user = SettingsController.getInstance().GetUser();
        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.VALIDATE_USER.toString();
        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.User, true, user);
    }

    @Override
    public boolean IsAnUserSaved() {
        User user = SettingsController.getInstance().GetUser();
        return !(user.GetName() == null || user.GetPassphrase() == null
                || user.GetName().length() == 0 || user.GetPassphrase().length() == 0
                || user.GetName().contentEquals("NA") || user.GetPassphrase().contentEquals("NA")
                || user.GetName().contentEquals("Dummy") || user.GetPassphrase().contentEquals("Dummy")
                || user.GetName().contentEquals("DUMMY") || user.GetPassphrase().contentEquals("DUMMY"));
    }

    @Override
    public boolean IsEnteredUserValid(@NonNull String user) {
        return (user.length() >= MinUserNameLength) && (!user.contains(" "));
    }

    @Override
    public boolean IsEnteredPasswordValid(@NonNull String password) {
        return password.length() >= MinPasswordLength;
    }

    @Override
    public ArrayList<User> GetDataList() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetDataList not implemented for " + Tag);
    }

    @Override
    public User GetByUuid(@NonNull UUID uuid) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetByUuid not implemented for " + Tag);
    }

    @Override
    public ArrayList<User> SearchDataList(@NonNull String searchKey) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SearchDataList not implemented for " + Tag);
    }

    @Override
    public void LoadData() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method LoadData not implemented for " + Tag);
    }

    @Override
    public void AddEntry(@NonNull User newEntry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method AddEntry not implemented for " + Tag);
    }

    @Override
    public void UpdateEntry(@NonNull User updateEntry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method UpdateEntry not implemented for " + Tag);
    }

    @Override
    public void DeleteEntry(@NonNull User deleteEntry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method DeleteEntry not implemented for " + Tag);
    }

    @Override
    public void SetReloadEnabled(boolean reloadEnabled) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetReloadEnabled not implemented for " + Tag);
    }

    @Override
    public boolean GetReloadEnabled() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetReloadEnabled not implemented for " + Tag);
    }

    @Override
    public void SetReloadTimeout(int reloadTimeout) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetReloadTimeout not implemented for " + Tag);
    }

    @Override
    public int GetReloadTimeout() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetReloadTimeout not implemented for " + Tag);
    }

    @Override
    public Calendar GetLastUpdate() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetLastUpdate not implemented for " + Tag);
    }

    @Override
    public void ShowNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method ShowNotification not implemented for " + Tag);
    }

    @Override
    public void CloseNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method CloseNotification not implemented for " + Tag);
    }

    @Override
    public void SetDisplayNotification(boolean displayNotification) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetDisplayNotification not implemented for " + Tag);
    }

    @Override
    public boolean GetDisplayNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetDisplayNotification not implemented for " + Tag);
    }

    @Override
    public Class<?> GetReceiverActivity() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetReceiverActivity not implemented for " + Tag);
    }

    @Override
    public void SetReceiverActivity(Class receiverActivity) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetReceiverActivity not implemented for " + Tag);
    }
}
