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
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.converter.JsonDataToSocketConverter;
import guepardoapps.lucahome.common.database.DatabaseWirelessSocketList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.interfaces.services.IDataNotificationService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class WirelessSocketService implements IDataNotificationService {
    public static class WirelessSocketDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<WirelessSocket> WirelessSocketList;

        WirelessSocketDownloadFinishedContent(SerializableList<WirelessSocket> wirelessSocketList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            WirelessSocketList = wirelessSocketList;
        }
    }

    public static final int NOTIFICATION_ID = 211990;

    public static final String WirelessSocketIntent = "WirelessSocketIntent";

    public static final String WirelessSocketDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelesssocket.download.finished";
    public static final String WirelessSocketDownloadFinishedBundle = "WirelessSocketDownloadFinishedBundle";

    public static final String WirelessSocketSetFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelesssocket.set.finished";
    public static final String WirelessSocketSetFinishedBundle = "WirelessSocketSetFinishedBundle";

    public static final String WirelessSocketAddFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelesssocket.add.finished";
    public static final String WirelessSocketAddFinishedBundle = "WirelessSocketAddFinishedBundle";

    public static final String WirelessSocketUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelesssocket.update.finished";
    public static final String WirelessSocketUpdateFinishedBundle = "WirelessSocketUpdateFinishedBundle";

    public static final String WirelessSocketDeleteFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelesssocket.delete.finished";
    public static final String WirelessSocketDeleteFinishedBundle = "WirelessSocketDeleteFinishedBundle";

    public static final String WirelessSocketGetFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelesssocket.get.finished";
    public static final String WirelessSocketGetFinishedBundle = "WirelessSocketGetFinishedBundle";

    private static final WirelessSocketService SINGLETON = new WirelessSocketService();
    private boolean _isInitialized;

    private static final String TAG = WirelessSocketService.class.getSimpleName();
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

    private DatabaseWirelessSocketList _databaseWirelessSocketList;

    private JsonDataToSocketConverter _jsonDataToWirelessSocketConverter;

    private SerializableList<WirelessSocket> _wirelessSocketList = new SerializableList<>();

    private BroadcastReceiver _wirelessSocketDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSocketDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSocket) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                _wirelessSocketList = _databaseWirelessSocketList.GetWirelessSocketList();
                sendFailedSocketDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                _wirelessSocketList = _databaseWirelessSocketList.GetWirelessSocketList();
                sendFailedSocketDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<WirelessSocket> wirelessSocketList = _jsonDataToWirelessSocketConverter.GetList(contentResponse);
            if (wirelessSocketList == null) {
                _logger.Error("Converted wirelessSocketList is null!");
                _wirelessSocketList = _databaseWirelessSocketList.GetWirelessSocketList();
                sendFailedSocketDownloadBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _wirelessSocketList = wirelessSocketList;
            ShowNotification();

            clearSocketListFromDatabase();
            saveSocketListToDatabase();

            _broadcastController.SendSerializableBroadcast(
                    WirelessSocketDownloadFinishedBroadcast,
                    WirelessSocketDownloadFinishedBundle,
                    new WirelessSocketDownloadFinishedContent(_wirelessSocketList, true, content.Response));
        }
    };

    private BroadcastReceiver _wirelessSocketSetFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSocketSetFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSocketSet) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedSocketSetBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedSocketSetBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    WirelessSocketSetFinishedBroadcast,
                    WirelessSocketSetFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _wirelessSocketAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSocketAddFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSocketAdd) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedSocketAddBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedSocketAddBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    WirelessSocketAddFinishedBroadcast,
                    WirelessSocketAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _wirelessSocketUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSocketUpdateFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSocketUpdate) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedSocketUpdateBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedSocketUpdateBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    WirelessSocketUpdateFinishedBroadcast,
                    WirelessSocketUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _wirelessSocketDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSocketDeleteFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSocketDelete) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedSocketDeleteBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedSocketDeleteBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    WirelessSocketDeleteFinishedBroadcast,
                    WirelessSocketDeleteFinishedBundle,
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

    private WirelessSocketService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static WirelessSocketService getInstance() {
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

        _databaseWirelessSocketList = new DatabaseWirelessSocketList(context);
        _databaseWirelessSocketList.Open();

        _receiverController.RegisterReceiver(_wirelessSocketDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSocketSetFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSocketAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSocketUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSocketDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        _jsonDataToWirelessSocketConverter = new JsonDataToSocketConverter();

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseWirelessSocketList.Close();
        _isInitialized = false;
    }

    @Override
    public SerializableList<WirelessSocket> GetDataList() {
        return _wirelessSocketList;
    }

    public ArrayList<String> GetNameList() {
        ArrayList<String> nameList = new ArrayList<>();

        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            nameList.add(_wirelessSocketList.getValue(index).GetName());
        }

        return nameList;
    }

    public ArrayList<String> GetAreaList() {
        ArrayList<String> areaList = new ArrayList<>();

        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            String area = _wirelessSocketList.getValue(index).GetArea();
            if (!areaList.contains(area)) {
                areaList.add(area);
            }
        }

        return areaList;
    }

    public ArrayList<String> GetCodeList() {
        ArrayList<String> codeList = new ArrayList<>();

        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            codeList.add(_wirelessSocketList.getValue(index).GetCode());
        }

        return codeList;
    }

    public WirelessSocket GetSocketById(int id) {
        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            WirelessSocket entry = _wirelessSocketList.getValue(index);

            if (entry.GetId() == id) {
                return entry;
            }
        }

        return null;
    }

    public WirelessSocket GetSocketByName(@NonNull String name) {
        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            WirelessSocket entry = _wirelessSocketList.getValue(index);

            if (entry.GetName().contains(name)) {
                return entry;
            }
        }

        return null;
    }

    @Override
    public SerializableList<WirelessSocket> SearchDataList(@NonNull String searchKey) {
        SerializableList<WirelessSocket> foundWirelessSockets = new SerializableList<>();

        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            WirelessSocket entry = _wirelessSocketList.getValue(index);

            if (String.valueOf(entry.GetId()).contains(searchKey)
                    || entry.GetName().contains(searchKey)
                    || entry.GetShortName().contains(searchKey)
                    || entry.GetArea().contains(searchKey)
                    || entry.GetCode().contains(searchKey)
                    || entry.GetActivationString().contains(searchKey)
                    || String.valueOf(entry.IsActivated()).contains(searchKey)) {
                foundWirelessSockets.addValue(entry);
            }
        }

        return foundWirelessSockets;
    }

    @Override
    public void LoadData() {
        _logger.Debug("LoadData");

        if (!_loadDataEnabled) {
            _logger.Debug("_loadDataEnabled is false!");
            return;
        }

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _wirelessSocketList = _databaseWirelessSocketList.GetWirelessSocketList();
            _broadcastController.SendSerializableBroadcast(
                    WirelessSocketDownloadFinishedBroadcast,
                    WirelessSocketDownloadFinishedBundle,
                    new WirelessSocketDownloadFinishedContent(_wirelessSocketList, true, Tools.CompressStringToByteArray("Loaded from database!")));
            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketDownloadBroadcast("No user");
            return;
        }

        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            for (int index = 0; index < notOnServerWirelessSockets().getSize(); index++) {
                WirelessSocket wirelessSocket = notOnServerWirelessSockets().getValue(index);

                switch (wirelessSocket.GetServerDbAction()) {
                    case Add:
                        AddWirelessSocket(wirelessSocket);
                        break;
                    case Update:
                        UpdateWirelessSocket(wirelessSocket);
                        break;
                    case Delete:
                        DeleteWirelessSocket(wirelessSocket);
                        break;
                    case Null:
                    default:
                        _logger.Debug(String.format(Locale.getDefault(), "Nothing todo with %s.", wirelessSocket));
                        break;
                }

            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_SOCKETS.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocket, true);
    }

    public void ChangeWirelessSocketState(@NonNull WirelessSocket entry) throws Exception {
        _logger.Debug(String.format(Locale.getDefault(), "ChangeWirelessSocketState: Changing state of entry %s", entry));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketSetBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandChangeState());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocketSet, true);
    }

    public void SetWirelessSocketState(@NonNull WirelessSocket entry, boolean newState) throws Exception {
        _logger.Debug(String.format(Locale.getDefault(), "SetWirelessSocketState: Setting state of entry %s to %s", entry, newState));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketSetBroadcast("No user");
            return;
        }

        entry.SetActivated(newState);
        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandSetState());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocketSet, true);
    }

    public void SetWirelessSocketState(@NonNull String socketName, boolean newState) {
        _logger.Debug(String.format(Locale.getDefault(), "SetWirelessSocketState: Setting state of socket %s to %s", socketName, newState));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketSetBroadcast("No user");
            return;
        }

        WirelessSocket wirelessSocket = foundWirelessSocketByName(socketName);

        if (wirelessSocket == null) {
            String errorMessage = String.format(Locale.getDefault(), "WirelessSocket is null! Name %s not found!", socketName);
            _logger.Error(errorMessage);
            sendFailedSocketSetBroadcast(errorMessage);
            return;
        }

        try {
            SetWirelessSocketState(wirelessSocket, newState);
        } catch (Exception exception) {
            _logger.Error(exception.getMessage());
        }
    }

    public void DeactivateAllWirelessSockets() {
        _logger.Debug("DeactivateAllWirelessSockets");

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketSetBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                LucaServerAction.DEACTIVATE_ALL_SOCKETS);

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocketSet, true);
    }

    public void AddWirelessSocket(@NonNull WirelessSocket entry) {
        _logger.Debug(String.format(Locale.getDefault(), "AddWirelessSocket: Adding new entry %s", entry));

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);

            _databaseWirelessSocketList.CreateEntry(entry);

            LoadData();

            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketAddBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocketAdd, true);
    }

    public void UpdateWirelessSocket(@NonNull WirelessSocket entry) {
        _logger.Debug(String.format(Locale.getDefault(), "UpdateWirelessSocket: Updating entry %s", entry));

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);

            _databaseWirelessSocketList.Update(entry);

            LoadData();

            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketUpdateBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocketUpdate, true);
    }

    public void DeleteWirelessSocket(@NonNull WirelessSocket entry) {
        _logger.Debug(String.format(Locale.getDefault(), "DeleteWirelessSocket: Deleting entry %s", entry));

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);

            _databaseWirelessSocketList.Update(entry);

            LoadData();

            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketDeleteBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocketDelete, true);
    }

    public boolean GetWirelessSocketState(@NonNull String socketName) {
        _logger.Debug(String.format(Locale.getDefault(), "GetWirelessSocketState: State of socket %s", socketName));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketGetBroadcast("No user");
            return false;
        }

        WirelessSocket wirelessSocket = foundWirelessSocketByName(socketName);

        if (wirelessSocket == null) {
            String errorMessage = String.format(Locale.getDefault(), "WirelessSocket is null! Name %s not found!", socketName);
            _logger.Error(errorMessage);
            sendFailedSocketGetBroadcast(errorMessage);
            return false;
        }

        return wirelessSocket.IsActivated();
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
        _notificationController.CreateSocketNotification(NOTIFICATION_ID, _wirelessSocketList, _receiverClass);
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

    private void clearSocketListFromDatabase() {
        _logger.Debug("clearSocketListFromDatabase");

        SerializableList<WirelessSocket> socketList = _databaseWirelessSocketList.GetSocketList();
        for (int index = 0; index < socketList.getSize(); index++) {
            WirelessSocket socket = socketList.getValue(index);
            _databaseWirelessSocketList.Delete(socket);
        }
    }

    private void saveSocketListToDatabase() {
        _logger.Debug("saveSocketListToDatabase");

        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            WirelessSocket socket = _wirelessSocketList.getValue(index);
            _databaseWirelessSocketList.CreateEntry(socket);
        }
    }

    private void sendFailedSocketDownloadBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Download for sockets failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                WirelessSocketDownloadFinishedBroadcast,
                WirelessSocketDownloadFinishedBundle,
                new WirelessSocketDownloadFinishedContent(_wirelessSocketList, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedSocketSetBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Set for socket failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                WirelessSocketSetFinishedBroadcast,
                WirelessSocketSetFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedSocketAddBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Add for socket failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                WirelessSocketAddFinishedBroadcast,
                WirelessSocketAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedSocketUpdateBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Update for socket failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                WirelessSocketUpdateFinishedBroadcast,
                WirelessSocketUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedSocketDeleteBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Delete for socket failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                WirelessSocketDeleteFinishedBroadcast,
                WirelessSocketDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedSocketGetBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Get for sockets failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                WirelessSocketGetFinishedBroadcast,
                WirelessSocketGetFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private WirelessSocket foundWirelessSocketByName(@NonNull String wirelessSocketName) {
        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            WirelessSocket currentWirelessSocket = _wirelessSocketList.getValue(index);
            if (currentWirelessSocket != null) {
                String currentWirelessSocketName = currentWirelessSocket.GetName();
                if (currentWirelessSocketName.contentEquals(wirelessSocketName)) {
                    return currentWirelessSocket;
                }
            }
        }
        return null;
    }

    private SerializableList<WirelessSocket> notOnServerWirelessSockets() {
        SerializableList<WirelessSocket> notOnServerWirelessSocketList = new SerializableList<>();

        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            if (!_wirelessSocketList.getValue(index).GetIsOnServer()) {
                notOnServerWirelessSocketList.addValue(_wirelessSocketList.getValue(index));
            }
        }

        return notOnServerWirelessSocketList;
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerWirelessSockets().getSize() > 0;
    }
}
