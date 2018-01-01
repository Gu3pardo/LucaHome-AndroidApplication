package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.MapContent;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToMapContentConverter;
import guepardoapps.lucahome.common.database.DatabaseMapContentList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.services.IDataService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class MapContentService implements IDataService {
    public static class MapContentDownloadFinishedContent extends ObjectChangeFinishedContent {
        SerializableList<MapContent> MapContentList;

        MapContentDownloadFinishedContent(SerializableList<MapContent> mapContentList, boolean succcess) {
            super(succcess, new byte[]{});
            MapContentList = mapContentList;
        }
    }

    public static final String MapContentDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.mapcontent.download.finished";
    public static final String MapContentDownloadFinishedBundle = "MapContentDownloadFinishedBundle";

    private static final MapContentService SINGLETON = new MapContentService();
    private boolean _isInitialized;

    private static final String TAG = MapContentService.class.getSimpleName();

    private boolean _loadDataEnabled;

    private Date _lastUpdate;

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

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private DatabaseMapContentList _databaseMapContentList;

    private SerializableList<MapContent> _mapContentList = new SerializableList<>();

    private BroadcastReceiver _mapContentDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MapContent) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                _mapContentList = _databaseMapContentList.GetMapContent(
                        /* TODO add MediaServerData */
                        new SerializableList<>(),
                        TemperatureService.getInstance().GetDataList(),
                        WirelessSocketService.getInstance().GetDataList(),
                        WirelessSwitchService.getInstance().GetDataList());
                sendFailedDownloadBroadcast();
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                _mapContentList = _databaseMapContentList.GetMapContent(
                        /* TODO add MediaServerData */
                        new SerializableList<>(),
                        TemperatureService.getInstance().GetDataList(),
                        WirelessSocketService.getInstance().GetDataList(),
                        WirelessSwitchService.getInstance().GetDataList());
                sendFailedDownloadBroadcast();
                return;
            }

            SerializableList<MapContent> mapContentList = JsonDataToMapContentConverter.getInstance().GetList(
                    contentResponse,
                    ListedMenuService.getInstance().GetDataList(),
                    MenuService.getInstance().GetDataList(),
                    ShoppingListService.getInstance().GetDataList(),
                    /* TODO add MediaServerData */
                    new SerializableList<>(),
                    SecurityService.getInstance().GetDataList().getValue(0),
                    TemperatureService.getInstance().GetDataList(),
                    WirelessSocketService.getInstance().GetDataList(),
                    WirelessSwitchService.getInstance().GetDataList());
            if (mapContentList == null) {
                Logger.getInstance().Error(TAG, "Converted mapContentList is null!");
                sendFailedDownloadBroadcast();
                return;
            }

            _lastUpdate = new Date();

            _mapContentList = mapContentList;

            clearMapContentListFromDatabase();
            saveMapContentListToDatabase();

            _broadcastController.SendSerializableBroadcast(
                    MapContentDownloadFinishedBroadcast,
                    MapContentDownloadFinishedBundle,
                    new MapContentDownloadFinishedContent(_mapContentList, true));
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

    private MapContentService() {
    }

    public static MapContentService getInstance() {
        return SINGLETON;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout) {
        if (_isInitialized) {
            Logger.getInstance().Warning(TAG, "Already initialized!");
            return;
        }

        _lastUpdate = new Date();

        _reloadEnabled = reloadEnabled;
        _loadDataEnabled = true;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _databaseMapContentList = new DatabaseMapContentList(context);
        _databaseMapContentList.Open();

        _receiverController.RegisterReceiver(_mapContentDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseMapContentList.Close();
        _isInitialized = false;
    }

    @Override
    public SerializableList<MapContent> GetDataList() {
        return _mapContentList;
    }

    public MapContent GetById(int id) {
        for (int index = 0; index < _mapContentList.getSize(); index++) {
            MapContent entry = _mapContentList.getValue(index);
            if (entry.GetId() == id) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public int GetHighestId() {
        int highestId = -1;
        for (int index = 0; index < _mapContentList.getSize(); index++) {
            int id = _mapContentList.getValue(index).GetId();
            if (id > highestId) {
                highestId = id;
            }
        }
        return highestId;
    }

    @Override
    public SerializableList<MapContent> SearchDataList(@NonNull String searchKey) {
        SerializableList<MapContent> foundMapContents = new SerializableList<>();
        for (int index = 0; index < _mapContentList.getSize(); index++) {
            MapContent entry = _mapContentList.getValue(index);
            if (entry.toString().contains(searchKey)) {
                foundMapContents.addValue(entry);
            }
        }
        return foundMapContents;
    }

    @Override
    public void LoadData() {
        if (!_loadDataEnabled) {
            return;
        }

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _mapContentList = _databaseMapContentList.GetMapContent(
                        /* TODO add MediaServerData */
                    new SerializableList<>(),
                    TemperatureService.getInstance().GetDataList(),
                    WirelessSocketService.getInstance().GetDataList(),
                    WirelessSwitchService.getInstance().GetDataList());
            _broadcastController.SendSerializableBroadcast(
                    MapContentDownloadFinishedBroadcast,
                    MapContentDownloadFinishedBundle,
                    new MapContentDownloadFinishedContent(_mapContentList, true));
            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast();
            return;
        }

        if (hasMapContentEntryNotOnServer()) {
            _loadDataEnabled = false;

            for (int index = 0; index < notOnServerMapContent().getSize(); index++) {
                MapContent mapContent = notOnServerMapContent().getValue(index);

                switch (mapContent.GetServerDbAction()) {
                    case Update:
                    case Delete:
                    case Add:
                    case Null:
                    default:
                        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "Nothing todo with %s.", mapContent));
                        break;
                }
            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_MAP_CONTENTS.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MapContent, true);
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

    private void clearMapContentListFromDatabase() {
        SerializableList<MapContent> mapContentList = _databaseMapContentList.GetMapContent(
                /* TODO add MediaServerData */
                new SerializableList<>(),
                TemperatureService.getInstance().GetDataList(),
                WirelessSocketService.getInstance().GetDataList(),
                WirelessSwitchService.getInstance().GetDataList());
        for (int index = 0; index < mapContentList.getSize(); index++) {
            MapContent mapContent = mapContentList.getValue(index);
            _databaseMapContentList.Delete(mapContent);
        }
    }

    private void saveMapContentListToDatabase() {
        for (int index = 0; index < _mapContentList.getSize(); index++) {
            MapContent mapContent = _mapContentList.getValue(index);
            _databaseMapContentList.CreateEntry(mapContent);
        }
    }

    private void sendFailedDownloadBroadcast() {
        _broadcastController.SendSerializableBroadcast(
                MapContentDownloadFinishedBroadcast,
                MapContentDownloadFinishedBundle,
                new MapContentDownloadFinishedContent(_mapContentList, false));
    }

    private SerializableList<MapContent> notOnServerMapContent() {
        SerializableList<MapContent> notOnServerMapContentList = new SerializableList<>();

        for (int index = 0; index < _mapContentList.getSize(); index++) {
            if (!_mapContentList.getValue(index).GetIsOnServer()) {
                notOnServerMapContentList.addValue(_mapContentList.getValue(index));
            }
        }

        return notOnServerMapContentList;
    }

    private boolean hasMapContentEntryNotOnServer() {
        return notOnServerMapContent().getSize() > 0;
    }
}
