package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

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
import guepardoapps.lucahome.common.converter.JsonDataToMapContentConverter;
import guepardoapps.lucahome.common.database.DatabaseMapContentList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.interfaces.services.IDataService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class MapContentService implements IDataService {
    public static class MapContentDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<MapContent> MapContentList;

        public MapContentDownloadFinishedContent(SerializableList<MapContent> mapContentList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            MapContentList = mapContentList;
        }
    }

    public static final String MapContentDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.mapcontent.download.finished";
    public static final String MapContentDownloadFinishedBundle = "MapContentDownloadFinishedBundle";

    private static final MapContentService SINGLETON = new MapContentService();
    private boolean _isInitialized;

    private static final String TAG = MapContentService.class.getSimpleName();
    private Logger _logger;

    private static final int MIN_TIMEOUT_MIN = 4 * 60;
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
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private DatabaseMapContentList _databaseMapContentList;

    private ScheduleService _scheduleService;
    private TemperatureService _temperatureService;
    private WirelessSocketService _wirelessSocketService;

    private JsonDataToMapContentConverter _jsonDataToMapContentConverter;

    private SerializableList<MapContent> _mapContentList = new SerializableList<>();

    private BroadcastReceiver _mapContentDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_mapContentDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MapContent) {
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

            SerializableList<MapContent> mapContentList = _jsonDataToMapContentConverter.GetList(
                    contentResponse,
                    _temperatureService.GetDataList(),
                    _wirelessSocketService.GetDataList(),
                    _scheduleService.GetDataList());
            if (mapContentList == null) {
                _logger.Error("Converted mapContentList is null!");
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _mapContentList = mapContentList;

            clearMapContentListFromDatabase();
            saveMapContentListToDatabase();

            _broadcastController.SendSerializableBroadcast(
                    MapContentDownloadFinishedBroadcast,
                    MapContentDownloadFinishedBundle,
                    new MapContentDownloadFinishedContent(_mapContentList, true, content.Response));
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

    private MapContentService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static MapContentService getInstance() {
        return SINGLETON;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _reloadEnabled = reloadEnabled;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _databaseMapContentList = new DatabaseMapContentList(context);
        _databaseMapContentList.Open();

        _scheduleService = ScheduleService.getInstance();
        _temperatureService = TemperatureService.getInstance();
        _wirelessSocketService = WirelessSocketService.getInstance();

        _receiverController.RegisterReceiver(_mapContentDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        _jsonDataToMapContentConverter = new JsonDataToMapContentConverter();

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _logger.Debug("Dispose");
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
    public SerializableList<MapContent> SearchDataList(@NonNull String searchKey) {
        SerializableList<MapContent> foundMapContents = new SerializableList<>();

        for (int index = 0; index < _mapContentList.getSize(); index++) {
            MapContent entry = _mapContentList.getValue(index);

            if (entry.GetTemperatureArea().contains(searchKey)
                    || String.valueOf(entry.GetId()).contains(searchKey)
                    || entry.GetTemperature().toString().contains(searchKey)
                    || entry.GetDrawingType().toString().contains(searchKey)
                    || entry.GetScheduleList().toString().contains(searchKey)
                    || entry.GetSocket().toString().contains(searchKey)) {
                foundMapContents.addValue(entry);
            }
        }

        return foundMapContents;
    }

    @Override
    public void LoadData() {
        _logger.Debug("LoadData");

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _mapContentList = _databaseMapContentList.GetMapContent(_wirelessSocketService.GetDataList(), _temperatureService.GetDataList());
            _broadcastController.SendSerializableBroadcast(
                    MapContentDownloadFinishedBroadcast,
                    MapContentDownloadFinishedBundle,
                    new MapContentDownloadFinishedContent(_mapContentList, true, Tools.CompressStringToByteArray("Loaded from database!")));
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
                + "&action=" + LucaServerAction.GET_MAP_CONTENTS.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

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

    private void clearMapContentListFromDatabase() {
        _logger.Debug("clearMapContentListFromDatabase");

        SerializableList<MapContent> mapContentList = _databaseMapContentList.GetMapContent(null, null);
        for (int index = 0; index < mapContentList.getSize(); index++) {
            MapContent mapContent = mapContentList.getValue(index);
            _databaseMapContentList.Delete(mapContent);
        }
    }

    private void saveMapContentListToDatabase() {
        _logger.Debug("saveMapContentListToDatabase");

        for (int index = 0; index < _mapContentList.getSize(); index++) {
            MapContent mapContent = _mapContentList.getValue(index);
            _databaseMapContentList.CreateEntry(mapContent);
        }
    }

    private void sendFailedDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                MapContentDownloadFinishedBroadcast,
                MapContentDownloadFinishedBundle,
                new MapContentDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
    }
}
