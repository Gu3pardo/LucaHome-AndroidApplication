package guepardoapps.library.lucahome.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.RemoteViews;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.IDs;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.dto.TemperatureDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.enums.TemperatureType;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.receiver.notifications.CameraReceiver;
import guepardoapps.library.lucahome.receiver.notifications.HeatingReceiver;
import guepardoapps.library.lucahome.receiver.notifications.SocketActionReceiver;

import guepardoapps.library.openweather.common.model.WeatherModel;

import guepardoapps.library.toolset.common.Tools;
import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.NotificationController;
import guepardoapps.library.toolset.controller.SharedPrefController;

public class LucaNotificationController extends NotificationController {

    private static final String TAG = LucaNotificationController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final int MAX_NOTIFICATION_SOCKETS = 10;

    public static final String SOCKET_ALL = "All_Sockets";
    public static final String SOCKET_SINGLE = "Single_Sockets";

    public static final String HEATING_AND_SOUND = "Heating_And_Sound";
    public static final String HEATING_SINGLE = "Single_Heating";
    public static final String SOUND_SINGLE = "Single_Sound";

    public static final String DISABLE_CAMERA = "Disable_Camera";

    private Context _context;
    private SharedPrefController _sharedPrefController;
    private Tools _tools;

    public LucaNotificationController(Context context) {
        super(context);
        _logger = new LucaHomeLogger(TAG);
        _context = context;
        _sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);
        _tools = new Tools();
    }

    public void CreateBirthdayNotification(@NonNull Class<?> birthdayActivity,
                                           int icon,
                                           @NonNull String title,
                                           @NonNull String body,
                                           boolean autoCancelable) {
        if (!_sharedPrefController
                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_BIRTHDAY_NOTIFICATION)) {
            _logger.Warn("Not allowed to display birthday notification!");
            return;
        }

        NotificationManager notificationManager = (NotificationManager) _context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(_context);

        Intent intent = new Intent(_context, birthdayActivity);
        PendingIntent pendingIntent = PendingIntent.getActivity(_context, IDs.NOTIFICATION_BIRTHDAY, intent, 0);

        builder.setSmallIcon(icon).setContentTitle(title).setContentText(body).setTicker(body)
                .setContentIntent(pendingIntent).setAutoCancel(autoCancelable);

        Notification notification = builder.build();
        notificationManager.notify(IDs.NOTIFICATION_BIRTHDAY, notification);
    }

    public void CreateTemperatureNotification(@NonNull Class<?> sensorTemperatureActivity,
                                              @NonNull SerializableList<TemperatureDto> temperatureList,
                                              @NonNull WeatherModel currentWeather) {
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
        Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), currentWeather.GetCondition().GetWallpaper());
        bitmap = _tools.GetCircleBitmap(bitmap);

        for (int index = 0; index < temperatureList.getSize(); index++) {
            body += temperatureList.getValue(index).GetArea() + ":"
                    + temperatureList.getValue(index).GetTemperatureString() + " | ";
        }
        body = body.substring(0, body.length() - 3);

        Intent intent = new Intent(_context, sensorTemperatureActivity);
        PendingIntent pendingIntent = PendingIntent.getActivity(_context, IDs.NOTIFICATION_TEMPERATURE * 2, intent, 0);

        NotificationManager notificationManager = (NotificationManager) _context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(_context);
        builder.setSmallIcon(R.drawable.temperature)
                .setLargeIcon(bitmap)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setTicker(body);
        Notification notification = builder.build();
        notificationManager.notify(IDs.NOTIFICATION_TEMPERATURE, notification);
    }

    @SuppressWarnings("deprecation")
    public void CreateSocketNotification(@NonNull SerializableList<WirelessSocketDto> wirelessSocketList) {
        if (!_sharedPrefController
                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SOCKET_NOTIFICATION)) {
            _logger.Warn("Not allowed to display socket notification!");
            return;
        }

        if (wirelessSocketList.getSize() > MAX_NOTIFICATION_SOCKETS) {
            _logger.Warn(
                    String.format("Too many sockets: %S! Cutting sockets to display!", wirelessSocketList.getSize()));

            SerializableList<WirelessSocketDto> tempWirelessSocketList = new SerializableList<>();
            for (int index = 0; index < wirelessSocketList.getSize(); index++) {
                WirelessSocketDto entry = wirelessSocketList.getValue(index);
                String key = entry.GetNotificationVisibilitySharedPrefKey();
                boolean isVisible = _sharedPrefController.LoadBooleanValueFromSharedPreferences(key);
                _logger.Info(String.format("Key of socket %s is %s and has value %s", entry.GetName(), key, isVisible));
                if (isVisible) {
                    _logger.Info(String.format("Adding socket %s to list!", entry.GetName()));
                    tempWirelessSocketList.addValue(entry);
                } else {
                    _logger.Warn(String.format("Socket %s is not visible!", entry.GetName()));
                }
            }
            wirelessSocketList = tempWirelessSocketList;
            _logger.Info(String.format("Size of visible wirelessSocketList is: %s!", wirelessSocketList.getSize()));

            if (wirelessSocketList.getSize() > MAX_NOTIFICATION_SOCKETS) {
                tempWirelessSocketList = new SerializableList<>();
                _logger.Warn(String.format("Still too many sockets: %s! Removing last entries!",
                        wirelessSocketList.getSize()));

                for (int index = 0; index < MAX_NOTIFICATION_SOCKETS; index++) {
                    WirelessSocketDto entry = wirelessSocketList.getValue(index);
                    tempWirelessSocketList.addValue(entry);
                }
                wirelessSocketList = tempWirelessSocketList;
                _logger.Info(String.format("Size of reduced wirelessSocketList is: %s!", wirelessSocketList.getSize()));
            }
        }
        _logger.Info(String.format("Size of wirelessSocketList is: %s!", wirelessSocketList.getSize()));

        int[] imageButtonArray = new int[]{R.id.socket_1_on, R.id.socket_1_off, R.id.socket_2_on, R.id.socket_2_off,
                R.id.socket_3_on, R.id.socket_3_off, R.id.socket_4_on, R.id.socket_4_off, R.id.socket_5_on,
                R.id.socket_5_off, R.id.socket_6_on, R.id.socket_6_off, R.id.socket_7_on, R.id.socket_7_off,
                R.id.socket_8_on, R.id.socket_8_off, R.id.socket_9_on, R.id.socket_9_off, R.id.socket_10_on,
                R.id.socket_10_off};
        boolean[] socketVisibility = new boolean[]{false, false, false, false, false, false, false, false, false,
                false};

        Intent[] socketIntents = new Intent[wirelessSocketList.getSize()];
        Bundle[] socketIntentData = new Bundle[wirelessSocketList.getSize()];
        PendingIntent[] socketPendingIntents = new PendingIntent[wirelessSocketList.getSize()];

        NotificationCompat.Action[] wearActions = new NotificationCompat.Action[wirelessSocketList.getSize()];

        Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.ic_launcher);
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(true).setBackground(bitmap);

        RemoteViews remoteViews = new RemoteViews(_context.getPackageName(), R.layout.notification_skeleton_socket);

        for (int index = 0; index < wirelessSocketList.getSize(); index++) {
            socketVisibility[index] = _sharedPrefController.LoadBooleanValueFromSharedPreferences(
                    wirelessSocketList.getValue(index).GetNotificationVisibilitySharedPrefKey());

            socketIntents[index] = new Intent(_context, SocketActionReceiver.class);

            socketIntentData[index] = new Bundle();
            socketIntentData[index].putString(Bundles.ACTION, SOCKET_SINGLE);
            socketIntentData[index].putSerializable(Bundles.SOCKET_DATA, wirelessSocketList.getValue(index));
            socketIntents[index].putExtras(socketIntentData[index]);

            socketPendingIntents[index] = PendingIntent.getBroadcast(_context, index * 1234, socketIntents[index],
                    PendingIntent.FLAG_UPDATE_CURRENT);

            int iconId = wirelessSocketList.getValue(index).GetDrawable();
            String text = ((wirelessSocketList.getValue(index).GetIsActivated()) ? "ON" : "OFF");
            Bitmap icon = BitmapFactory.decodeResource(_context.getResources(), iconId);

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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context);
        builder.setSmallIcon(R.drawable.ic_launcher).setContentTitle(_context.getString(R.string.app_name))
                .setContentText("Set Sockets").setTicker("Set Sockets");
        builder.extend(wearableExtender);
        for (int index = 0; index < wirelessSocketList.getSize(); index++) {
            if (socketVisibility[index]) {
                builder.addAction(wearActions[index]);
            }
        }

        // Hack for disabling all sockets at once!

        Intent allSocketsIntent = new Intent(_context, SocketActionReceiver.class);
        Bundle allSocketsData = new Bundle();
        allSocketsData.putString(Bundles.ACTION, SOCKET_ALL);
        allSocketsIntent.putExtras(allSocketsData);
        PendingIntent allSocketsPendingIntent;
        allSocketsPendingIntent = PendingIntent.getBroadcast(_context, 30, allSocketsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action_all_sockets_off = new NotificationCompat.Action.Builder(
                R.drawable.all_power_off, _context.getString(R.string.all_sockets_off), allSocketsPendingIntent)
                .build();
        remoteViews.setOnClickPendingIntent(R.id.socket_all_off, allSocketsPendingIntent);
        builder.addAction(action_all_sockets_off);

        // End Hack

        Notification notification = builder.build();
        notification.contentView = remoteViews;
        notification.bigContentView = remoteViews;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(_context);
        notificationManager.notify(IDs.NOTIFICATION_WEAR, notification);
    }

    public void CreateForecastWeatherNotification(@NonNull Class<?> receiverActivity,
                                                  int icon,
                                                  @NonNull String title,
                                                  @NonNull String body,
                                                  int id) {
        NotificationManager notificationManager = (NotificationManager) _context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(_context);
        builder.setSmallIcon(icon).setContentTitle(title).setContentText(body).setTicker(body);

        Intent intent = new Intent(_context, receiverActivity);
        PendingIntent pendingIntent = PendingIntent.getActivity(_context, id, intent, 0);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }

    @SuppressWarnings("deprecation")
    public void CreateSleepHeatingNotification() {
        if (!_sharedPrefController
                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION)) {
            _logger.Warn("Not allowed to display sleep notification!");
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.heating);
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(true).setBackground(bitmap);
        RemoteViews remoteViews = new RemoteViews(_context.getPackageName(), R.layout.notification_sleep);

        // Action for button enable heating only
        Intent enableHeatingIntent = new Intent(_context, HeatingReceiver.class);
        Bundle enableHeatingBundle = new Bundle();
        enableHeatingBundle.putString(Bundles.ACTION, HEATING_SINGLE);
        enableHeatingIntent.putExtras(enableHeatingBundle);
        PendingIntent enableHeatingPendingIntent = PendingIntent.getBroadcast(_context, 34154338, enableHeatingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action enableHeatingWearAction = new NotificationCompat.Action.Builder(
                R.drawable.compose_icon_socket, "Enable Heating", enableHeatingPendingIntent).build();
        remoteViews.setOnClickPendingIntent(R.id.enableHeatingOnly, enableHeatingPendingIntent);

        // Action for button enable sound only
        Intent enableSoundIntent = new Intent(_context, HeatingReceiver.class);
        Bundle enableSoundBundle = new Bundle();
        enableSoundBundle.putString(Bundles.ACTION, SOUND_SINGLE);
        enableSoundIntent.putExtras(enableSoundBundle);
        PendingIntent enableSoundPendingIntent = PendingIntent.getBroadcast(_context, 34154565, enableSoundIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action enableSoundWearAction = new NotificationCompat.Action.Builder(
                R.drawable.compose_icon_socket, "Enable Sound", enableSoundPendingIntent).build();
        remoteViews.setOnClickPendingIntent(R.id.enableSoundOnly, enableSoundPendingIntent);

        // Action for button enable heating and sound
        Intent enableHeatingAndSoundIntent = new Intent(_context, HeatingReceiver.class);
        Bundle enableHeatingAndSoundBundle = new Bundle();
        enableHeatingAndSoundBundle.putString(Bundles.ACTION, HEATING_AND_SOUND);
        enableHeatingAndSoundIntent.putExtras(enableHeatingAndSoundBundle);
        PendingIntent enableHeatingAndSoundPendingIntent = PendingIntent.getBroadcast(_context, 34154235,
                enableHeatingAndSoundIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action enableHeatingAndSoundWearAction = new NotificationCompat.Action.Builder(
                R.drawable.compose_icon_socket, "Heating And Sound", enableHeatingAndSoundPendingIntent).build();
        remoteViews.setOnClickPendingIntent(R.id.enableHeatingAndSound, enableHeatingAndSoundPendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context);
        builder.setSmallIcon(R.drawable.heating).setContentTitle("Relax in the bed!")
                .setContentText("Enjoying a warm bed!").setTicker("").extend(wearableExtender)
                .addAction(enableHeatingWearAction).addAction(enableSoundWearAction)
                .addAction(enableHeatingAndSoundWearAction);

        Notification notification = builder.build();
        notification.contentView = remoteViews;
        notification.bigContentView = remoteViews;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(_context);
        notificationManager.notify(IDs.NOTIFICATION_SLEEP, notification);
    }

    @SuppressWarnings("deprecation")
    public void CreateCameraNotification(@NonNull Class<?> receiverActivity) {
        if (!_sharedPrefController
                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_CAMERA_NOTIFICATION)) {
            _logger.Warn("Not allowed to display camera notification!");
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.camera);
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(true).setBackground(bitmap);
        RemoteViews remoteViews = new RemoteViews(_context.getPackageName(), R.layout.notification_camera);

        // Action for button show camera
        Intent showCameraIntent = new Intent(_context, receiverActivity);
        PendingIntent showCameraPendingIntent = PendingIntent.getActivity(_context, 34678743, showCameraIntent, 0);
        NotificationCompat.Action showCameraWearAction = new NotificationCompat.Action.Builder(R.drawable.camera,
                "Show camera", showCameraPendingIntent).build();
        remoteViews.setOnClickPendingIntent(R.id.showCamera, showCameraPendingIntent);

        // Action for button disable camera
        Intent disableCameraIntent = new Intent(_context, CameraReceiver.class);
        Bundle disableCameraBundle = new Bundle();
        disableCameraBundle.putString(Bundles.ACTION, DISABLE_CAMERA);
        disableCameraIntent.putExtras(disableCameraBundle);
        PendingIntent disableCameraPendingIntent = PendingIntent.getBroadcast(_context, 34678733, disableCameraIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action disableCameraWearAction = new NotificationCompat.Action.Builder(
                R.drawable.compose_icon_socket, "Disable camera", disableCameraPendingIntent).build();
        remoteViews.setOnClickPendingIntent(R.id.disableCamera, disableCameraPendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context);
        builder.setSmallIcon(R.drawable.camera).setContentTitle("Camera is active!").setContentText("Disable!")
                .setTicker("").extend(wearableExtender).addAction(showCameraWearAction)
                .addAction(disableCameraWearAction);

        Notification notification = builder.build();
        notification.contentView = remoteViews;
        notification.bigContentView = remoteViews;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(_context);
        notificationManager.notify(IDs.NOTIFICATION_CAMERA, notification);
    }
}
