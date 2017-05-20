package guepardoapps.lucahome.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaNotificationController;

import guepardoapps.library.openweather.common.OWBroadcasts;
import guepardoapps.library.openweather.common.OWBundles;
import guepardoapps.library.openweather.common.OWIds;
import guepardoapps.library.openweather.common.model.ForecastModel;
import guepardoapps.library.openweather.controller.OpenWeatherController;

import guepardoapps.library.toolset.common.classes.NotificationContent;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.views.ForecastWeatherView;

public class OpenWeatherService extends Service {

    private static final String TAG = OpenWeatherService.class.getSimpleName();
    private LucaHomeLogger _logger;

    private LucaNotificationController _notificationController;
    private OpenWeatherController _openWeatherController;
    private ReceiverController _receiverController;

    private BroadcastReceiver _forecastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _receiverController.UnregisterReceiver(_forecastReceiver);

            ForecastModel forecastModel = (ForecastModel) intent.getSerializableExtra(OWBundles.EXTRA_FORECAST_MODEL);
            if (forecastModel != null) {
                _logger.Debug(forecastModel.toString());

                NotificationContent _message = forecastModel.TellForecastWeather();

                if (_message != null) {
                    _notificationController.CreateForecastWeatherNotification(ForecastWeatherView.class,
                            _message.GetIcon(), _message.GetTitle(), _message.GetText(),
                            OWIds.FORECAST_NOTIFICATION_ID);
                } else {
                    _logger.Error("_message is null!");
                }
            } else {
                _logger.Error("_forecastModel is null!");
            }

            stopSelf();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _logger = new LucaHomeLogger(TAG);

        if (_notificationController == null) {
            _notificationController = new LucaNotificationController(this);
        }
        if (_openWeatherController == null) {
            _openWeatherController = new OpenWeatherController(this, Constants.CITY);
        }
        if (_receiverController == null) {
            _receiverController = new ReceiverController(this);
        }

        _receiverController.RegisterReceiver(_forecastReceiver,
                new String[]{OWBroadcasts.FORECAST_WEATHER_JSON_FINISHED});

        _openWeatherController.LoadForecastWeather();

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        _receiverController.UnregisterReceiver(_forecastReceiver);
        _logger.Debug("onDestroy");
        super.onDestroy();
    }
}