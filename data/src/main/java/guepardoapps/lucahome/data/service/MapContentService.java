package guepardoapps.lucahome.data.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.MapContent;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.converter.JsonDataToMapContentConverter;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.data.controller.DownloadController;
import guepardoapps.lucahome.data.controller.SettingsController;
import guepardoapps.lucahome.data.service.broadcasts.content.ObjectChangeFinishedContent;

public class MapContentService {
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

    private static final int TIMEOUT_MS = 5 * 60 * 1000;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_reloadListRunnable run");
            LoadMapContentList();
            _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);
        }
    };

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

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
                    _temperatureService.GetTemperatureList(),
                    _wirelessSocketService.GetWirelessSocketList(),
                    _scheduleService.GetScheduleList());
            if (mapContentList == null) {
                _logger.Error("Converted mapContentList is null!");
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _mapContentList = mapContentList;

            _broadcastController.SendSerializableBroadcast(
                    MapContentDownloadFinishedBroadcast,
                    MapContentDownloadFinishedBundle,
                    new MapContentDownloadFinishedContent(_mapContentList, true, content.Response));
        }
    };

    private MapContentService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static MapContentService getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _scheduleService = ScheduleService.getInstance();
        _temperatureService = TemperatureService.getInstance();
        _wirelessSocketService = WirelessSocketService.getInstance();

        _receiverController.RegisterReceiver(_mapContentDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _jsonDataToMapContentConverter = new JsonDataToMapContentConverter();

        _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);

        _isInitialized = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public SerializableList<MapContent> GetMapContentList() {
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

    public SerializableList<MapContent> FoundMapContents(@NonNull String searchKey) {
        SerializableList<MapContent> foundMapContents = new SerializableList<>();

        for (int index = 0; index < _mapContentList.getSize(); index++) {
            MapContent entry = _mapContentList.getValue(index);

            if (entry.GetTemperatureArea().contains(searchKey)
                    || String.valueOf(entry.GetId()).contains(searchKey)
                    || entry.GetTemperature().toString().contains(searchKey)
                    || entry.GetPosition().toString().contains(searchKey)
                    || entry.GetDrawingType().toString().contains(searchKey)
                    || entry.GetScheduleList().toString().contains(searchKey)
                    || entry.GetSocket().toString().contains(searchKey)) {
                foundMapContents.addValue(entry);
            }
        }

        return foundMapContents;
    }

    public void LoadMapContentList() {
        _logger.Debug("LoadMapContentList");

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

    private void sendFailedDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                MapContentDownloadFinishedBroadcast,
                MapContentDownloadFinishedBundle,
                new MapContentDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
    }
}
