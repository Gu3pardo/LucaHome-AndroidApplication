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
import guepardoapps.lucahome.common.classes.MeterLog;
import guepardoapps.lucahome.common.classes.MeterLogItem;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToMeterLogConverter;
import guepardoapps.lucahome.common.databases.DatabaseMeterLogItemList;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"WeakerAccess"})
public class MeterLogService implements IMeterLogService {
    private static final String Tag = MeterLogService.class.getSimpleName();

    private static final MeterLogService Singleton = new MeterLogService();

    private static final int MinTimeoutMin = 60;
    private static final int MaxTimeoutMin = 24 * 60;

    private DatabaseMeterLogItemList _databaseMeterLogItemList;

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
                ArrayList<MeterLogItem> meterLogItemList = JsonDataToMeterLogConverter.getInstance().GetList(contentResponse);
                if (meterLogItemList == null) {
                    Logger.getInstance().Error(Tag, "Converted meterLogItemList is null!");
                    _broadcastController.SendBooleanBroadcast(MeterLogItemDownloadFinishedBroadcast, MeterLogItemDownloadFinishedBundle, false);
                    return "";
                }

                _databaseMeterLogItemList.ClearDatabase();
                saveMeterLogItemListToDatabase(meterLogItemList);

                _lastUpdate = Calendar.getInstance();
                _broadcastController.SendBooleanBroadcast(MeterLogItemDownloadFinishedBroadcast, MeterLogItemDownloadFinishedBundle, true);
            }
            return "Success";
        }
    }

    private BroadcastReceiver _meterLogItemDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MeterLogItem) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(MeterLogItemDownloadFinishedBroadcast, MeterLogItemDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(MeterLogItemDownloadFinishedBroadcast, MeterLogItemDownloadFinishedBundle, false);
                return;
            }

            new AsyncConverterTask().execute(contentResponse);
        }
    };

    private BroadcastReceiver _meterLogItemAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MeterLogItemAdd) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(MeterLogItemAddFinishedBroadcast, MeterLogItemAddFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Adding was not successful!");
                _broadcastController.SendBooleanBroadcast(MeterLogItemAddFinishedBroadcast, MeterLogItemAddFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(MeterLogItemAddFinishedBroadcast, MeterLogItemAddFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _meterLogItemUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MeterLogItemUpdate) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(MeterLogItemUpdateFinishedBroadcast, MeterLogItemUpdateFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Update was not successful!");
                _broadcastController.SendBooleanBroadcast(MeterLogItemUpdateFinishedBroadcast, MeterLogItemUpdateFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(MeterLogItemUpdateFinishedBroadcast, MeterLogItemUpdateFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _meterLogItemDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MeterLogItemDelete) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(MeterLogItemDeleteFinishedBroadcast, MeterLogItemDeleteFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Deleting was not successful!");
                _broadcastController.SendBooleanBroadcast(MeterLogItemDeleteFinishedBroadcast, MeterLogItemDeleteFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(MeterLogItemDeleteFinishedBroadcast, MeterLogItemDeleteFinishedBundle, true);
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

    private MeterLogService() {
    }

    public static MeterLogService getInstance() {
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

        _databaseMeterLogItemList = new DatabaseMeterLogItemList(context);
        _databaseMeterLogItemList.Open();

        _receiverController.RegisterReceiver(_meterLogItemDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_meterLogItemAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_meterLogItemUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_meterLogItemDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseMeterLogItemList.Close();
        _isInitialized = false;
    }

    @Override
    public ArrayList<MeterLogItem> GetDataList() {
        try {
            return _databaseMeterLogItemList.GetList(null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<MeterLogItem> GetByMeterId(@NonNull String meterId) {
        try {
            return _databaseMeterLogItemList.GetList(String.format(Locale.getDefault(), "%s like %%%s%%", DatabaseMeterLogItemList.KeyMeterId, meterId), null, null);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<MeterLog> GetMeterLogList() {
        ArrayList<MeterLog> meterLogList = new ArrayList<>();

        for (String meterId : GetMeterIdList()) {
            ArrayList<MeterLogItem> meterLogItemList = GetByMeterId(meterId);
            if (meterLogItemList.size() > 0) {
                MeterLogItem exampleEntry = meterLogItemList.get(0);
                meterLogList.add(new MeterLog(exampleEntry.GetUuid(), exampleEntry.GetRoomUuid(), exampleEntry.GetType(), meterId, meterLogItemList));
            }
        }

        return meterLogList;
    }

    @Override
    public MeterLogItem GetByUuid(@NonNull UUID uuid) {
        try {
            return _databaseMeterLogItemList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseMeterLogItemList.KeyUuid, uuid), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new MeterLogItem(uuid, UUID.randomUUID(), UUID.randomUUID(), "NULL", Calendar.getInstance(), "NULL", 0, "NULL", false, ILucaClass.LucaServerDbAction.Null);
        }
    }

    @Override
    public ArrayList<MeterLogItem> SearchDataList(@NonNull String searchKey) {
        ArrayList<MeterLogItem> list = GetDataList();
        ArrayList<MeterLogItem> foundList = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            MeterLogItem entry = list.get(index);
            if (entry.toString().contains(searchKey)) {
                foundList.add(entry);
            }
        }
        return foundList;
    }

    @Override
    public ArrayList<String> GetMeterIdList() {
        try {
            return _databaseMeterLogItemList.GetStringQueryList(true, DatabaseMeterLogItemList.KeyMeterId, null, null, null);
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
            _broadcastController.SendBooleanBroadcast(MeterLogItemDownloadFinishedBroadcast, MeterLogItemDownloadFinishedBundle, true);
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(MeterLogItemDownloadFinishedBroadcast, MeterLogItemDownloadFinishedBundle, false);
            return;
        }

        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            ArrayList<MeterLogItem> notOnServerMeterLogItemList = notOnServerEntries();
            for (int index = 0; index < notOnServerMeterLogItemList.size(); index++) {
                MeterLogItem meterLogItem = notOnServerMeterLogItemList.get(index);

                switch (meterLogItem.GetServerDbAction()) {
                    case Add:
                        AddEntry(meterLogItem);
                        break;
                    case Update:
                        UpdateEntry(meterLogItem);
                        break;
                    case Delete:
                        DeleteEntry(meterLogItem);
                        break;
                    case Null:
                    default:
                        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Nothing todo with %s.", meterLogItem));
                        break;
                }
            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_METER_LOGS.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MeterLogItem, true);
    }

    @Override
    public void AddEntry(@NonNull MeterLogItem entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);
            _databaseMeterLogItemList.AddEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(MeterLogItemAddFinishedBroadcast, MeterLogItemAddFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MeterLogItemAdd, true);
    }

    @Override
    public void UpdateEntry(@NonNull MeterLogItem entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);
            _databaseMeterLogItemList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(MeterLogItemUpdateFinishedBroadcast, MeterLogItemUpdateFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MeterLogItemUpdate, true);
    }

    @Override
    public void DeleteEntry(@NonNull MeterLogItem entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);
            _databaseMeterLogItemList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(MeterLogItemDeleteFinishedBroadcast, MeterLogItemDeleteFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MeterLogItemDelete, true);
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

    private void saveMeterLogItemListToDatabase(@NonNull ArrayList<MeterLogItem> list) {
        for (int index = 0; index < list.size(); index++) {
            MeterLogItem entry = list.get(index);
            _databaseMeterLogItemList.AddEntry(entry);
        }
    }

    private ArrayList<MeterLogItem> notOnServerEntries() {
        try {
            return _databaseMeterLogItemList.GetList(String.format(Locale.getDefault(), "%s like %d", DatabaseMeterLogItemList.KeyIsOnServer, 0), null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerEntries().size() > 0;
    }
}
