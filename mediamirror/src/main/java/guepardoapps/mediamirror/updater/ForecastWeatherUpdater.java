package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import guepardoapps.library.openweather.service.OpenWeatherService;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.Constants;

public class ForecastWeatherUpdater {
    private static final String TAG = ForecastWeatherUpdater.class.getSimpleName();
    private Logger _logger;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private OpenWeatherService _openWeatherService;

    private boolean _isRunning;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver onReceive");
            OpenWeatherService.ForecastWeatherDownloadFinishedContent result =
                    (OpenWeatherService.ForecastWeatherDownloadFinishedContent) intent.getSerializableExtra(OpenWeatherService.ForecastWeatherDownloadFinishedBundle);

            if (result != null) {
                _broadcastController.SendSerializableBroadcast(
                        Broadcasts.SHOW_FORECAST_WEATHER_MODEL,
                        Bundles.FORECAST_WEATHER_MODEL,
                        result);
            } else {
                _logger.Warning("Forecast weather is null!");
            }
        }
    };

    private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_performUpdateReceiver onReceive");
            DownloadWeather();
        }
    };

    public ForecastWeatherUpdater(@NonNull Context context) {
        _logger = new Logger(TAG);

        _broadcastController = new BroadcastController(context);
        _receiverController = new ReceiverController(context);

        _openWeatherService = OpenWeatherService.getInstance();
        _openWeatherService.Initialize(context, Constants.CITY, false, false, null, null, false, true, 5 * 60 * 1000);
    }

    public void Start() {
        _logger.Debug("Initialize");

        if (_isRunning) {
            _logger.Warning("Already running!");
            return;
        }

        _receiverController.RegisterReceiver(_updateReceiver, new String[]{OpenWeatherService.ForecastWeatherDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_FORECAST_WEATHER_UPDATE});

        _isRunning = true;
        DownloadWeather();
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void DownloadWeather() {
        _logger.Debug("DownloadWeather");
        _openWeatherService.LoadForecastWeather();
    }
}
