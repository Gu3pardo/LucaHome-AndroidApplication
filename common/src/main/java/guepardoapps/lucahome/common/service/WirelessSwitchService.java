package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToWirelessSwitchConverter;
import guepardoapps.lucahome.common.database.DatabaseWirelessSwitchList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.interfaces.services.IDataNotificationService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class WirelessSwitchService implements IDataNotificationService {
    public static class WirelessSwitchDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<WirelessSwitch> WirelessSwitchList;

        WirelessSwitchDownloadFinishedContent(SerializableList<WirelessSwitch> wirelessSwitchList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            WirelessSwitchList = wirelessSwitchList;
        }
    }

    public static final int NOTIFICATION_ID = 10752177;

    public static final String WirelessSwitchIntent = "WirelessSwitchIntent";

    public static final String WirelessSwitchDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelessswitch.download.finished";
    public static final String WirelessSwitchDownloadFinishedBundle = "WirelessSwitchDownloadFinishedBundle";

    public static final String WirelessSwitchToggleFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelessswitch.toggle.finished";
    public static final String WirelessSwitchToggleFinishedBundle = "WirelessSwitchToggleFinishedBundle";

    public static final String WirelessSwitchAddFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelessswitch.add.finished";
    public static final String WirelessSwitchAddFinishedBundle = "WirelessSwitchAddFinishedBundle";

    public static final String WirelessSwitchUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelessswitch.update.finished";
    public static final String WirelessSwitchUpdateFinishedBundle = "WirelessSwitchUpdateFinishedBundle";

    public static final String WirelessSwitchDeleteFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelessswitch.delete.finished";
    public static final String WirelessSwitchDeleteFinishedBundle = "WirelessSwitchDeleteFinishedBundle";

    public static final String WirelessSwitchGetFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelessswitch.get.finished";
    public static final String WirelessSwitchGetFinishedBundle = "WirelessSwitchGetFinishedBundle";

    private static final WirelessSwitchService SINGLETON = new WirelessSwitchService();
    private boolean _isInitialized;

    private static final String TAG = WirelessSwitchService.class.getSimpleName();
    private Logger _logger;

    private boolean _loadDataEnabled;

    private Date _lastUpdate;

    private boolean _displayNotification;
    private Class<?> _receiverClass;

    private static final int MIN_TIMEOUT_MIN = 15;
    private static final int MAX_TIMEOUT_MIN = 24 * 60;

    private boolean _reloadEnabled;
    private int _reloadTimeout;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_reloadListRunnable run");

            LoadData();

            if (_reloadEnabled && _networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private NotificationController _notificationController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private DatabaseWirelessSwitchList _databaseWirelessSwitchList;

    private JsonDataToWirelessSwitchConverter _jsonDataToWirelessSwitchConverter;

    private SerializableList<WirelessSwitch> _wirelessSwitchList = new SerializableList<>();

    private BroadcastReceiver _wirelessSwitchDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSwitchDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSwitch) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                _wirelessSwitchList = _databaseWirelessSwitchList.GetWirelessSwitchList();
                sendFailedWirelessSwitchDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                _wirelessSwitchList = _databaseWirelessSwitchList.GetWirelessSwitchList();
                sendFailedWirelessSwitchDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<WirelessSwitch> wirelessSwitchList = _jsonDataToWirelessSwitchConverter.GetList(contentResponse);
            if (wirelessSwitchList == null) {
                _logger.Error("Converted wirelessSwitchList is null!");
                _wirelessSwitchList = _databaseWirelessSwitchList.GetWirelessSwitchList();
                sendFailedWirelessSwitchDownloadBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _wirelessSwitchList = wirelessSwitchList;
            ShowNotification();

            clearSwitchListFromDatabase();
            saveSwitchListToDatabase();

            _broadcastController.SendSerializableBroadcast(
                    WirelessSwitchDownloadFinishedBroadcast,
                    WirelessSwitchDownloadFinishedBundle,
                    new WirelessSwitchDownloadFinishedContent(_wirelessSwitchList, true, content.Response));
        }
    };

    private BroadcastReceiver _wirelessSwitchToggleFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSwitchToggleFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSwitchToggle) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedWirelessSwitchToggleBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedWirelessSwitchToggleBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    WirelessSwitchToggleFinishedBroadcast,
                    WirelessSwitchToggleFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _wirelessSwitchAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSwitchAddFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSwitchAdd) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedWirelessSwitchAddBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedWirelessSwitchAddBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    WirelessSwitchAddFinishedBroadcast,
                    WirelessSwitchAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _wirelessSwitchUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSwitchUpdateFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSwitchUpdate) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedWirelessSwitchUpdateBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedWirelessSwitchUpdateBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    WirelessSwitchUpdateFinishedBroadcast,
                    WirelessSwitchUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _wirelessSwitchDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSwitchDeleteFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSwitchDelete) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedWirelessSwitchDeleteBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedWirelessSwitchDeleteBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    WirelessSwitchDeleteFinishedBroadcast,
                    WirelessSwitchDeleteFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _homeNetworkAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_homeNetworkAvailableReceiver onReceive");
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            if (_reloadEnabled && _networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private BroadcastReceiver _homeNetworkNotAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_homeNetworkNotAvailableReceiver onReceive");
            _reloadHandler.removeCallbacks(_reloadListRunnable);
        }
    };

    private WirelessSwitchService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static WirelessSwitchService getInstance() {
        return SINGLETON;
    }

    @Override
    public void Initialize(@NonNull Context context, @NonNull Class<?> receiverClass, boolean displayNotification, boolean reloadEnabled, int reloadTimeout) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _lastUpdate = new Date();

        _loadDataEnabled = true;

        _receiverClass = receiverClass;
        _displayNotification = displayNotification;
        _reloadEnabled = reloadEnabled;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _notificationController = new NotificationController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _databaseWirelessSwitchList = new DatabaseWirelessSwitchList(context);
        _databaseWirelessSwitchList.Open();

        _receiverController.RegisterReceiver(_wirelessSwitchDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSwitchToggleFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSwitchAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSwitchUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSwitchDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        _jsonDataToWirelessSwitchConverter = new JsonDataToWirelessSwitchConverter();

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseWirelessSwitchList.Close();
        _isInitialized = false;
    }

    @Override
    public SerializableList<WirelessSwitch> GetDataList() {
        return _wirelessSwitchList;
    }

    public ArrayList<String> GetNameList() {
        ArrayList<String> nameList = new ArrayList<>();

        for (int index = 0; index < _wirelessSwitchList.getSize(); index++) {
            nameList.add(_wirelessSwitchList.getValue(index).GetName());
        }

        return nameList;
    }

    public ArrayList<String> GetAreaList() {
        ArrayList<String> areaList = new ArrayList<>();

        for (int index = 0; index < _wirelessSwitchList.getSize(); index++) {
            String area = _wirelessSwitchList.getValue(index).GetArea();
            if (!areaList.contains(area)) {
                areaList.add(area);
            }
        }

        return areaList;
    }

    public ArrayList<String> GetRemoteIdList() {
        ArrayList<String> remoteIdList = new ArrayList<>();

        for (int index = 0; index < _wirelessSwitchList.getSize(); index++) {
            String remoteId = String.valueOf(_wirelessSwitchList.getValue(index).GetRemoteId());
            if (!remoteIdList.contains(remoteId)) {
                remoteIdList.add(remoteId);
            }
        }

        return remoteIdList;
    }

    public ArrayList<String> GetKeyCodeList() {
        ArrayList<String> keyCodeList = new ArrayList<>();

        for (int index = 0; index < _wirelessSwitchList.getSize(); index++) {
            String keyCode = String.valueOf(_wirelessSwitchList.getValue(index).GetKeyCode());
            if (!keyCodeList.contains(keyCode)) {
                keyCodeList.add(keyCode);
            }
        }

        return keyCodeList;
    }

    public WirelessSwitch GetSocketById(int id) {
        for (int index = 0; index < _wirelessSwitchList.getSize(); index++) {
            WirelessSwitch entry = _wirelessSwitchList.getValue(index);

            if (entry.GetId() == id) {
                return entry;
            }
        }

        return null;
    }

    public WirelessSwitch GetWirelessSwitchByName(@NonNull String name) {
        for (int index = 0; index < _wirelessSwitchList.getSize(); index++) {
            WirelessSwitch entry = _wirelessSwitchList.getValue(index);

            if (entry.GetName().contains(name)) {
                return entry;
            }
        }

        return null;
    }

    @Override
    public SerializableList<WirelessSwitch> SearchDataList(@NonNull String searchKey) {
        SerializableList<WirelessSwitch> foundWirelessSwitches = new SerializableList<>();

        for (int index = 0; index < _wirelessSwitchList.getSize(); index++) {
            WirelessSwitch entry = _wirelessSwitchList.getValue(index);

            if (String.valueOf(entry.GetId()).contains(searchKey)
                    || entry.GetName().contains(searchKey)
                    || entry.GetShortName().contains(searchKey)
                    || entry.GetArea().contains(searchKey)
                    || String.valueOf(entry.GetRemoteId()).contains(searchKey)
                    || String.valueOf(entry.GetKeyCode()).contains(searchKey)
                    || entry.GetActivationString().contains(searchKey)
                    || String.valueOf(entry.IsActivated()).contains(searchKey)) {
                foundWirelessSwitches.addValue(entry);
            }
        }

        return foundWirelessSwitches;
    }

    @Override
    public void LoadData() {
        _logger.Debug("LoadData");

        if (!_loadDataEnabled) {
            _logger.Debug("_loadDataEnabled is false!");
            return;
        }

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _wirelessSwitchList = _databaseWirelessSwitchList.GetWirelessSwitchList();
            _broadcastController.SendSerializableBroadcast(
                    WirelessSwitchDownloadFinishedBroadcast,
                    WirelessSwitchDownloadFinishedBundle,
                    new WirelessSwitchDownloadFinishedContent(_wirelessSwitchList, true, Tools.CompressStringToByteArray("Loaded from database!")));
            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedWirelessSwitchDownloadBroadcast("No user");
            return;
        }

        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            for (int index = 0; index < notOnServerWirelessSwitches().getSize(); index++) {
                WirelessSwitch wirelessSwitch = notOnServerWirelessSwitches().getValue(index);

                switch (wirelessSwitch.GetServerDbAction()) {
                    case Add:
                        AddWirelessSwitch(wirelessSwitch);
                        break;
                    case Update:
                        UpdateWirelessSwitch(wirelessSwitch);
                        break;
                    case Delete:
                        DeleteWirelessSwitch(wirelessSwitch);
                        break;
                    case Null:
                    default:
                        _logger.Debug(String.format(Locale.getDefault(), "Nothing todo with %s.", wirelessSwitch));
                        break;
                }

            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_SWITCHES.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSwitch, true);
    }

    public void ToggleWirelessSwitch(@NonNull WirelessSwitch entry) throws Exception {
        _logger.Debug(String.format(Locale.getDefault(), "ToggleWirelessSwitch: Toggle entry %s", entry));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedWirelessSwitchToggleBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandToggle());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSwitchToggle, true);
    }

    public void ToggleWirelessSwitch(@NonNull String wirelessSwitchName) {
        _logger.Debug(String.format(Locale.getDefault(), "ToggleWirelessSwitch: Toggle entry %s", wirelessSwitchName));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedWirelessSwitchToggleBroadcast("No user");
            return;
        }

        WirelessSwitch wirelessSwitch = foundWirelessSwitchByName(wirelessSwitchName);

        if (wirelessSwitch == null) {
            String errorMessage = String.format(Locale.getDefault(), "WirelessSwitch is null! Name %s not found!", wirelessSwitchName);
            _logger.Error(errorMessage);
            sendFailedWirelessSwitchToggleBroadcast(errorMessage);
            return;
        }

        try {
            ToggleWirelessSwitch(wirelessSwitch);
        } catch (Exception exception) {
            _logger.Error(exception.getMessage());
        }
    }

    public void ToggleAllWirelessSwitches() {
        _logger.Debug("ToggleAllWirelessSwitches");

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedWirelessSwitchToggleBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                LucaServerAction.TOGGLE_ALL_SWITCHES);

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSwitchToggle, true);
    }

    public void AddWirelessSwitch(@NonNull WirelessSwitch entry) {
        _logger.Debug(String.format(Locale.getDefault(), "AddWirelessSwitch: Adding new entry %s", entry));

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);

            _databaseWirelessSwitchList.CreateEntry(entry);

            LoadData();

            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedWirelessSwitchAddBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSwitchAdd, true);
    }

    public void UpdateWirelessSwitch(@NonNull WirelessSwitch entry) {
        _logger.Debug(String.format(Locale.getDefault(), "UpdateWirelessSwitch: Updating entry %s", entry));

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);

            _databaseWirelessSwitchList.Update(entry);

            LoadData();

            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedWirelessSwitchUpdateBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSwitchUpdate, true);
    }

    public void DeleteWirelessSwitch(@NonNull WirelessSwitch entry) {
        _logger.Debug(String.format(Locale.getDefault(), "DeleteWirelessSwitch: Deleting entry %s", entry));

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);

            _databaseWirelessSwitchList.Update(entry);

            LoadData();

            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedWirelessSwitchDeleteBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSwitchDelete, true);
    }

    public boolean GetWirelessSwitchAction(@NonNull String wirelessSwitchName) {
        _logger.Debug(String.format(Locale.getDefault(), "GetWirelessSwitchAction: Action of wirelessSwitch %s", wirelessSwitchName));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedWirelessSwitchGetBroadcast("No user");
            return false;
        }

        WirelessSwitch wirelessSwitch = foundWirelessSwitchByName(wirelessSwitchName);

        if (wirelessSwitch == null) {
            String errorMessage = String.format(Locale.getDefault(), "WirelessSwitch is null! Name %s not found!", wirelessSwitchName);
            _logger.Error(errorMessage);
            sendFailedWirelessSwitchGetBroadcast(errorMessage);
            return false;
        }

        return wirelessSwitch.GetAction();
    }

    @Override
    public void ShowNotification() {
        _logger.Debug("ShowNotification");
        if (!_displayNotification) {
            _logger.Warning("_displayNotification is false!");
            return;
        }
        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _logger.Warning("No home network!");
            return;
        }
        _notificationController.CreateWirelessSwitchNotification(NOTIFICATION_ID, _wirelessSwitchList, _receiverClass);
    }

    @Override
    public void CloseNotification() {
        _logger.Debug("CloseNotification");
        _notificationController.CloseNotification(NOTIFICATION_ID);
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
    public void SetReceiverActivity(@NonNull Class<?> receiverActivity) {
        _receiverClass = receiverActivity;
    }

    @Override
    public Class<?> GetReceiverActivity() {
        return _receiverClass;
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
            _logger.Warning(String.format(Locale.getDefault(), "reloadTimeout %d is lower then MIN_TIMEOUT_MIN %d! Setting to MIN_TIMEOUT_MIN!", reloadTimeout, MIN_TIMEOUT_MIN));
            reloadTimeout = MIN_TIMEOUT_MIN;
        }
        if (reloadTimeout > MAX_TIMEOUT_MIN) {
            _logger.Warning(String.format(Locale.getDefault(), "reloadTimeout %d is higher then MAX_TIMEOUT_MIN %d! Setting to MAX_TIMEOUT_MIN!", reloadTimeout, MAX_TIMEOUT_MIN));
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

    private void clearSwitchListFromDatabase() {
        _logger.Debug("clearSwitchListFromDatabase");

        SerializableList<WirelessSwitch> wirelessSwitchList = _databaseWirelessSwitchList.GetWirelessSwitchList();
        for (int index = 0; index < wirelessSwitchList.getSize(); index++) {
            WirelessSwitch wirelessSwitch = wirelessSwitchList.getValue(index);
            _databaseWirelessSwitchList.Delete(wirelessSwitch);
        }
    }

    private void saveSwitchListToDatabase() {
        _logger.Debug("saveSwitchListToDatabase");

        for (int index = 0; index < _wirelessSwitchList.getSize(); index++) {
            WirelessSwitch wirelessSwitch = _wirelessSwitchList.getValue(index);
            _databaseWirelessSwitchList.CreateEntry(wirelessSwitch);
        }
    }

    private void sendFailedWirelessSwitchDownloadBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Download for wireless switches failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                WirelessSwitchDownloadFinishedBroadcast,
                WirelessSwitchDownloadFinishedBundle,
                new WirelessSwitchDownloadFinishedContent(_wirelessSwitchList, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedWirelessSwitchToggleBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Toggle for wireless switch failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                WirelessSwitchToggleFinishedBroadcast,
                WirelessSwitchToggleFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedWirelessSwitchAddBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Add for wireless switch failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                WirelessSwitchAddFinishedBroadcast,
                WirelessSwitchAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedWirelessSwitchUpdateBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Update for wireless switch failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                WirelessSwitchUpdateFinishedBroadcast,
                WirelessSwitchUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedWirelessSwitchDeleteBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Delete for wireless switch failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                WirelessSwitchDeleteFinishedBroadcast,
                WirelessSwitchDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedWirelessSwitchGetBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Get for wireless switch failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                WirelessSwitchGetFinishedBroadcast,
                WirelessSwitchGetFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private WirelessSwitch foundWirelessSwitchByName(@NonNull String wirelessSwitchName) {
        for (int index = 0; index < _wirelessSwitchList.getSize(); index++) {
            WirelessSwitch currentWirelessSwitch = _wirelessSwitchList.getValue(index);
            if (currentWirelessSwitch != null) {
                String currentWirelessSocketName = currentWirelessSwitch.GetName();
                if (currentWirelessSocketName.contentEquals(wirelessSwitchName)) {
                    return currentWirelessSwitch;
                }
            }
        }
        return null;
    }

    private SerializableList<WirelessSwitch> notOnServerWirelessSwitches() {
        SerializableList<WirelessSwitch> notOnServerWirelessSwitchList = new SerializableList<>();

        for (int index = 0; index < _wirelessSwitchList.getSize(); index++) {
            if (!_wirelessSwitchList.getValue(index).GetIsOnServer()) {
                notOnServerWirelessSwitchList.addValue(_wirelessSwitchList.getValue(index));
            }
        }

        return notOnServerWirelessSwitchList;
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerWirelessSwitches().getSize() > 0;
    }
}
