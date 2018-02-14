package guepardoapps.lucahome.common.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.MoneyLog;
import guepardoapps.lucahome.common.classes.MoneyLogItem;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToMoneyLogConverter;
import guepardoapps.lucahome.common.databases.DatabaseMoneyLogItemList;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"WeakerAccess"})
public class MoneyLogService implements IMoneyLogService {
    private static final String Tag = MoneyLogService.class.getSimpleName();

    private static final MoneyLogService Singleton = new MoneyLogService();

    private static final int MinTimeoutMin = 60;
    private static final int MaxTimeoutMin = 24 * 60;

    private DatabaseMoneyLogItemList _databaseMoneyLogItemList;

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;

    private Calendar _lastUpdate;

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
                ArrayList<MoneyLogItem> list = JsonDataToMoneyLogConverter.getInstance().GetList(contentResponse);
                if (list == null) {
                    Logger.getInstance().Error(Tag, "Converted list is null!");
                    _broadcastController.SendBooleanBroadcast(MoneyLogItemDownloadFinishedBroadcast, MoneyLogItemDownloadFinishedBundle, false);
                    return "";
                }

                _databaseMoneyLogItemList.ClearDatabase();
                saveMoneyLogItemListToDatabase(list);

                _lastUpdate = Calendar.getInstance();
                _broadcastController.SendBooleanBroadcast(MoneyLogItemDownloadFinishedBroadcast, MoneyLogItemDownloadFinishedBundle, true);
            }
            return "Success";
        }
    }

    private BroadcastReceiver _moneyLogItemDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MoneyLogItem) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(MoneyLogItemDownloadFinishedBroadcast, MoneyLogItemDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(MoneyLogItemDownloadFinishedBroadcast, MoneyLogItemDownloadFinishedBundle, false);
                return;
            }

            new AsyncConverterTask().execute(contentResponse);
        }
    };

    private BroadcastReceiver _moneyLogItemAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MoneyLogItemAdd) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(MoneyLogItemAddFinishedBroadcast, MoneyLogItemAddFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Adding was not successful!");
                _broadcastController.SendBooleanBroadcast(MoneyLogItemAddFinishedBroadcast, MoneyLogItemAddFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(MoneyLogItemAddFinishedBroadcast, MoneyLogItemAddFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _moneyLogItemUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MoneyLogItemUpdate) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(MoneyLogItemUpdateFinishedBroadcast, MoneyLogItemUpdateFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Update was not successful!");
                _broadcastController.SendBooleanBroadcast(MoneyLogItemUpdateFinishedBroadcast, MoneyLogItemUpdateFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(MoneyLogItemUpdateFinishedBroadcast, MoneyLogItemUpdateFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _moneyLogItemDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MoneyLogItemDelete) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(MoneyLogItemDeleteFinishedBroadcast, MoneyLogItemDeleteFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Deleting was not successful!");
                _broadcastController.SendBooleanBroadcast(MoneyLogItemDeleteFinishedBroadcast, MoneyLogItemDeleteFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(MoneyLogItemDeleteFinishedBroadcast, MoneyLogItemDeleteFinishedBundle, true);
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

    private MoneyLogService() {
    }

    public static MoneyLogService getInstance() {
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

        _reloadEnabled = reloadEnabled;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _receiverController = new ReceiverController(context);

        _databaseMoneyLogItemList = new DatabaseMoneyLogItemList(context);
        _databaseMoneyLogItemList.Open();

        _receiverController.RegisterReceiver(_moneyLogItemDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_moneyLogItemAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_moneyLogItemUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_moneyLogItemDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseMoneyLogItemList.Close();
        _isInitialized = false;
    }

    @Override
    public ArrayList<MoneyLogItem> GetDataList() {
        try {
            return _databaseMoneyLogItemList.GetList(null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<MoneyLogItem> GetByTypeUuid(@NonNull UUID typeUuid) {
        try {
            return _databaseMoneyLogItemList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseMoneyLogItemList.KeyTypeUuid, typeUuid), null, null);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<MoneyLog> GetMoneyLogList() {
        ArrayList<MoneyLog> moneyLogList = new ArrayList<>();

        for (UUID typeUuid : GetTypeUuidList()) {
            ArrayList<MoneyLogItem> moneyLogItemList = GetByTypeUuid(typeUuid);
            if (moneyLogItemList.size() > 0) {
                MoneyLogItem exampleEntry = moneyLogItemList.get(0);
                moneyLogList.add(new MoneyLog(exampleEntry.GetTypeUuid(), exampleEntry.GetBank(), exampleEntry.GetPlan(), exampleEntry.GetUser(), moneyLogItemList));
            }
        }

        return moneyLogList;
    }

    @Override
    public MoneyLogItem GetByUuid(@NonNull UUID uuid) {
        try {
            return _databaseMoneyLogItemList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseMoneyLogItemList.KeyUuid, uuid), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new MoneyLogItem(uuid, UUID.randomUUID(), "NULL", "NULL", 0, "NULL", Calendar.getInstance(), "NULL", false, ILucaClass.LucaServerDbAction.Null);
        }
    }

    @Override
    public ArrayList<MoneyLogItem> SearchDataList(@NonNull String searchKey) {
        ArrayList<MoneyLogItem> list = GetDataList();
        ArrayList<MoneyLogItem> foundList = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            MoneyLogItem entry = list.get(index);
            if (entry.toString().contains(searchKey)) {
                foundList.add(entry);
            }
        }
        return foundList;
    }

    @Override
    public ArrayList<UUID> GetTypeUuidList() {
        try {
            ArrayList<String> uuidStringList = _databaseMoneyLogItemList.GetStringQueryList(true, DatabaseMoneyLogItemList.KeyTypeUuid, null, null, null);
            ArrayList<UUID> uuidList = new ArrayList<>();
            for (String uuidString : uuidStringList) {
                uuidList.add(UUID.fromString(uuidString));
            }
            return uuidList;
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
            _broadcastController.SendBooleanBroadcast(MoneyLogItemDownloadFinishedBroadcast, MoneyLogItemDownloadFinishedBundle, true);
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(MoneyLogItemDownloadFinishedBroadcast, MoneyLogItemDownloadFinishedBundle, false);
            return;
        }

        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            ArrayList<MoneyLogItem> notOnServerMoneyLogItemList = notOnServerEntries();
            for (int index = 0; index < notOnServerMoneyLogItemList.size(); index++) {
                MoneyLogItem moneyLogItem = notOnServerMoneyLogItemList.get(index);

                switch (moneyLogItem.GetServerDbAction()) {
                    case Add:
                        AddEntry(moneyLogItem);
                        break;
                    case Update:
                        UpdateEntry(moneyLogItem);
                        break;
                    case Delete:
                        DeleteEntry(moneyLogItem);
                        break;
                    case Null:
                    default:
                        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Nothing todo with %s.", moneyLogItem));
                        break;
                }
            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_MONEY_LOGS_USER.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MoneyLogItem, true);
    }

    @Override
    public void AddEntry(@NonNull MoneyLogItem entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);
            _databaseMoneyLogItemList.AddEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(MoneyLogItemAddFinishedBroadcast, MoneyLogItemAddFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MoneyLogItemAdd, true);
    }

    @Override
    public void UpdateEntry(@NonNull MoneyLogItem entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);
            _databaseMoneyLogItemList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(MoneyLogItemUpdateFinishedBroadcast, MoneyLogItemUpdateFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MoneyLogItemUpdate, true);
    }

    @Override
    public void DeleteEntry(@NonNull MoneyLogItem entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);
            _databaseMoneyLogItemList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(MoneyLogItemDeleteFinishedBroadcast, MoneyLogItemDeleteFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MoneyLogItemDelete, true);
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
    public void SetReceiverActivity(@NonNull Class<?> receiverActivity) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetReceiverActivity not implemented for " + Tag);
    }

    @Override
    public Class<?> GetReceiverActivity() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetReceiverActivity not implemented for " + Tag);
    }

    private void saveMoneyLogItemListToDatabase(@NonNull ArrayList<MoneyLogItem> list) {
        for (int index = 0; index < list.size(); index++) {
            MoneyLogItem entry = list.get(index);
            _databaseMoneyLogItemList.AddEntry(entry);
        }
    }

    private ArrayList<MoneyLogItem> notOnServerEntries() {
        try {
            return _databaseMoneyLogItemList.GetList(String.format(Locale.getDefault(), "%s like %d", DatabaseMoneyLogItemList.KeyIsOnServer, 0), null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerEntries().size() > 0;
    }
}
