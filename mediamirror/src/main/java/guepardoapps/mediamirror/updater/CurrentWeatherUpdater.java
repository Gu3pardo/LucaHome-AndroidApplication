package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import guepardoapps.library.openweather.models.WeatherModel;
import guepardoapps.library.openweather.service.OpenWeatherService;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.Constants;

public class CurrentWeatherUpdater {
    private static final String TAG = CurrentWeatherUpdater.class.getSimpleName();

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private boolean _isRunning;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            OpenWeatherService.CurrentWeatherDownloadFinishedContent result = (OpenWeatherService.CurrentWeatherDownloadFinishedContent) intent.getSerializableExtra(OpenWeatherService.CurrentWeatherDownloadFinishedBundle);
            if (result != null) {
                WeatherModel currentWeather = result.CurrentWeather;
                if (currentWeather != null) {
                    _broadcastController.SendSerializableBroadcast(
                            Broadcasts.SHOW_CURRENT_WEATHER_MODEL,
                            Bundles.CURRENT_WEATHER_MODEL,
                            currentWeather);
                } else {
                    Logger.getInstance().Warning(TAG, "Current weather is null!");
                }
            }
        }
    };

    private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadWeather();
        }
    };

    public CurrentWeatherUpdater(@NonNull Context context) {
        _broadcastController = new BroadcastController(context);
        _receiverController = new ReceiverController(context);
        OpenWeatherService.getInstance().Initialize(context, Constants.CITY, false, false, null, null, false, true, 5 * 60 * 1000);
    }

    public void Start() {
        if (_isRunning) {
            Logger.getInstance().Warning(TAG, "Already running!");
            return;
        }

        _receiverController.RegisterReceiver(_updateReceiver, new String[]{OpenWeatherService.CurrentWeatherDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_CURRENT_WEATHER_UPDATE});

        _isRunning = true;
        DownloadWeather();
    }

    public void Dispose() {
        OpenWeatherService.getInstance().Dispose();
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void DownloadWeather() {
        OpenWeatherService.getInstance().LoadCurrentWeather();
    }
}
