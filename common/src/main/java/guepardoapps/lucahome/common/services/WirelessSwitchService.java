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
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToWirelessSwitchConverter;
import guepardoapps.lucahome.common.databases.DatabaseWirelessSwitchList;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"WeakerAccess"})
public class WirelessSwitchService implements IWirelessSwitchService {
    private static final String Tag = WirelessSwitchService.class.getSimpleName();

    private static final WirelessSwitchService Singleton = new WirelessSwitchService();

    private static final int MinTimeoutMin = 5;
    private static final int MaxTimeoutMin = 24 * 60;

    private DatabaseWirelessSwitchList _databaseWirelessSwitchList;

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
                ArrayList<WirelessSwitch> wirelessSwitchList = JsonDataToWirelessSwitchConverter.getInstance().GetList(contentResponse);
                if (wirelessSwitchList == null) {
                    Logger.getInstance().Error(Tag, "Converted wirelessSwitchList is null!");
                    _broadcastController.SendBooleanBroadcast(WirelessSwitchDownloadFinishedBroadcast, WirelessSwitchDownloadFinishedBundle, false);
                    return "";
                }

                _databaseWirelessSwitchList.ClearDatabase();
                saveWirelessSwitchListToDatabase(wirelessSwitchList);

                _lastUpdate = Calendar.getInstance();
                _broadcastController.SendBooleanBroadcast(WirelessSwitchDownloadFinishedBroadcast, WirelessSwitchDownloadFinishedBundle, true);
            }
            return "Success";
        }
    }

    private BroadcastReceiver _wirelessSwitchDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSwitch) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(WirelessSwitchDownloadFinishedBroadcast, WirelessSwitchDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(WirelessSwitchDownloadFinishedBroadcast, WirelessSwitchDownloadFinishedBundle, false);
                return;
            }

            new AsyncConverterTask().execute(contentResponse);
        }
    };

    private BroadcastReceiver _wirelessSwitchAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSwitchAdd) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(WirelessSwitchAddFinishedBroadcast, WirelessSwitchAddFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Adding was not successful!");
                _broadcastController.SendBooleanBroadcast(WirelessSwitchAddFinishedBroadcast, WirelessSwitchAddFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(WirelessSwitchAddFinishedBroadcast, WirelessSwitchAddFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _wirelessSwitchUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSwitchUpdate) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(WirelessSwitchUpdateFinishedBroadcast, WirelessSwitchUpdateFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Update was not successful!");
                _broadcastController.SendBooleanBroadcast(WirelessSwitchUpdateFinishedBroadcast, WirelessSwitchUpdateFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(WirelessSwitchUpdateFinishedBroadcast, WirelessSwitchUpdateFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _wirelessSwitchDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSwitchDelete) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(WirelessSwitchDeleteFinishedBroadcast, WirelessSwitchDeleteFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Deleting was not successful!");
                _broadcastController.SendBooleanBroadcast(WirelessSwitchDeleteFinishedBroadcast, WirelessSwitchDeleteFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(WirelessSwitchDeleteFinishedBroadcast, WirelessSwitchDeleteFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _wirelessSwitchToggleFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSwitchToggle) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(WirelessSwitchToggleFinishedBroadcast, WirelessSwitchToggleFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Toggle was not successful!");
                _broadcastController.SendBooleanBroadcast(WirelessSwitchToggleFinishedBroadcast, WirelessSwitchToggleFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(WirelessSwitchToggleFinishedBroadcast, WirelessSwitchToggleFinishedBundle, true);
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

    private WirelessSwitchService() {
    }

    public static WirelessSwitchService getInstance() {
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

        _displayNotification = displayNotification;
        _receiverActivity = receiverActivity;

        _reloadEnabled = reloadEnabled;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _notificationController = new NotificationController(context);
        _receiverController = new ReceiverController(context);

        _databaseWirelessSwitchList = new DatabaseWirelessSwitchList(context);
        _databaseWirelessSwitchList.Open();

        _receiverController.RegisterReceiver(_wirelessSwitchDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSwitchAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSwitchUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSwitchDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSwitchToggleFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseWirelessSwitchList.Close();
        _isInitialized = false;
    }

    @Override
    public ArrayList<WirelessSwitch> GetDataList() {
        try {
            return _databaseWirelessSwitchList.GetList(null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public WirelessSwitch GetByUuid(@NonNull UUID uuid) {
        try {
            return _databaseWirelessSwitchList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseWirelessSwitchList.KeyUuid, uuid), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new WirelessSwitch(uuid, UUID.randomUUID(), "NULL", -1, (char) -1, false, Calendar.getInstance(), "NULL", false, ILucaClass.LucaServerDbAction.Null);
        }
    }

    @Override
    public WirelessSwitch GetByName(@NonNull String wirelessSwitchName) {
        try {
            return _databaseWirelessSwitchList.GetList(String.format(Locale.getDefault(), "%s like %%%s%%", DatabaseWirelessSwitchList.KeyName, wirelessSwitchName), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new WirelessSwitch(UUID.randomUUID(), UUID.randomUUID(), wirelessSwitchName, -1, (char) -1, false, Calendar.getInstance(), "NULL", false, ILucaClass.LucaServerDbAction.Null);
        }
    }

    @Override
    public ArrayList<WirelessSwitch> SearchDataList(@NonNull String searchKey) {
        ArrayList<WirelessSwitch> list = GetDataList();
        ArrayList<WirelessSwitch> foundList = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            WirelessSwitch entry = list.get(index);
            if (entry.toString().contains(searchKey)) {
                foundList.add(entry);
            }
        }
        return foundList;
    }

    @Override
    public ArrayList<String> GetNameList() {
        try {
            return _databaseWirelessSwitchList.GetStringQueryList(true, DatabaseWirelessSwitchList.KeyName, null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<UUID> GetRoomUuidList() {
        try {
            ArrayList<String> roomUuidStringList = _databaseWirelessSwitchList.GetStringQueryList(true, DatabaseWirelessSwitchList.KeyUuid, null, null, null);
            ArrayList<UUID> roomUuidList = new ArrayList<>();
            for (String roomUuidString : roomUuidStringList) {
                roomUuidList.add(UUID.fromString(roomUuidString));
            }
            return roomUuidList;
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
            _broadcastController.SendBooleanBroadcast(WirelessSwitchDownloadFinishedBroadcast, WirelessSwitchDownloadFinishedBundle, true);
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(WirelessSwitchDownloadFinishedBroadcast, WirelessSwitchDownloadFinishedBundle, false);
            return;
        }

        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            ArrayList<WirelessSwitch> notOnServerWirelessSwitchList = notOnServerEntries();
            for (int index = 0; index < notOnServerWirelessSwitchList.size(); index++) {
                WirelessSwitch wirelessSwitch = notOnServerWirelessSwitchList.get(index);

                switch (wirelessSwitch.GetServerDbAction()) {
                    case Add:
                        AddEntry(wirelessSwitch);
                        break;
                    case Update:
                        UpdateEntry(wirelessSwitch);
                        break;
                    case Delete:
                        DeleteEntry(wirelessSwitch);
                        break;
                    case Null:
                    default:
                        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Nothing todo with %s.", wirelessSwitch));
                        break;
                }
            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_WIRELESS_SOCKETS.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSwitch, true);
    }

    @Override
    public void AddEntry(@NonNull WirelessSwitch entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);
            _databaseWirelessSwitchList.AddEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(WirelessSwitchAddFinishedBroadcast, WirelessSwitchAddFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSwitchAdd, true);
    }

    @Override
    public void UpdateEntry(@NonNull WirelessSwitch entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);
            _databaseWirelessSwitchList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(WirelessSwitchUpdateFinishedBroadcast, WirelessSwitchUpdateFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSwitchUpdate, true);
    }

    @Override
    public void DeleteEntry(@NonNull WirelessSwitch entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);
            _databaseWirelessSwitchList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(WirelessSwitchDeleteFinishedBroadcast, WirelessSwitchDeleteFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSwitchDelete, true);
    }

    @Override
    public void ToggleWirelessSwitch(WirelessSwitch entry) {
        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(WirelessSwitchToggleFinishedBroadcast, WirelessSwitchToggleFinishedBundle, false);
            return;
        }

        if (entry == null) {
            _broadcastController.SendBooleanBroadcast(WirelessSwitchToggleFinishedBroadcast, WirelessSwitchToggleFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandToggle());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSwitchToggle, true);
    }

    @Override
    public void ToggleAllWirelessSwitches() {
        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(WirelessSwitchToggleFinishedBroadcast, WirelessSwitchToggleFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                LucaServerActionTypes.TOGGLE_ALL_WIRELESS_SWITCHES);

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSwitchToggle, true);
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

        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            Logger.getInstance().Warning(Tag, "No home network!");
            return;
        }

        _notificationController.CreateWirelessSwitchNotification(NotificationId, GetDataList(), _receiverActivity);
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

    private void saveWirelessSwitchListToDatabase(@NonNull ArrayList<WirelessSwitch> wirelessSwitchList) {
        for (int index = 0; index < wirelessSwitchList.size(); index++) {
            WirelessSwitch wirelessSocket = wirelessSwitchList.get(index);
            _databaseWirelessSwitchList.AddEntry(wirelessSocket);
        }
    }

    private ArrayList<WirelessSwitch> notOnServerEntries() {
        try {
            return _databaseWirelessSwitchList.GetList(String.format(Locale.getDefault(), "%s like %d", DatabaseWirelessSwitchList.KeyIsOnServer, 0), null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerEntries().size() > 0;
    }
}
