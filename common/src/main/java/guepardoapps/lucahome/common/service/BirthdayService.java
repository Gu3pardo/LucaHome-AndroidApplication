package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.LucaBirthday;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToBirthdayConverter;
import guepardoapps.lucahome.common.database.DatabaseBirthdayList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.interfaces.services.IDataNotificationService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class BirthdayService implements IDataNotificationService {
    public static final String BirthdayIntent = "BirthdayIntent";

    public static final String BirthdayDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.birthday.download.finished";
    public static final String BirthdayDownloadFinishedBundle = "BirthdayDownloadFinishedBundle";

    public static final String BirthdayAddFinishedBroadcast = "guepardoapps.lucahome.data.service.birthday.add.finished";
    public static final String BirthdayAddFinishedBundle = "BirthdayAddFinishedBundle";

    public static final String BirthdayUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.birthday.update.finished";
    public static final String BirthdayUpdateFinishedBundle = "BirthdayUpdateFinishedBundle";

    public static final String BirthdayDeleteFinishedBroadcast = "guepardoapps.lucahome.data.service.birthday.delete.finished";
    public static final String BirthdayDeleteFinishedBundle = "BirthdayDeleteFinishedBundle";

    private static final BirthdayService SINGLETON = new BirthdayService();
    private boolean _isInitialized;

    private static final String TAG = BirthdayService.class.getSimpleName();

    private boolean _loadDataEnabled;

    private Date _lastUpdate;

    private boolean _displayNotification;
    private Class<?> _receiverActivity;

    private static final int MIN_TIMEOUT_MIN = 4 * 60;
    private static final int MAX_TIMEOUT_MIN = 24 * 60;

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

    private Context _context;

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private NotificationController _notificationController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private DatabaseBirthdayList _databaseBirthdayList;

    private SerializableList<LucaBirthday> _birthdayList = new SerializableList<>();

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
                Logger.getInstance().Error(TAG, contentResponse);
                _birthdayList = _databaseBirthdayList.GetBirthdayList();
                sendFailedDownloadBroadcast();
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                _birthdayList = _databaseBirthdayList.GetBirthdayList();
                sendFailedDownloadBroadcast();
                return;
            }

            SerializableList<LucaBirthday> birthdayList = JsonDataToBirthdayConverter.getInstance().GetList(contentResponse, _context);
            if (birthdayList == null) {
                Logger.getInstance().Error(TAG, "Converted birthdayList is null!");
                _birthdayList = _databaseBirthdayList.GetBirthdayList();
                sendFailedDownloadBroadcast();
                return;
            }

            _birthdayList = birthdayList;

            ShowNotification();

            clearBirthdayListFromDatabase();
            saveBirthdayListToDatabase();

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    BirthdayDownloadFinishedBroadcast,
                    BirthdayDownloadFinishedBundle,
                    new ObjectChangeFinishedContent(true));
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
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedAddBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedAddBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    BirthdayAddFinishedBroadcast,
                    BirthdayAddFinishedBundle,
                    new ObjectChangeFinishedContent(true));

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
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedUpdateBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedUpdateBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    BirthdayUpdateFinishedBroadcast,
                    BirthdayUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true));

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
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedDeleteBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedDeleteBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    BirthdayDeleteFinishedBroadcast,
                    BirthdayDeleteFinishedBundle,
                    new ObjectChangeFinishedContent(true));

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

    private BirthdayService() {
    }

    public static BirthdayService getInstance() {
        return SINGLETON;
    }

    @Override
    public void Initialize(@NonNull Context context, @NonNull Class<?> receiverActivity, boolean displayNotification, boolean reloadEnabled, int reloadTimeout) {
        if (_isInitialized) {
            Logger.getInstance().Warning(TAG, "Already initialized!");
            return;
        }

        _loadDataEnabled = true;

        _lastUpdate = new Date();

        _receiverActivity = receiverActivity;
        _displayNotification = displayNotification;
        _reloadEnabled = reloadEnabled;

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _downloadController = new DownloadController(_context);
        _networkController = new NetworkController(_context);
        _notificationController = new NotificationController(_context);
        _receiverController = new ReceiverController(_context);
        _settingsController = SettingsController.getInstance();

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
    public SerializableList<LucaBirthday> GetDataList() {
        return _birthdayList;
    }

    public ArrayList<String> GetBirthdayNameList() {
        ArrayList<String> birthdayNameList = new ArrayList<>();
        for (int index = 0; index < _birthdayList.getSize(); index++) {
            birthdayNameList.add(_birthdayList.getValue(index).GetName());
        }
        return birthdayNameList;
    }

    public ArrayList<String> GetBirthdayGroupList() {
        ArrayList<String> birthdayGroupList = new ArrayList<>();
        for (int index = 0; index < _birthdayList.getSize(); index++) {
            birthdayGroupList.add(_birthdayList.getValue(index).GetGroup());
        }
        return birthdayGroupList;
    }

    public LucaBirthday GetById(int id) {
        for (int index = 0; index < _birthdayList.getSize(); index++) {
            LucaBirthday entry = _birthdayList.getValue(index);

            if (entry.GetId() == id) {
                return entry;
            }
        }

        return null;
    }

    @Override
    public SerializableList<LucaBirthday> SearchDataList(@NonNull String searchKey) {
        SerializableList<LucaBirthday> foundBirthdays = new SerializableList<>();

        for (int index = 0; index < _birthdayList.getSize(); index++) {
            LucaBirthday entry = _birthdayList.getValue(index);

            if (String.valueOf(entry.GetId()).contains(searchKey)
                    || entry.GetName().contains(searchKey)
                    || entry.GetDate().toString().contains(searchKey)
                    || String.valueOf(entry.GetAge()).contains(searchKey)) {
                foundBirthdays.addValue(entry);
            }
        }

        return foundBirthdays;
    }

    @Override
    public void LoadData() {
        if (!_loadDataEnabled) {
            return;
        }

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _birthdayList = _databaseBirthdayList.GetBirthdayList();
            _broadcastController.SendSerializableBroadcast(
                    BirthdayDownloadFinishedBroadcast,
                    BirthdayDownloadFinishedBundle,
                    new ObjectChangeFinishedContent(true));
            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast();
            return;
        }

        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            for (int index = 0; index < notOnServerBirthdays().getSize(); index++) {
                LucaBirthday lucaBirthday = notOnServerBirthdays().getValue(index);

                switch (lucaBirthday.GetServerDbAction()) {
                    case Add:
                        AddBirthday(lucaBirthday);
                        break;
                    case Update:
                        UpdateBirthday(lucaBirthday);
                        break;
                    case Delete:
                        DeleteBirthday(lucaBirthday);
                        break;
                    case Null:
                    default:
                        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "Nothing todo with %s.", lucaBirthday));
                        break;
                }

            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_BIRTHDAYS.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Birthday, true);
    }

    public void AddBirthday(@NonNull LucaBirthday entry) {
        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);

            _databaseBirthdayList.CreateEntry(entry);

            LoadData();

            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedAddBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.BirthdayAdd, true);
    }

    public void UpdateBirthday(@NonNull LucaBirthday entry) {
        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);

            _databaseBirthdayList.Update(entry);

            LoadData();

            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedUpdateBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.BirthdayUpdate, true);
    }

    public void DeleteBirthday(@NonNull LucaBirthday entry) {
        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);

            _databaseBirthdayList.Update(entry);

            LoadData();

            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDeleteBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.BirthdayDelete, true);
    }

    public void RetrieveContactPhotos() {
        for (int index = 0; index < _birthdayList.getSize(); index++) {
            LucaBirthday birthday = _birthdayList.getValue(index);

            Bitmap photo = BitmapFactory.decodeResource(_context.getResources(), guepardoapps.lucahome.basic.R.mipmap.ic_face_white_48dp);
            try {
                photo = Tools.RetrieveContactPhoto(_context, birthday.GetName(), 100, 100, true);
            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.getMessage());
            }

            birthday.SetPhoto(photo);
        }

        _broadcastController.SendSerializableBroadcast(
                BirthdayDownloadFinishedBroadcast,
                BirthdayDownloadFinishedBundle,
                new ObjectChangeFinishedContent(true));
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
    public void ShowNotification() {
        if (!_displayNotification) {
            Logger.getInstance().Warning(TAG, "_displayNotification is false!");
            return;
        }

        for (int index = 0; index < _birthdayList.getSize(); index++) {
            LucaBirthday birthday = _birthdayList.getValue(index);
            if (birthday.HasBirthday() && _settingsController.IsBirthdayNotificationEnabled()) {
                _notificationController.CreateSimpleNotification(birthday.GetNotificationId(), R.drawable.birthday, _receiverActivity, birthday.GetPhoto(), birthday.GetName(), birthday.GetNotificationBody(), true);
            }
        }
    }

    @Override
    public void CloseNotification() {
        for (int index = 0; index < _birthdayList.getSize(); index++) {
            LucaBirthday birthday = _birthdayList.getValue(index);
            _notificationController.CloseNotification(birthday.GetNotificationId());
        }
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
            ShowNotification();
        }
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

    public Date GetLastUpdate() {
        return _lastUpdate;
    }

    private void clearBirthdayListFromDatabase() {
        SerializableList<LucaBirthday> birthdayList = _databaseBirthdayList.GetBirthdayList();
        for (int index = 0; index < birthdayList.getSize(); index++) {
            LucaBirthday birthday = birthdayList.getValue(index);
            _databaseBirthdayList.Delete(birthday);
        }
    }

    private void saveBirthdayListToDatabase() {
        for (int index = 0; index < _birthdayList.getSize(); index++) {
            LucaBirthday birthday = _birthdayList.getValue(index);
            _databaseBirthdayList.CreateEntry(birthday);
        }
    }

    private void sendFailedDownloadBroadcast() {
        _broadcastController.SendSerializableBroadcast(
                BirthdayDownloadFinishedBroadcast,
                BirthdayDownloadFinishedBundle,
                new ObjectChangeFinishedContent(false));
    }

    private void sendFailedAddBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Add for birthday failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                BirthdayAddFinishedBroadcast,
                BirthdayAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedUpdateBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Update for birthday failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                BirthdayUpdateFinishedBroadcast,
                BirthdayUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedDeleteBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Delete for birthday failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                BirthdayDeleteFinishedBroadcast,
                BirthdayDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private SerializableList<LucaBirthday> notOnServerBirthdays() {
        SerializableList<LucaBirthday> notOnServerBirthdayList = new SerializableList<>();

        for (int index = 0; index < _birthdayList.getSize(); index++) {
            if (!_birthdayList.getValue(index).GetIsOnServer()) {
                notOnServerBirthdayList.addValue(_birthdayList.getValue(index));
            }
        }

        return notOnServerBirthdayList;
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerBirthdays().getSize() > 0;
    }
}
