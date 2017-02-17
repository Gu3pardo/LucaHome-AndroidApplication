package guepardoapps.lucahome.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.classes.*;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.IDs;
import guepardoapps.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.dto.sensor.TemperatureDto;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.enums.TemperatureType;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.receiver.notifications.HeatingReceiver;
import guepardoapps.lucahome.receiver.notifications.SocketActionReceiver;
import guepardoapps.lucahome.receiver.notifications.StopSoundReceiver;
import guepardoapps.lucahome.view.BirthdayView;
import guepardoapps.lucahome.view.SensorTemperatureView;
import guepardoapps.lucahome.view.controller.SocketController;
import guepardoapps.toolset.controller.SharedPrefController;
import guepardoapps.toolset.openweather.model.WeatherModel;

public class NotificationService extends Service {

	private static final String TAG = NotificationService.class.getName();
	private LucaHomeLogger _logger;

	public static final String SOCKET_ALL = "All_Sockets";
	public static final String SOCKET_SINGLE = "Single_Sockets";

	public static final String HEATING_AND_SOUND = "Heating_And_Sound";
	public static final String HEATING_SINGLE = "Single_Heating";

	private SharedPrefController _sharedPrefController;
	private SocketController _socketController;

	@SuppressWarnings("unchecked")
	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		_logger = new LucaHomeLogger(TAG);

		_sharedPrefController = new SharedPrefController(this, SharedPrefConstants.SHARED_PREF_NAME);
		_socketController = new SocketController(this);

		Bundle data = intent.getExtras();

		LucaObject lucaObject = (LucaObject) data.getSerializable(Bundles.LUCA_OBJECT);
		int id = data.getInt(Bundles.NOTIFICATION_ID);

		switch (lucaObject) {
		case BIRTHDAY:
			String title = data.getString(Bundles.NOTIFICATION_TITLE);
			String body = data.getString(Bundles.NOTIFICATION_BODY);
			CreateBirthdayNotification(R.drawable.birthday, title, body, true, id);
			break;
		case SOUND:
			String soundFile = data.getString(Bundles.NOTIFICATION_BODY);
			String raspberry = data.getString(Bundles.NOTIFICATION_TITLE);
			CreateSoundNotification(soundFile, raspberry, id);
			break;
		case TEMPERATURE:
			SerializableList<TemperatureDto> temperatureList = (SerializableList<TemperatureDto>) data
					.getSerializable(Bundles.TEMPERATURE_LIST);
			WeatherModel currentWeather = (WeatherModel) intent.getSerializableExtra(Bundles.WEATHER_CURRENT);
			CreateTemperatureNotification(temperatureList, currentWeather);
			break;
		case WIRELESS_SOCKET:
			SerializableList<WirelessSocketDto> wirelessSocketList = (SerializableList<WirelessSocketDto>) data
					.getSerializable(Bundles.SOCKET_LIST);
			CreateSocketNotification(wirelessSocketList);
			break;
		case WEATHER_FORECAST:
			String forecastTitle = data.getString(Bundles.NOTIFICATION_TITLE);
			String forecastBody = data.getString(Bundles.NOTIFICATION_BODY);
			int forecastIcon = data.getInt(Bundles.NOTIFICATION_ICON);
			CreateForecastWeatherNotification(forecastIcon, forecastTitle, forecastBody, id);
			break;
		case GO_TO_BED:
			CreateSleepHeatingNotification();
			break;
		default:
			_logger.Warn(lucaObject.toString() + " is not supported!");
			break;
		}

		stopSelf();
		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private void CreateBirthdayNotification(int icon, String title, String body, boolean autocancelable, int id) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);

		Intent intent = new Intent(this, BirthdayView.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, id, intent, 0);

		builder.setSmallIcon(icon).setContentTitle(title).setContentText(body).setTicker(body)
				.setContentIntent(pendingIntent).setAutoCancel(autocancelable);

		Notification notification = builder.build();
		notificationManager.notify(id, notification);
	}

	@SuppressWarnings("deprecation")
	private void CreateSoundNotification(String soundFile, String raspberry, int id) {
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sound_on);
		NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
				.setHintHideIcon(true).setBackground(bitmap);

		Intent stopSoundIntent = new Intent(this, StopSoundReceiver.class);
		PendingIntent stopSoundPendingIntent = PendingIntent.getBroadcast(this, 7452348, stopSoundIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Action wearAction = new NotificationCompat.Action.Builder(R.drawable.sound_on, "Stop",
				stopSoundPendingIntent).build();

		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_play_sound);
		remoteViews.setTextViewText(R.id.notification_playsound_title, soundFile);
		remoteViews.setOnClickPendingIntent(R.id.notification_playsound_stop, stopSoundPendingIntent);

		remoteViews.setTextViewText(R.id.notification_playsound_raspberry, raspberry);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.sound_on).setContentTitle("Sound is playing").setContentText("Stop Sound")
				.setTicker("Stop Sound").extend(wearableExtender).addAction(wearAction);

		Notification notification = builder.build();
		notification.contentView = remoteViews;
		notification.bigContentView = remoteViews;

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		notificationManager.notify(id, notification);
	}

	private void CreateTemperatureNotification(SerializableList<TemperatureDto> temperatureList,
			WeatherModel currentWeather) {
		if (!_sharedPrefController
				.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_WEATHER_NOTIFICATION)) {
			_logger.Warn("Not allowed to display socket notification!");
			return;
		}

		boolean containsEntriesToRemove = true;
		int removeCounts = 0;
		int listSize = temperatureList.getSize();

		while (containsEntriesToRemove && removeCounts < listSize) {
			containsEntriesToRemove = false;
			removeCounts++;

			for (int index = 0; index < temperatureList.getSize(); index++) {
				if (temperatureList.getValue(index).GetTemperatureType() == TemperatureType.CITY
						|| temperatureList.getValue(index).GetTemperatureType() == TemperatureType.SMARTPHONE_SENSOR) {
					temperatureList.removeValue(index);
					containsEntriesToRemove = true;
				}
			}
		}

		TemperatureDto currentTemperature = new TemperatureDto(currentWeather.GetTemperature(),
				currentWeather.GetCity(), currentWeather.GetLastUpdate(), "n.a.", TemperatureType.CITY, "n.a.");
		temperatureList.addValue(currentTemperature);

		String title = "Temperatures";
		String body = "";

		for (int index = 0; index < temperatureList.getSize(); index++) {
			body += temperatureList.getValue(index).GetArea() + ":"
					+ temperatureList.getValue(index).GetTemperatureString() + " | ";
		}
		body = body.substring(0, body.length() - 3);

		Intent intent = new Intent(this, SensorTemperatureView.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, IDs.NOTIFICATION_TEMPERATURE * 2, intent, 0);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setSmallIcon(R.drawable.temperature).setContentIntent(pendingIntent).setContentTitle(title)
				.setContentText(body).setTicker(body);
		Notification notification = builder.build();
		notificationManager.notify(IDs.NOTIFICATION_TEMPERATURE, notification);
	}

	@SuppressWarnings("deprecation")
	private void CreateSocketNotification(SerializableList<WirelessSocketDto> wirelessSocketList) {
		if (!_sharedPrefController
				.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SOCKET_NOTIFICATION)) {
			_logger.Warn("Not allowed to display socket notification!");
			return;
		}

		if (wirelessSocketList.getSize() > 10) {
			_logger.Warn("Too many sockets! Cannot display notification!");
			Toasty.error(this, "Too many sockets! Cannot display notification!", Toast.LENGTH_SHORT).show();
			return;
		}

		int[] imageButtonArray = new int[] { R.id.socket_1_on, R.id.socket_1_off, R.id.socket_2_on, R.id.socket_2_off,
				R.id.socket_3_on, R.id.socket_3_off, R.id.socket_4_on, R.id.socket_4_off, R.id.socket_5_on,
				R.id.socket_5_off, R.id.socket_6_on, R.id.socket_6_off, R.id.socket_7_on, R.id.socket_7_off,
				R.id.socket_8_on, R.id.socket_8_off, R.id.socket_9_on, R.id.socket_9_off, R.id.socket_10_on,
				R.id.socket_10_off };
		boolean[] socketVisibility = new boolean[] { false, false, false, false, false, false, false, false, false,
				false };

		Intent[] socketIntents = new Intent[wirelessSocketList.getSize()];
		Bundle[] socketIntentDatas = new Bundle[wirelessSocketList.getSize()];
		PendingIntent[] socketPendingIntents = new PendingIntent[wirelessSocketList.getSize()];

		NotificationCompat.Action[] wearActions = new NotificationCompat.Action[wirelessSocketList.getSize()];

		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
				.setHintHideIcon(true).setBackground(bitmap);

		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_skeleton_socket);

		for (int index = 0; index < wirelessSocketList.getSize(); index++) {
			socketVisibility[index] = _sharedPrefController.LoadBooleanValueFromSharedPreferences(
					wirelessSocketList.getValue(index).GetNotificationVisibilitySharedPrefKey());

			socketIntents[index] = new Intent(this, SocketActionReceiver.class);

			socketIntentDatas[index] = new Bundle();
			socketIntentDatas[index].putString(Bundles.ACTION, SOCKET_SINGLE);
			socketIntentDatas[index].putSerializable(Bundles.SOCKET_DATA, wirelessSocketList.getValue(index));
			socketIntents[index].putExtras(socketIntentDatas[index]);

			socketPendingIntents[index] = PendingIntent.getBroadcast(this, index * 1234, socketIntents[index],
					PendingIntent.FLAG_UPDATE_CURRENT);

			int iconId = _socketController.GetDrawable(wirelessSocketList.getValue(index));
			String text = ((wirelessSocketList.getValue(index).GetIsActivated()) ? "ON" : "OFF");
			Bitmap icon = BitmapFactory.decodeResource(getResources(), iconId);

			wearActions[index] = new NotificationCompat.Action.Builder(iconId, text, socketPendingIntents[index])
					.build();

			if (socketVisibility[index]) {
				if (wirelessSocketList.getValue(index).GetIsActivated()) {
					remoteViews.setViewVisibility(imageButtonArray[(index * 2)], View.GONE);
					remoteViews.setViewVisibility(imageButtonArray[(index * 2) + 1], View.VISIBLE);
					remoteViews.setOnClickPendingIntent(imageButtonArray[(index * 2) + 1], socketPendingIntents[index]);
					remoteViews.setImageViewBitmap(imageButtonArray[(index * 2) + 1], icon);
				} else {
					remoteViews.setViewVisibility(imageButtonArray[(index * 2)], View.VISIBLE);
					remoteViews.setViewVisibility(imageButtonArray[(index * 2) + 1], View.GONE);
					remoteViews.setOnClickPendingIntent(imageButtonArray[(index * 2)], socketPendingIntents[index]);
					remoteViews.setImageViewBitmap(imageButtonArray[(index * 2)], icon);
				}
			}
		}

		for (int visibilityIndex = 0; visibilityIndex < 10; visibilityIndex++) {
			if (!socketVisibility[visibilityIndex]) {
				remoteViews.setViewVisibility(imageButtonArray[(visibilityIndex * 2)], View.GONE);
				remoteViews.setViewVisibility(imageButtonArray[(visibilityIndex * 2) + 1], View.GONE);
			}
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.ic_launcher).setContentTitle(getString(R.string.app_name))
				.setContentText("Set Sockets").setTicker("Set Sockets");
		builder.extend(wearableExtender);
		for (int index = 0; index < wirelessSocketList.getSize(); index++) {
			if (socketVisibility[index]) {
				builder.addAction(wearActions[index]);
			}
		}

		// Hack for disabling all sockets at once!

		Intent allSocketsIntent = new Intent(this, SocketActionReceiver.class);
		Bundle allSocketsData = new Bundle();
		allSocketsData.putString(Bundles.ACTION, SOCKET_ALL);
		allSocketsIntent.putExtras(allSocketsData);
		PendingIntent allSocketsPendingIntent;
		allSocketsPendingIntent = PendingIntent.getBroadcast(this, 30, allSocketsIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Action action_all_sockets_off = new NotificationCompat.Action.Builder(
				R.drawable.all_power_off, getString(R.string.all_sockets_off), allSocketsPendingIntent).build();
		remoteViews.setOnClickPendingIntent(R.id.socket_all_off, allSocketsPendingIntent);
		builder.addAction(action_all_sockets_off);

		// End Hack

		Notification notification = builder.build();
		notification.contentView = remoteViews;
		notification.bigContentView = remoteViews;

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		notificationManager.notify(IDs.NOTIFICATION_WEAR, notification);
	}

	private void CreateForecastWeatherNotification(int icon, String title, String body, int id) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setSmallIcon(icon).setContentTitle(title).setContentText(body).setTicker(body);
		Notification notification = builder.build();
		notificationManager.notify(id, notification);
	}

	@SuppressWarnings("deprecation")
	private void CreateSleepHeatingNotification() {
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.heating);
		NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
				.setHintHideIcon(true).setBackground(bitmap);
		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_sleep);

		// Action for button enable heating only
		Intent enableHeatingIntent = new Intent(this, HeatingReceiver.class);
		Bundle enableHeatingBundle = new Bundle();
		enableHeatingBundle.putString(Bundles.ACTION, HEATING_SINGLE);
		enableHeatingIntent.putExtras(enableHeatingBundle);
		PendingIntent enableHeatingPendingIntent = PendingIntent.getBroadcast(this, 34154338, enableHeatingIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Action enableHeatingWearAction = new NotificationCompat.Action.Builder(
				R.drawable.compose_icon_socket, "Enable Heating", enableHeatingPendingIntent).build();
		remoteViews.setOnClickPendingIntent(R.id.enableHeatingOnly, enableHeatingPendingIntent);

		// Action for button enable heating and sound
		Intent enableHeatingAndSoundIntent = new Intent(this, HeatingReceiver.class);
		Bundle enableHeatingAndSoundBundle = new Bundle();
		enableHeatingAndSoundBundle.putString(Bundles.ACTION, HEATING_AND_SOUND);
		enableHeatingAndSoundIntent.putExtras(enableHeatingAndSoundBundle);
		PendingIntent enableHeatingAndSoundPendingIntent = PendingIntent.getBroadcast(this, 34154235,
				enableHeatingAndSoundIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Action enableHeatingAndSoundWearAction = new NotificationCompat.Action.Builder(
				R.drawable.compose_icon_socket, "Heating And Sound", enableHeatingAndSoundPendingIntent).build();
		remoteViews.setOnClickPendingIntent(R.id.enableHeatingAndSound, enableHeatingAndSoundPendingIntent);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.heating).setContentTitle("Relax in the bed!")
				.setContentText("Enjoying a warm bed!").setTicker("").extend(wearableExtender)
				.addAction(enableHeatingWearAction).addAction(enableHeatingAndSoundWearAction);

		Notification notification = builder.build();
		notification.contentView = remoteViews;
		notification.bigContentView = remoteViews;

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		notificationManager.notify(IDs.NOTIFICATION_SLEEP, notification);
	}
}
