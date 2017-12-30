package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.Locale;

import guepardoapps.library.openweather.models.WeatherModel;
import guepardoapps.library.openweather.service.OpenWeatherService;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.Temperature;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.converter.JsonDataToTemperatureConverter;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.interfaces.services.IDataNotificationService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class TemperatureService implements IDataNotificationService {
    public static class TemperatureDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<Temperature> TemperatureList;

        TemperatureDownloadFinishedContent(SerializableList<Temperature> temperatureList, boolean succcess) {
            super(succcess, new byte[]{});
            TemperatureList = temperatureList;
        }
    }

    public static final int NOTIFICATION_ID = 64371930;

    public static final String TemperatureDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.temperature.download.finished";
    public static final String TemperatureDownloadFinishedBundle = "TemperatureDownloadFinishedBundle";

    private static final TemperatureService SINGLETON = new TemperatureService();
    private boolean _isInitialized;

    private static final String TAG = TemperatureService.class.getSimpleName();

    private boolean _displayNotification;
    private Class<?> _receiverActivity;

    private static final int MIN_TIMEOUT_MIN = 15;
    private static final int MAX_TIMEOUT_MIN = 24 * 60;

    private Date _lastUpdate;

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
    private NotificationController _notificationController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;
    private OpenWeatherService _openWeatherService;

    private SerializableList<Temperature> _temperatureList = new SerializableList<>();

    private BroadcastReceiver _temperatureDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Temperature) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedDownloadBroadcast();
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedDownloadBroadcast();
                return;
            }

            SerializableList<Temperature> temperatureList = JsonDataToTemperatureConverter.getInstance().GetList(contentResponse);
            if (temperatureList == null) {
                Logger.getInstance().Error(TAG, "Converted temperatureList is null!");
                sendFailedDownloadBroadcast();
                return;
            }

            _temperatureList = temperatureList;

            ShowNotification();

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

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    TemperatureDownloadFinishedBroadcast,
                    TemperatureDownloadFinishedBundle,
                    new TemperatureDownloadFinishedContent(_temperatureList, true));
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

    private TemperatureService() {
    }

    public static TemperatureService getInstance() {
        return SINGLETON;
    }

    @Override
    public void Initialize(@NonNull Context context, @NonNull Class<?> receiverActivity, boolean displayNotification, boolean reloadEnabled, int reloadTimeout) {
        if (_isInitialized) {
            Logger.getInstance().Warning(TAG, "Already initialized!");
            return;
        }

        _lastUpdate = new Date();

        _receiverActivity = receiverActivity;
        _displayNotification = displayNotification;
        _reloadEnabled = reloadEnabled;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _notificationController = new NotificationController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();
        _openWeatherService = OpenWeatherService.getInstance();

        _receiverController.RegisterReceiver(_temperatureDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _isInitialized = false;
    }

    @Override
    public SerializableList<Temperature> GetDataList() {
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

    @Override
    public SerializableList<Temperature> SearchDataList(@NonNull String searchKey) {
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

    @Override
    public void LoadData() {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast();
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_TEMPERATURES.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Temperature, true);
    }

    @Override
    public void SetReceiverActivity(@NonNull Class<?> receiverActivity) {
        _receiverActivity = receiverActivity;
    }

    @Override
    public Class<?> GetReceiverActivity() {
        return _receiverActivity;
    }

    @Override
    public void ShowNotification() {
        if (_temperatureList == null) {
            Logger.getInstance().Error(TAG, "_temperatureList is null!");
            return;
        }

        if (_temperatureList.getSize() < 1) {
            Logger.getInstance().Error(TAG, "_temperatureList is empty!");
            return;
        }

        Temperature temperature = _temperatureList.getValue(0);
        String title = String.format(Locale.getDefault(), "%s", temperature.GetTime().HHMM());
        String body = String.format(Locale.getDefault(), "%s: %s", temperature.GetArea(), temperature.GetTemperatureString());

        _notificationController.CreateTemperatureNotification(NOTIFICATION_ID, _receiverActivity, R.drawable.weather_dummy, title, body, true);
    }

    @Override
    public void CloseNotification() {
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

    private void sendFailedDownloadBroadcast() {
        _broadcastController.SendSerializableBroadcast(
                TemperatureDownloadFinishedBroadcast,
                TemperatureDownloadFinishedBundle,
                new TemperatureDownloadFinishedContent(null, false));
    }
}
