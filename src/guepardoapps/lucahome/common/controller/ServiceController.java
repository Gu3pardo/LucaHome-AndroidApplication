package guepardoapps.lucahome.common.controller;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.constants.IDs;
import guepardoapps.lucahome.common.constants.ServerActions;
import guepardoapps.lucahome.common.dto.UserDto;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.dto.sensor.TemperatureDto;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.enums.RaspberrySelection;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.*;
import guepardoapps.lucahome.services.helper.UserService;
import guepardoapps.lucahome.services.wearcontrol.WearMessageService;
import guepardoapps.toolset.controller.NetworkController;
import guepardoapps.toolset.openweather.model.ForecastModel;
import guepardoapps.toolset.openweather.model.WeatherModel;

public class ServiceController {

	private static final String TAG = ServiceController.class.getName();
	private LucaHomeLogger _logger;

	private Context _context;

	private NetworkController _networkController;
	private UserService _userService;

	public ServiceController(Context context) {
		_logger = new LucaHomeLogger(TAG);

		_context = context;

		_networkController = new NetworkController(_context, null);
		_userService = new UserService(_context);
	}

	public void StartRestService(String name, String action, String broadcast, LucaObject lucaObject,
			RaspberrySelection raspberrySelection) {
		Intent serviceIntent = new Intent(_context, RESTService.class);
		Bundle serviceData = new Bundle();

		UserDto loggedInUser = _userService.LoadUser();

		serviceData.putString(Bundles.USER, loggedInUser.GetUserName());
		serviceData.putString(Bundles.PASSPHRASE, loggedInUser.GetPassword());

		serviceData.putString(Bundles.ACTION, action);
		serviceData.putString(Bundles.NAME, name);
		serviceData.putString(Bundles.BROADCAST, broadcast);
		serviceData.putSerializable(Bundles.LUCA_OBJECT, lucaObject);
		serviceData.putSerializable(Bundles.RASPBERRY_SELECTION, raspberrySelection);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}

	public void StartNotificationService(String title, String body, int notificationId, LucaObject lucaObject) {
		Intent serviceIntent = new Intent(_context, NotificationService.class);
		Bundle serviceData = new Bundle();

		serviceData.putString(Bundles.NOTIFICATION_TITLE, title);
		serviceData.putString(Bundles.NOTIFICATION_BODY, body);
		serviceData.putInt(Bundles.NOTIFICATION_ID, notificationId);
		serviceData.putSerializable(Bundles.LUCA_OBJECT, lucaObject);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}

	public void StartNotificationWithIconService(String title, String body, int notificationId, int notificationIcon,
			LucaObject lucaObject) {
		Intent serviceIntent = new Intent(_context, NotificationService.class);
		Bundle serviceData = new Bundle();

		serviceData.putString(Bundles.NOTIFICATION_TITLE, title);
		serviceData.putString(Bundles.NOTIFICATION_BODY, body);
		serviceData.putInt(Bundles.NOTIFICATION_ID, notificationId);
		serviceData.putInt(Bundles.NOTIFICATION_ICON, notificationIcon);
		serviceData.putSerializable(Bundles.LUCA_OBJECT, lucaObject);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}

	public void StartSocketNotificationService(int notificationId, SerializableList<WirelessSocketDto> socketList) {
		if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
			Toast.makeText(_context, "No LucaHome network!", Toast.LENGTH_SHORT).show();
			return;
		}

		Intent serviceIntent = new Intent(_context, NotificationService.class);
		Bundle serviceData = new Bundle();

		serviceData.putSerializable(Bundles.LUCA_OBJECT, LucaObject.WIRELESS_SOCKET);
		serviceData.putInt(Bundles.NOTIFICATION_ID, notificationId);
		serviceData.putSerializable(Bundles.SOCKET_LIST, socketList);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}

	public void StartTemperatureNotificationService(int notificationId,
			SerializableList<TemperatureDto> temperatureList, WeatherModel currentWeather) {
		if (temperatureList == null) {
			_logger.Error("temperatureList is null!");
			return;
		}
		if (currentWeather == null) {
			_logger.Error("currentWeather is null!");
			return;
		}

		if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
			Toast.makeText(_context, "No LucaHome network!", Toast.LENGTH_SHORT).show();
			return;
		}

		Intent serviceIntent = new Intent(_context, NotificationService.class);
		Bundle serviceData = new Bundle();

		serviceData.putSerializable(Bundles.LUCA_OBJECT, LucaObject.TEMPERATURE);
		serviceData.putInt(Bundles.NOTIFICATION_ID, notificationId);
		serviceData.putSerializable(Bundles.TEMPERATURE_LIST, temperatureList);
		serviceData.putSerializable(Bundles.WEATHER_CURRENT, currentWeather);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}

	public void StartWeatherNotificationService(int notificationId, WeatherModel currentWeather,
			ForecastModel forecastWeather) {
		if (currentWeather == null) {
			_logger.Error("currentWeather is null!");
			return;
		}
		if (forecastWeather == null) {
			_logger.Error("forecastWeather is null!");
			return;
		}

		Intent serviceIntent = new Intent(_context, NotificationService.class);
		Bundle serviceData = new Bundle();

		serviceData.putSerializable(Bundles.LUCA_OBJECT, LucaObject.WEATHER_DATA);
		serviceData.putInt(Bundles.NOTIFICATION_ID, notificationId);
		serviceData.putSerializable(Bundles.WEATHER_CURRENT, currentWeather);
		serviceData.putSerializable(Bundles.WEATHER_FORECAST, forecastWeather);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}

	public void StopSound() {
		StartRestService(TAG, ServerActions.STOP_SOUND, Broadcasts.STOP_SOUND, LucaObject.SOUND,
				RaspberrySelection.BOTH);
		CloseNotification(IDs.NOTIFICATION_SONG + RaspberrySelection.RASPBERRY_1.GetInt());
		CloseNotification(IDs.NOTIFICATION_SONG + RaspberrySelection.RASPBERRY_2.GetInt());
	}

	public void SendMessageToWear(String message) {
		Intent serviceIntent = new Intent(_context, WearMessageService.class);
		Bundle serviceData = new Bundle();
		serviceData.putString(Bundles.WEAR_MESSAGE_TEXT, message);
		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}

	public void CloseNotification(int notificationId) {
		NotificationManager notificationManager = (NotificationManager) _context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(notificationId);
	}
}
