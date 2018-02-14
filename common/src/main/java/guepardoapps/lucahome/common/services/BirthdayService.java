package guepardoapps.lucahome.common.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.Birthday;
import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToBirthdayConverter;
import guepardoapps.lucahome.common.databases.DatabaseBirthdayList;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.utils.BitmapHelper;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"WeakerAccess"})
public class BirthdayService implements IBirthdayService {
    private static final String Tag = BirthdayService.class.getSimpleName();

    private static final BirthdayService Singleton = new BirthdayService();

    private static final int MinTimeoutMin = 15;
    private static final int MaxTimeoutMin = 24 * 60;

    private Context _context;

    private DatabaseBirthdayList _databaseBirthdayList;

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private NotificationController _notificationController;
    private ReceiverController _receiverController;

    private Calendar _lastUpdate;

    private Class<?> _receiverActivity;
    private boolean _displayNotification;

    private boolean _isInitialized;

    private boolean _loadDataEnabled;

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
                ArrayList<Birthday> birthdayList = JsonDataToBirthdayConverter.getInstance().GetList(contentResponse, _context);
                if (birthdayList == null) {
                    Logger.getInstance().Error(Tag, "Converted birthdayList is null!");
                    _broadcastController.SendBooleanBroadcast(BirthdayDownloadFinishedBroadcast, BirthdayDownloadFinishedBundle, false);
                    return "";
                }

                _databaseBirthdayList.ClearDatabase();
                saveBirthdayListToDatabase(birthdayList);

                ShowNotification();

                _lastUpdate = Calendar.getInstance();
                _broadcastController.SendBooleanBroadcast(BirthdayDownloadFinishedBroadcast, BirthdayDownloadFinishedBundle, true);
            }
            return "Success";
        }
    }

    private BroadcastReceiver _birthdayDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Birthday) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(BirthdayDownloadFinishedBroadcast, BirthdayDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(BirthdayDownloadFinishedBroadcast, BirthdayDownloadFinishedBundle, false);
                return;
            }

            new AsyncConverterTask().execute(contentResponse);
        }
    };

    private BroadcastReceiver _birthdayAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.BirthdayAdd) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(BirthdayAddFinishedBroadcast, BirthdayAddFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(BirthdayAddFinishedBroadcast, BirthdayAddFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(BirthdayAddFinishedBroadcast, BirthdayAddFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _birthdayUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.BirthdayUpdate) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(BirthdayUpdateFinishedBroadcast, BirthdayUpdateFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(BirthdayUpdateFinishedBroadcast, BirthdayUpdateFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(BirthdayUpdateFinishedBroadcast, BirthdayUpdateFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _birthdayDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.BirthdayDelete) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(BirthdayDeleteFinishedBroadcast, BirthdayDeleteFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(BirthdayDeleteFinishedBroadcast, BirthdayDeleteFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(BirthdayDeleteFinishedBroadcast, BirthdayDeleteFinishedBundle, true);
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

    private BirthdayService() {
    }

    public static BirthdayService getInstance() {
        return Singleton;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout, boolean displayNotification, Class<?> receiverActivity) {
        if (_isInitialized) {
            Logger.getInstance().Warning(Tag, "Already initialized!");
            return;
        }

        _loadDataEnabled = true;

        _lastUpdate = Calendar.getInstance();

        _receiverActivity = receiverActivity;
        _displayNotification = displayNotification;
        _reloadEnabled = reloadEnabled;

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _downloadController = new DownloadController(_context);
        _networkController = new NetworkController(_context);
        _notificationController = new NotificationController(_context);
        _receiverController = new ReceiverController(_context);

        _databaseBirthdayList = new DatabaseBirthdayList(_context);
        _databaseBirthdayList.Open();

        _receiverController.RegisterReceiver(_birthdayDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_birthdayAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_birthdayUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_birthdayDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseBirthdayList.Close();
        _isInitialized = false;
    }

    @Override
    public ArrayList<Birthday> GetDataList() {
        try {
            return _databaseBirthdayList.GetList(null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Birthday GetByUuid(@NonNull UUID uuid) {
        try {
            return _databaseBirthdayList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseBirthdayList.KeyUuid, uuid), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new Birthday(uuid, "NULL", Calendar.getInstance(), "NULL", false, false, null, false, ILucaClass.LucaServerDbAction.Null);
        }
    }

    @Override
    public ArrayList<Birthday> SearchDataList(@NonNull String searchKey) {
        ArrayList<Birthday> birthdayList = GetDataList();
        ArrayList<Birthday> foundBirthdays = new ArrayList<>();
        for (int index = 0; index < birthdayList.size(); index++) {
            Birthday entry = birthdayList.get(index);
            if (entry.toString().contains(searchKey)) {
                foundBirthdays.add(entry);
            }
        }
        return foundBirthdays;
    }

    @Override
    public ArrayList<Birthday> GetRemindMeList() {
        try {
            return _databaseBirthdayList.GetList(String.format(Locale.getDefault(), "%s like %d", DatabaseBirthdayList.KeyRemindMe, 1), null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<String> GetNameList() {
        try {
            return _databaseBirthdayList.GetStringQueryList(true, DatabaseBirthdayList.KeyName, null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<String> GetGroupList() {
        try {
            return _databaseBirthdayList.GetStringQueryList(true, DatabaseBirthdayList.KeyGroup, null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void LoadData() {
        if (!_loadDataEnabled) {
            return;
        }

        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            _broadcastController.SendBooleanBroadcast(BirthdayDownloadFinishedBroadcast, BirthdayDownloadFinishedBundle, true);
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(BirthdayDownloadFinishedBroadcast, BirthdayDownloadFinishedBundle, false);
            return;
        }

        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            ArrayList<Birthday> notOnServerBirthdayList = notOnServerEntries();
            for (int index = 0; index < notOnServerBirthdayList.size(); index++) {
                Birthday birthday = notOnServerBirthdayList.get(index);

                switch (birthday.GetServerDbAction()) {
                    case Add:
                        AddEntry(birthday);
                        break;
                    case Update:
                        UpdateEntry(birthday);
                        break;
                    case Delete:
                        DeleteEntry(birthday);
                        break;
                    case Null:
                    default:
                        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Nothing todo with %s.", birthday));
                        break;
                }

            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_BIRTHDAYS.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Birthday, true);
    }

    @Override
    public void AddEntry(@NonNull Birthday entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);
            _databaseBirthdayList.AddEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(BirthdayAddFinishedBroadcast, BirthdayAddFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.BirthdayAdd, true);
    }

    @Override
    public void UpdateEntry(@NonNull Birthday entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);
            _databaseBirthdayList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(BirthdayUpdateFinishedBroadcast, BirthdayUpdateFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.BirthdayUpdate, true);
    }

    @Override
    public void DeleteEntry(@NonNull Birthday entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);
            _databaseBirthdayList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(BirthdayDeleteFinishedBroadcast, BirthdayDeleteFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.BirthdayDelete, true);
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

    @Override
    public void ShowNotification() {
        if (!_displayNotification) {
            Logger.getInstance().Warning(Tag, "_displayNotification is false!");
            return;
        }

        if (_receiverActivity == null) {
            Logger.getInstance().Error(Tag, "ReceiverActivity is null!");
            return;
        }

        ArrayList<Birthday> birthdayList = GetDataList();
        for (int index = 0; index < birthdayList.size(); index++) {
            Birthday birthday = birthdayList.get(index);
            if (birthday.HasBirthday() && birthday.GetRemindMe() && SettingsController.getInstance().IsBirthdayNotificationEnabled()) {
                _notificationController.CreateSimpleNotification(birthday.GetNotificationId(), R.drawable.notification_birthday, _receiverActivity, birthday.GetPhoto(), birthday.GetName(), birthday.GetNotificationBody(), true);
            }
        }
    }

    @Override
    public void CloseNotification() {
        ArrayList<Birthday> birthdayList = GetDataList();
        for (int index = 0; index < birthdayList.size(); index++) {
            _notificationController.CloseNotification(birthdayList.get(index).GetNotificationId());
        }
    }

    @Override
    public void SetDisplayNotification(boolean displayNotification) {
        _displayNotification = displayNotification;

        if (!_displayNotification) {
            CloseNotification();
        } else {
            ShowNotification();
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
    public ArrayList<Birthday> RetrieveContactPhotos(@NonNull ArrayList<Birthday> birthdayList) {
        for (int index = 0; index < birthdayList.size(); index++) {
            Birthday birthday = birthdayList.get(index);

            Bitmap photo = BitmapFactory.decodeResource(_context.getResources(), R.mipmap.ic_face_white_48dp);
            try {
                photo = BitmapHelper.RetrieveContactPhoto(_context, birthday.GetName(), 100, 100, true);
            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.getMessage());
            }

            birthday.SetPhoto(photo);
            birthdayList.set(index, birthday);
        }

        return birthdayList;
    }

    private void saveBirthdayListToDatabase(@NonNull ArrayList<Birthday> birthdayList) {
        for (int index = 0; index < birthdayList.size(); index++) {
            Birthday birthday = birthdayList.get(index);
            _databaseBirthdayList.AddEntry(birthday);
        }
    }

    private ArrayList<Birthday> notOnServerEntries() {
        try {
            return _databaseBirthdayList.GetList(String.format(Locale.getDefault(), "%s like %d", DatabaseBirthdayList.KeyIsOnServer, 0), null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerEntries().size() > 0;
    }
}
