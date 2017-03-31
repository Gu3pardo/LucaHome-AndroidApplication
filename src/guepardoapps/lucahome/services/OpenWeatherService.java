package guepardoapps.lucahome.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaNotificationController;

import guepardoapps.library.openweather.common.OWBroadcasts;
import guepardoapps.library.openweather.common.OWBundles;
import guepardoapps.library.openweather.common.OWIds;
import guepardoapps.library.openweather.common.model.ForecastModel;
import guepardoapps.library.openweather.controller.OpenWeatherController;

import guepardoapps.library.toolset.common.classes.NotificationContent;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.view.ForecastWeatherView;

public class OpenWeatherService extends Service {

	private static final String TAG = OpenWeatherService.class.getSimpleName();
	private LucaHomeLogger _logger;

	private ForecastModel _forecastModel;

	private Context _context;

	private LucaNotificationController _notificationController;
	private OpenWeatherController _openWeatherController;
	private ReceiverController _receiverController;

	private BroadcastReceiver _forecastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_receiverController.UnregisterReceiver(_forecastReceiver);

			_forecastModel = (ForecastModel) intent.getSerializableExtra(OWBundles.EXTRA_FORECAST_MODEL);

			if (_forecastModel != null) {
				_logger.Debug(_forecastModel.toString());

				NotificationContent _message = _forecastModel.TellForecastWeather();

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
		_context = this;

		_logger = new LucaHomeLogger(TAG);

		if (_notificationController == null) {
			_notificationController = new LucaNotificationController(_context);
		}
		if (_openWeatherController == null) {
			_openWeatherController = new OpenWeatherController(_context, Constants.CITY);
		}
		if (_receiverController == null) {
			_receiverController = new ReceiverController(_context);
		}

		_receiverController.RegisterReceiver(_forecastReceiver,
				new String[] { OWBroadcasts.FORECAST_WEATHER_JSON_FINISHED });

		_openWeatherController.loadForecastWeather();

		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {

	}

	@Override
	public void onDestroy() {
		_receiverController.UnregisterReceiver(_forecastReceiver);
		_logger.Debug("onDestroy");
		super.onDestroy();
	}
}