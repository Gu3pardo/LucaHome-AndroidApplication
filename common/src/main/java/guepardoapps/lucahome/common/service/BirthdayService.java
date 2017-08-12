package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaBirthday;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToBirthdayConverter;
import guepardoapps.lucahome.common.database.DatabaseBirthdayList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class BirthdayService {
    public static class BirthdayDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<LucaBirthday> BirthdayList;

        public BirthdayDownloadFinishedContent(SerializableList<LucaBirthday> birthdayList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            BirthdayList = birthdayList;
        }
    }

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
    private Logger _logger;

    private Class<?> _receiverActivity;

    private static final int TIMEOUT_MS = 60 * 60 * 1000;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_reloadListRunnable run");
            LoadBirthdayList();
            _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);
        }
    };

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private NotificationController _notificationController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private DatabaseBirthdayList _databaseBirthdayList;

    private JsonDataToBirthdayConverter _jsonDataToBirthdayConverter;

    private SerializableList<LucaBirthday> _birthdayList = new SerializableList<>();

    private BroadcastReceiver _birthdayDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_birthdayDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Birthday) {
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

            SerializableList<LucaBirthday> birthdayList = _jsonDataToBirthdayConverter.GetList(contentResponse);
            if (birthdayList == null) {
                _logger.Error("Converted birthdayList is null!");
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _birthdayList = birthdayList;

            CheckForBirthdays();

            clearBirthdayListFromDatabase();
            saveBirthdayListToDatabase();

            _broadcastController.SendSerializableBroadcast(
                    BirthdayDownloadFinishedBroadcast,
                    BirthdayDownloadFinishedBundle,
                    new BirthdayDownloadFinishedContent(_birthdayList, true, content.Response));
        }
    };

    private BroadcastReceiver _birthdayAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_birthdayAddFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.BirthdayAdd) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedAddBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedAddBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    BirthdayAddFinishedBroadcast,
                    BirthdayAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadBirthdayList();
        }
    };

    private BroadcastReceiver _birthdayUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_birthdayUpdateFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.BirthdayUpdate) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedUpdateBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", content.Response));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedUpdateBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    BirthdayUpdateFinishedBroadcast,
                    BirthdayUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadBirthdayList();
        }
    };

    private BroadcastReceiver _birthdayDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_birthdayDeleteFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.BirthdayDelete) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedDeleteBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedDeleteBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    BirthdayDeleteFinishedBroadcast,
                    BirthdayDeleteFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadBirthdayList();
        }
    };

    private BirthdayService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static BirthdayService getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context, @NonNull Class<?> receiverActivity) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _receiverActivity = receiverActivity;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _notificationController = new NotificationController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _databaseBirthdayList = new DatabaseBirthdayList(context);
        _databaseBirthdayList.Open();

        _receiverController.RegisterReceiver(_birthdayDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_birthdayAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_birthdayUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_birthdayDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _jsonDataToBirthdayConverter = new JsonDataToBirthdayConverter();

        _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);

        _isInitialized = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseBirthdayList.Close();
        _isInitialized = false;
    }

    public SerializableList<LucaBirthday> GetBirthdayList() {
        return _birthdayList;
    }

    public ArrayList<String> GetBirthdayNameList() {
        ArrayList<String> birthdayNameList = new ArrayList<>();

        for (int index = 0; index < _birthdayList.getSize(); index++) {
            birthdayNameList.add(_birthdayList.getValue(index).GetName());
        }

        return birthdayNameList;
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

    public SerializableList<LucaBirthday> FoundBirthdays(@NonNull String searchKey) {
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

    public void LoadBirthdayList() {
        _logger.Debug("LoadBirthdayList");

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _birthdayList = _databaseBirthdayList.GetBirthdayList();
            _broadcastController.SendSerializableBroadcast(
                    BirthdayDownloadFinishedBroadcast,
                    BirthdayDownloadFinishedBundle,
                    new BirthdayDownloadFinishedContent(_birthdayList, true, Tools.CompressStringToByteArray("Loaded from database!")));
            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast("No user");
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_BIRTHDAYS.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Birthday, true);
    }

    public void AddBirthday(@NonNull LucaBirthday entry) {
        _logger.Debug(String.format(Locale.getDefault(), "AddBirthday: Adding new entry %s", entry));

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
        _logger.Debug(String.format(Locale.getDefault(), "UpdateBirthday: Updating entry %s", entry));

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
        _logger.Debug(String.format(Locale.getDefault(), "DeleteBirthday: Deleting entry %s", entry));

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

    public void SetReceiverActivity(@NonNull Class<?> receiverActivity) {
        _receiverActivity = receiverActivity;
    }

    public Class<?> GetReceiverActivity() {
        return _receiverActivity;
    }

    public void CheckForBirthdays() {
        _logger.Debug("CheckForBirthdays");

        for (int index = 0; index < _birthdayList.getSize(); index++) {
            LucaBirthday birthday = _birthdayList.getValue(index);
            if (birthday.HasBirthday() && _settingsController.IsBirthdayNotificationEnabled()) {
                _notificationController.CreateBirthdayNotification(birthday.GetNotificationId(), _receiverActivity, birthday.Icon(), birthday.GetName(), birthday.GetNotificationBody(), true);
            }
        }
    }

    private void clearBirthdayListFromDatabase() {
        _logger.Debug("clearBirthdayListFromDatabase");

        SerializableList<LucaBirthday> birthdayList = _databaseBirthdayList.GetBirthdayList();
        for (int index = 0; index < birthdayList.getSize(); index++) {
            LucaBirthday birthday = birthdayList.getValue(index);
            _databaseBirthdayList.Delete(birthday);
        }
    }

    private void saveBirthdayListToDatabase() {
        _logger.Debug("saveBirthdayListToDatabase");

        for (int index = 0; index < _birthdayList.getSize(); index++) {
            LucaBirthday birthday = _birthdayList.getValue(index);
            _databaseBirthdayList.CreateEntry(birthday);
        }
    }

    public void CloseNotifications() {
        _logger.Debug("CloseNotifications");
        for (int index = 0; index < _birthdayList.getSize(); index++) {
            LucaBirthday birthday = _birthdayList.getValue(index);
            _notificationController.CloseNotification(birthday.GetNotificationId());
        }
    }

    private void sendFailedDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                BirthdayDownloadFinishedBroadcast,
                BirthdayDownloadFinishedBundle,
                new BirthdayDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedAddBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                BirthdayAddFinishedBroadcast,
                BirthdayAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedUpdateBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                BirthdayUpdateFinishedBroadcast,
                BirthdayUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedDeleteBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                BirthdayDeleteFinishedBroadcast,
                BirthdayDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }
}
