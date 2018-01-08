package guepardoapps.lucahome.common.controller;

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

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.WirelessSwitch;

@SuppressWarnings({"deprecation", "unused"})
public class NotificationController {
    private static final String TAG = NotificationController.class.getSimpleName();

    private static final int MAX_NOTIFICATION_SOCKETS = 10;

    public static final String ACTION = "Action";

    public static final String SOCKET_ALL = "All_Sockets";
    public static final String SOCKET_SINGLE = "Single_Socket";
    public static final String SOCKET_DATA = "SOCKET_DATA";

    public static final String SWITCH_ALL = "All_Switches";
    public static final String SWITCH_SINGLE = "Single_Switch";
    public static final String SWITCH_DATA = "SWITCH_DATA";

    protected Context _context;

    private NotificationManager _notificationManager;
    private SettingsController _settingsController;

    public NotificationController(@NonNull Context context) {
        _context = context;
        _notificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
        _settingsController = SettingsController.getInstance();
    }

    public void CreateSimpleNotification(
            int notificationId,
            int smallIcon,
            @NonNull Class<?> receiverActivity,
            Bitmap photo,
            @NonNull String title,
            @NonNull String body,
            boolean autoCancelable) {
        if (!_settingsController.IsBirthdayNotificationEnabled()) {
            Logger.getInstance().Warning(TAG, "Not allowed to display birthday notification!");
            return;
        }

        NotificationManager notificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Logger.getInstance().Error(TAG, "NotificationManager is null!");
            return;
        }

        Notification.Builder builder = new Notification.Builder(_context);

        Intent intent = new Intent(_context, receiverActivity);
        PendingIntent pendingIntent = PendingIntent.getActivity(_context, notificationId, intent, 0);

        builder.setSmallIcon(smallIcon)
                .setLargeIcon(photo)
                .setContentTitle(title)
                .setContentText(body)
                .setTicker(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(autoCancelable);

        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }

    public void CreateCameraNotification(
            int notificationId,
            @NonNull Class<?> receiverActivity) {

        if (!_settingsController.IsCameraNotificationEnabled()) {
            Logger.getInstance().Warning(TAG, "Not allowed to display camera notification!");
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.camera);
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().setHintHideIcon(true).setBackground(bitmap);
        RemoteViews remoteViews = new RemoteViews(_context.getPackageName(), R.layout.notification_camera);

        // Action for button show camera
        Intent goToSecurityIntent = new Intent(_context, receiverActivity);
        PendingIntent goToSecurityPendingIntent = PendingIntent.getActivity(_context, 34678743, goToSecurityIntent, 0);
        NotificationCompat.Action goToSecurityWearAction = new NotificationCompat.Action.Builder(R.drawable.camera, "Go to security", goToSecurityPendingIntent).build();

        remoteViews.setOnClickPendingIntent(R.id.goToSecurity, goToSecurityPendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context);
        builder.setSmallIcon(R.drawable.camera)
                .setContentTitle("Camera is active!")
                .setContentText("Go to security!")
                .setTicker("")
                .extend(wearableExtender)
                .addAction(goToSecurityWearAction);

        Notification notification = builder.build();
        notification.contentView = remoteViews;
        notification.bigContentView = remoteViews;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(_context);
        notificationManager.notify(notificationId, notification);
    }

    public void CreateSocketNotification(int notificationId, @NonNull SerializableList<WirelessSocket> wirelessSocketList, @NonNull Class<?> receiverClass) {
        if (!_settingsController.IsSocketNotificationEnabled()) {
            Logger.getInstance().Warning(TAG, "Not allowed to display socket notification!");
            return;
        }

        if (wirelessSocketList.getSize() > MAX_NOTIFICATION_SOCKETS) {
            Logger.getInstance().Warning(TAG, String.format(Locale.getDefault(), "Too many sockets: %S! Cutting sockets to display!", wirelessSocketList.getSize()));

            SerializableList<WirelessSocket> tempWirelessSocketList = new SerializableList<>();
            for (int index = 0; index < wirelessSocketList.getSize(); index++) {
                WirelessSocket entry = wirelessSocketList.getValue(index);
                boolean isVisible = _settingsController.IsWirelessSocketVisible(entry);
                if (isVisible) {
                    tempWirelessSocketList.addValue(entry);
                }
            }

            wirelessSocketList = tempWirelessSocketList;

            if (wirelessSocketList.getSize() > MAX_NOTIFICATION_SOCKETS) {
                tempWirelessSocketList = new SerializableList<>();
                for (int index = 0; index < MAX_NOTIFICATION_SOCKETS; index++) {
                    WirelessSocket entry = wirelessSocketList.getValue(index);
                    tempWirelessSocketList.addValue(entry);
                }
                wirelessSocketList = tempWirelessSocketList;
            }
        }

        int[] imageButtonArray = new int[]{
                R.id.socket_1_on, R.id.socket_1_off,
                R.id.socket_2_on, R.id.socket_2_off,
                R.id.socket_3_on, R.id.socket_3_off,
                R.id.socket_4_on, R.id.socket_4_off,
                R.id.socket_5_on, R.id.socket_5_off,
                R.id.socket_6_on, R.id.socket_6_off,
                R.id.socket_7_on, R.id.socket_7_off,
                R.id.socket_8_on, R.id.socket_8_off,
                R.id.socket_9_on, R.id.socket_9_off,
                R.id.socket_10_on, R.id.socket_10_off};

        boolean[] socketVisibility = new boolean[]{false, false, false, false, false, false, false, false, false, false};

        Intent[] socketIntents = new Intent[wirelessSocketList.getSize()];
        Bundle[] socketIntentData = new Bundle[wirelessSocketList.getSize()];
        PendingIntent[] socketPendingIntents = new PendingIntent[wirelessSocketList.getSize()];

        NotificationCompat.Action[] wearActions = new NotificationCompat.Action[wirelessSocketList.getSize()];

        Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.main_image_sockets);
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().setHintHideIcon(true).setBackground(bitmap);

        RemoteViews remoteViews = new RemoteViews(_context.getPackageName(), R.layout.notification_wireless_socket);

        for (int index = 0; index < wirelessSocketList.getSize(); index++) {
            socketVisibility[index] = _settingsController.IsWirelessSocketVisible(wirelessSocketList.getValue(index));

            socketIntents[index] = new Intent(_context, receiverClass);

            socketIntentData[index] = new Bundle();
            socketIntentData[index].putString(ACTION, SOCKET_SINGLE);
            socketIntentData[index].putSerializable(SOCKET_DATA, wirelessSocketList.getValue(index));
            socketIntents[index].putExtras(socketIntentData[index]);

            socketPendingIntents[index] = PendingIntent.getBroadcast(_context, index * 1234, socketIntents[index],
                    PendingIntent.FLAG_UPDATE_CURRENT);

            int iconId = wirelessSocketList.getValue(index).GetDrawable();
            String text = ((wirelessSocketList.getValue(index).IsActivated()) ? "ON" : "OFF");
            Bitmap icon = BitmapFactory.decodeResource(_context.getResources(), iconId);

            wearActions[index] = new NotificationCompat.Action.Builder(iconId, text, socketPendingIntents[index])
                    .build();

            if (socketVisibility[index]) {
                if (wirelessSocketList.getValue(index).IsActivated()) {
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
        builder.setSmallIcon(R.mipmap.ic_launcher).setContentTitle(_context.getString(R.string.application_name)).setContentText("Set Sockets").setTicker("Set Sockets");
        builder.extend(wearableExtender);
        for (int index = 0; index < wirelessSocketList.getSize(); index++) {
            if (socketVisibility[index]) {
                builder.addAction(wearActions[index]);
            }
        }

        // Hack for disabling all sockets at once!

        Bundle allSocketsData = new Bundle();
        allSocketsData.putString(ACTION, SOCKET_ALL);

        Intent allSocketsIntent = new Intent(_context, receiverClass);
        allSocketsIntent.putExtras(allSocketsData);

        PendingIntent allSocketsPendingIntent = PendingIntent.getBroadcast(_context, 30, allSocketsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action_all_sockets_off = new NotificationCompat.Action.Builder(R.drawable.all_power_off, _context.getString(R.string.all_sockets_off), allSocketsPendingIntent).build();

        remoteViews.setOnClickPendingIntent(R.id.socket_all_off, allSocketsPendingIntent);
        builder.addAction(action_all_sockets_off);

        // End Hack

        Notification notification = builder.build();
        notification.contentView = remoteViews;
        notification.bigContentView = remoteViews;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(_context);
        notificationManager.notify(notificationId, notification);
    }

    public void CreateWirelessSwitchNotification(int notificationId, SerializableList<WirelessSwitch> wirelessSwitchList, Class<?> receiverClass) {
        // TODO implement method
        Logger.getInstance().Error(TAG, "TODO: Implement method!");
    }

    public void CreateTemperatureNotification(
            int notificationId,
            @NonNull Class<?> temperatureActivity,
            int icon,
            @NonNull String title,
            @NonNull String body,
            boolean autoCancelable) {
        if (!_settingsController.IsTemperatureNotificationEnabled()) {
            Logger.getInstance().Warning(TAG, "Not allowed to display temperature notification!");
            return;
        }

        NotificationManager notificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Logger.getInstance().Error(TAG, "NotificationManager is null!");
            return;
        }

        Notification.Builder builder = new Notification.Builder(_context);

        Intent intent = new Intent(_context, temperatureActivity);
        PendingIntent pendingIntent = PendingIntent.getActivity(_context, notificationId, intent, 0);

        Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.weather_wallpaper_dummy);
        bitmap = Tools.GetCircleBitmap(bitmap);

        builder.setSmallIcon(icon)
                .setLargeIcon(bitmap)
                .setContentTitle(title)
                .setContentText(body)
                .setTicker(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(autoCancelable);

        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }

    public void CloseNotification(int notificationId) {
        _notificationManager.cancel(notificationId);
    }
}
