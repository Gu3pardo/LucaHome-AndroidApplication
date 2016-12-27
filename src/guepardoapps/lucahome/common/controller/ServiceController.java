package guepardoapps.lucahome.common.controller;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import guepardoapps.lucahome.common.Constants;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.enums.RaspberrySelection;
import guepardoapps.lucahome.dto.UserDto;
import guepardoapps.lucahome.dto.WirelessSocketDto;
import guepardoapps.lucahome.dto.sensor.TemperatureDto;
import guepardoapps.lucahome.services.*;
import guepardoapps.lucahome.services.helper.UserService;
import guepardoapps.lucahome.wearcontrol.services.WearMessageService;
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

		serviceData.putString(Constants.BUNDLE_USER, loggedInUser.GetUserName());
		serviceData.putString(Constants.BUNDLE_PASSPHRASE, loggedInUser.GetPassword());

		serviceData.putString(Constants.BUNDLE_ACTION, action);
		serviceData.putString(Constants.BUNDLE_NAME, name);
		serviceData.putString(Constants.BUNDLE_BROADCAST, broadcast);
		serviceData.putSerializable(Constants.BUNDLE_LUCA_OBJECT, lucaObject);
		serviceData.putSerializable(Constants.BUNDLE_RASPBERRY_SELECTION, raspberrySelection);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}

	public void StartNotificationService(String title, String body, int notificationId, LucaObject lucaObject) {
		Intent serviceIntent = new Intent(_context, NotificationService.class);
		Bundle serviceData = new Bundle();

		serviceData.putString(Constants.BUNDLE_NOTIFICATION_TITLE, title);
		serviceData.putString(Constants.BUNDLE_NOTIFICATION_BODY, body);
		serviceData.putInt(Constants.BUNDLE_NOTIFICATION_ID, notificationId);
		serviceData.putSerializable(Constants.BUNDLE_LUCA_OBJECT, lucaObject);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}

	public void StartNotificationWithIconService(String title, String body, int notificationId, int notificationIcon,
			LucaObject lucaObject) {
		Intent serviceIntent = new Intent(_context, NotificationService.class);
		Bundle serviceData = new Bundle();

		serviceData.putString(Constants.BUNDLE_NOTIFICATION_TITLE, title);
		serviceData.putString(Constants.BUNDLE_NOTIFICATION_BODY, body);
		serviceData.putInt(Constants.BUNDLE_NOTIFICATION_ID, notificationId);
		serviceData.putInt(Constants.BUNDLE_NOTIFICATION_ICON, notificationIcon);
		serviceData.putSerializable(Constants.BUNDLE_LUCA_OBJECT, lucaObject);

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

		serviceData.putSerializable(Constants.BUNDLE_LUCA_OBJECT, LucaObject.WIRELESS_SOCKET);
		serviceData.putInt(Constants.BUNDLE_NOTIFICATION_ID, notificationId);
		serviceData.putSerializable(Constants.BUNDLE_SOCKET_LIST, socketList);

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

		serviceData.putSerializable(Constants.BUNDLE_LUCA_OBJECT, LucaObject.TEMPERATURE);
		serviceData.putInt(Constants.BUNDLE_NOTIFICATION_ID, notificationId);
		serviceData.putSerializable(Constants.BUNDLE_TEMPERATURE_LIST, temperatureList);
		serviceData.putSerializable(Constants.BUNDLE_WEATHER_CURRENT, currentWeather);

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

		serviceData.putSerializable(Constants.BUNDLE_LUCA_OBJECT, LucaObject.WEATHER_DATA);
		serviceData.putInt(Constants.BUNDLE_NOTIFICATION_ID, notificationId);
		serviceData.putSerializable(Constants.BUNDLE_WEATHER_CURRENT, currentWeather);
		serviceData.putSerializable(Constants.BUNDLE_WEATHER_FORECAST, forecastWeather);

		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}

	public void StopSound() {
		StartRestService(TAG, Constants.ACTION_STOP_SOUND, Constants.BROADCAST_STOP_SOUND, LucaObject.SOUND,
				RaspberrySelection.BOTH);
		CloseNotification(Constants.ID_NOTIFICATION_SONG + RaspberrySelection.RASPBERRY_1.GetInt());
		CloseNotification(Constants.ID_NOTIFICATION_SONG + RaspberrySelection.RASPBERRY_2.GetInt());
	}

	public void SendMessageToWear(String message) {
		Intent serviceIntent = new Intent(_context, WearMessageService.class);
		Bundle serviceData = new Bundle();
		serviceData.putString(Constants.BUNDLE_WEAR_MESSAGE_TEXT, message);
		serviceIntent.putExtras(serviceData);
		_context.startService(serviceIntent);
	}

	public void CloseNotification(int notificationId) {
		NotificationManager notificationManager = (NotificationManager) _context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(notificationId);
	}
}
