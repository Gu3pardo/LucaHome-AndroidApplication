package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.library.openweather.models.WeatherModel;
import guepardoapps.library.openweather.service.OpenWeatherService;
import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.Temperature;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.converter.JsonDataToTemperatureConverter;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class TemperatureService {
    public static class TemperatureDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<Temperature> TemperatureList;

        public TemperatureDownloadFinishedContent(SerializableList<Temperature> temperatureList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            TemperatureList = temperatureList;
        }
    }

    public static final String TemperatureDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.temperature.download.finished";
    public static final String TemperatureDownloadFinishedBundle = "TemperatureDownloadFinishedBundle";

    private static final TemperatureService SINGLETON = new TemperatureService();
    private boolean _isInitialized;

    private static final String TAG = TemperatureService.class.getSimpleName();
    private Logger _logger;

    private static final int TIMEOUT_MS = 5 * 60 * 1000;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_reloadListRunnable run");
            LoadTemperatureList();
            _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);
        }
    };

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;
    private OpenWeatherService _openWeatherService;

    private JsonDataToTemperatureConverter _jsonDataToTemperatureConverter;

    private SerializableList<Temperature> _temperatureList = new SerializableList<>();

    private BroadcastReceiver _temperatureDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_temperatureDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Temperature) {
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

            SerializableList<Temperature> temperatureList = _jsonDataToTemperatureConverter.GetList(contentResponse);
            if (temperatureList == null) {
                _logger.Error("Converted temperatureList is null!");
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _temperatureList = temperatureList;

            WeatherModel currentWeather = _openWeatherService.CurrentWeather();
            if (currentWeather != null) {
                Temperature currentWeatherTemperature = new Temperature(
                        currentWeather.GetTemperature(),
                        "Outdoor",
                        new SerializableDate(), new SerializableTime(),
                        "",
                        Temperature.TemperatureType.CITY,
                        "");
                _temperatureList.addValue(currentWeatherTemperature);
            }

            _broadcastController.SendSerializableBroadcast(
                    TemperatureDownloadFinishedBroadcast,
                    TemperatureDownloadFinishedBundle,
                    new TemperatureDownloadFinishedContent(_temperatureList, true, content.Response));
        }
    };

    private TemperatureService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static TemperatureService getInstance() {
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
        _openWeatherService = OpenWeatherService.getInstance();

        _receiverController.RegisterReceiver(_temperatureDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _jsonDataToTemperatureConverter = new JsonDataToTemperatureConverter();

        _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);

        _isInitialized = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public SerializableList<Temperature> GetTemperatureList() {
        return _temperatureList;
    }

    public Temperature GetByArea(@NonNull String area) {
        for (int index = 0; index < _temperatureList.getSize(); index++) {
            Temperature entry = _temperatureList.getValue(index);

            if (entry.GetArea().contains(area)) {
                return entry;
            }
        }

        return null;
    }

    public Temperature GetByType(@NonNull Temperature.TemperatureType temperatureType) {
        for (int index = 0; index < _temperatureList.getSize(); index++) {
            Temperature entry = _temperatureList.getValue(index);

            if (entry.GetTemperatureType() == temperatureType) {
                return entry;
            }
        }

        return null;
    }

    public SerializableList<Temperature> FoundTemperatures(@NonNull String searchKey) {
        SerializableList<Temperature> foundTemperatures = new SerializableList<>();

        for (int index = 0; index < _temperatureList.getSize(); index++) {
            Temperature entry = _temperatureList.getValue(index);

            if (entry.GetArea().contains(searchKey)
                    || entry.GetDate().toString().contains(searchKey)
                    || entry.GetGraphPath().contains(searchKey)
                    || entry.GetSensorPath().contains(searchKey)
                    || entry.GetTemperatureString().contains(searchKey)
                    || String.valueOf(entry.GetTemperature()).contains(searchKey)) {
                foundTemperatures.addValue(entry);
            }
        }

        return foundTemperatures;
    }

    public String GetOpenWeatherCity() {
        return _openWeatherService.GetCity();
    }

    public void SetOpenWeatherCity(@NonNull String city) {
        _openWeatherService.SetCity(city);
    }

    public void LoadTemperatureList() {
        _logger.Debug("LoadTemperatureList");

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast("No user");
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_TEMPERATURES.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Temperature, true);
    }

    public void ShowNotification() {
        _logger.Debug("ShowNotification");
        /*TODO Show notification*/
    }

    public void CloseNotification() {
        _logger.Debug("CloseNotification");
        /*TODO Close notification*/
    }

    private void sendFailedDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                TemperatureDownloadFinishedBroadcast,
                TemperatureDownloadFinishedBundle,
                new TemperatureDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
    }
}
