package guepardoapps.lucahome.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.ServiceController;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.toolset.common.classes.NotificationContent;

import guepardoapps.toolset.openweather.OpenWeatherController;
import guepardoapps.toolset.openweather.common.OpenWeatherConstants;
import guepardoapps.toolset.openweather.model.ForecastModel;

import guepardoapps.toolset.controller.ReceiverController;

public class OpenWeatherService extends Service {

	private static final String TAG = OpenWeatherService.class.getName();
	private LucaHomeLogger _logger;

	private ForecastModel _forecastModel;

	private Context _context;

	private NotificationService _notificationService;

	private OpenWeatherController _openWeatherController;
	private ReceiverController _receiverController;
	private ServiceController _serviceController;

	private BroadcastReceiver _forecastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_receiverController.UnregisterReceiver(_forecastReceiver);

			_forecastModel = (ForecastModel) intent
					.getSerializableExtra(OpenWeatherConstants.BUNDLE_EXTRA_FORECAST_MODEL);

			if (_forecastModel != null) {
				_logger.Debug(_forecastModel.toString());

				NotificationContent _message = _forecastModel.TellTodaysWeather();

				if (_message != null) {
					_serviceController.StartNotificationWithIconService(_message.GetTitle(), _message.GetText(),
							OpenWeatherConstants.FORECAST_NOTIFICATION_ID, _message.GetIcon(),
							LucaObject.WEATHER_FORECAST);
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

		if (_notificationService == null) {
			_notificationService = new NotificationService();
		}
		if (_openWeatherController == null) {
			_openWeatherController = new OpenWeatherController(_context, Constants.CITY);
		}
		if (_receiverController == null) {
			_receiverController = new ReceiverController(_context);
		}
		if (_serviceController == null) {
			_serviceController = new ServiceController(_context);
		}

		_receiverController.RegisterReceiver(_forecastReceiver,
				new String[] { OpenWeatherConstants.BROADCAST_GET_FORECAST_WEATHER_JSON_FINISHED });

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