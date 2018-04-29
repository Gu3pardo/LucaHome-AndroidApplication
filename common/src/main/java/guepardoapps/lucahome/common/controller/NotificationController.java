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

import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.utils.BitmapHelper;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class NotificationController implements INotificationController {
    private static final String Tag = NotificationController.class.getSimpleName();

    private static final int MaxNotificationWirelessSockets = 10;
    private static final int MaxNotificationWirelessSwitches = 5;

    private static final String ChannelId = "guepardoapps.lucahome.common.controller.NotificationController.23291";

    protected Context _context;
    private NotificationManager _notificationManager;

    public NotificationController(@NonNull Context context) {
        _context = context;
        _notificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void CreateSimpleNotification(int notificationId, int smallIcon, @NonNull Class<?> receiverActivity, Bitmap photo, @NonNull String title, @NonNull String body, boolean autoCancelable) {
        if (!SettingsController.getInstance().IsBirthdayNotificationEnabled()) {
            Logger.getInstance().Warning(Tag, "Not allowed to display birthday notification!");
            return;
        }

        NotificationManager notificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Logger.getInstance().Error(Tag, "NotificationManager is null!");
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

    @Override
    public void CreateCameraNotification(int notificationId, @NonNull Class<?> receiverActivity) {
        if (!SettingsController.getInstance().IsCameraNotificationEnabled()) {
            Logger.getInstance().Warning(Tag, "Not allowed to display camera notification!");
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.notification_camera);
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().setHintHideIcon(true).setBackground(bitmap);
        RemoteViews remoteViews = new RemoteViews(_context.getPackageName(), R.layout.notification_camera);

        // Action for button show camera
        Intent goToSecurityIntent = new Intent(_context, receiverActivity);
        PendingIntent goToSecurityPendingIntent = PendingIntent.getActivity(_context, 34678743, goToSecurityIntent, 0);
        NotificationCompat.Action goToSecurityWearAction = new NotificationCompat.Action.Builder(R.drawable.notification_camera, "Go to security", goToSecurityPendingIntent).build();

        remoteViews.setOnClickPendingIntent(R.id.notification_camera_security_button, goToSecurityPendingIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context, ChannelId);
        builder.setSmallIcon(R.drawable.notification_camera)
                .setContentTitle("Camera is active!")
                .setContentText("Go to security!")
                .setContent(remoteViews)
                .setTicker("")
                .extend(wearableExtender)
                .addAction(goToSecurityWearAction);

        Notification notification = builder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(_context);
        notificationManager.notify(notificationId, notification);
    }

    @Override
    public void CreateWirelessSocketNotification(int notificationId, @NonNull ArrayList<WirelessSocket> wirelessSocketList, @NonNull Class<?> receiverClass) {
        if (!SettingsController.getInstance().IsWirelessSocketNotificationEnabled()) {
            Logger.getInstance().Warning(Tag, "Not allowed to display wireless socket notification!");
            return;
        }

        if (wirelessSocketList.size() > MaxNotificationWirelessSockets) {
            Logger.getInstance().Warning(Tag, String.format(Locale.getDefault(), "Too many wireless sockets: %d! Cutting wireless sockets to display!", wirelessSocketList.size()));

            ArrayList<WirelessSocket> tempWirelessSocketList = new ArrayList<>();
            for (int index = 0; index < wirelessSocketList.size(); index++) {
                WirelessSocket entry = wirelessSocketList.get(index);
                boolean isVisible = SettingsController.getInstance().IsWirelessSocketVisible(entry);
                if (isVisible) {
                    tempWirelessSocketList.add(entry);
                }
            }

            wirelessSocketList = tempWirelessSocketList;

            if (wirelessSocketList.size() > MaxNotificationWirelessSockets) {
                tempWirelessSocketList = new ArrayList<>();
                for (int index = 0; index < MaxNotificationWirelessSockets; index++) {
                    WirelessSocket entry = wirelessSocketList.get(index);
                    tempWirelessSocketList.add(entry);
                }
                wirelessSocketList = tempWirelessSocketList;
            }
        }

        int[] imageButtonArray = new int[]{
                R.id.wireless_socket_1_on, R.id.wireless_socket_1_off,
                R.id.wireless_socket_2_on, R.id.wireless_socket_2_off,
                R.id.wireless_socket_3_on, R.id.wireless_socket_3_off,
                R.id.wireless_socket_4_on, R.id.wireless_socket_4_off,
                R.id.wireless_socket_5_on, R.id.wireless_socket_5_off,
                R.id.wireless_socket_6_on, R.id.wireless_socket_6_off,
                R.id.wireless_socket_7_on, R.id.wireless_socket_7_off,
                R.id.wireless_socket_8_on, R.id.wireless_socket_8_off,
                R.id.wireless_socket_9_on, R.id.wireless_socket_9_off,
                R.id.wireless_socket_10_on, R.id.wireless_socket_10_off};

        boolean[] socketVisibility = new boolean[]{false, false, false, false, false, false, false, false, false, false};

        Intent[] socketIntents = new Intent[wirelessSocketList.size()];
        Bundle[] socketIntentData = new Bundle[wirelessSocketList.size()];
        PendingIntent[] socketPendingIntents = new PendingIntent[wirelessSocketList.size()];

        NotificationCompat.Action[] wearActions = new NotificationCompat.Action[wirelessSocketList.size()];

        Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.main_image_wireless_sockets);
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().setHintHideIcon(true).setBackground(bitmap);

        RemoteViews remoteViews = new RemoteViews(_context.getPackageName(), R.layout.notification_wireless_socket);

        for (int index = 0; index < wirelessSocketList.size(); index++) {
            socketVisibility[index] = SettingsController.getInstance().IsWirelessSocketVisible(wirelessSocketList.get(index));

            socketIntents[index] = new Intent(_context, receiverClass);

            socketIntentData[index] = new Bundle();
            socketIntentData[index].putString(NotificationAction, WirelessSocketSingle);
            socketIntentData[index].putSerializable(WirelessSocketData, wirelessSocketList.get(index));
            socketIntents[index].putExtras(socketIntentData[index]);

            socketPendingIntents[index] = PendingIntent.getBroadcast(_context, index * 1234, socketIntents[index], PendingIntent.FLAG_UPDATE_CURRENT);

            int iconId = wirelessSocketList.get(index).GetDrawable();
            String text = ((wirelessSocketList.get(index).GetState()) ? "ON" : "OFF");
            Bitmap icon = BitmapFactory.decodeResource(_context.getResources(), iconId);

            wearActions[index] = new NotificationCompat.Action.Builder(iconId, text, socketPendingIntents[index]).build();

            if (socketVisibility[index]) {
                if (wirelessSocketList.get(index).GetState()) {
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

        for (int visibilityIndex = 0; visibilityIndex < MaxNotificationWirelessSockets; visibilityIndex++) {
            if (!socketVisibility[visibilityIndex]) {
                remoteViews.setViewVisibility(imageButtonArray[(visibilityIndex * 2)], View.GONE);
                remoteViews.setViewVisibility(imageButtonArray[(visibilityIndex * 2) + 1], View.GONE);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context, ChannelId);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(_context.getString(R.string.application_name))
                .setContentText("Set Wireless Sockets")
                .setContent(remoteViews)
                .setTicker("Set Wireless Sockets");
        builder.extend(wearableExtender);
        for (int index = 0; index < wirelessSocketList.size(); index++) {
            if (socketVisibility[index]) {
                builder.addAction(wearActions[index]);
            }
        }

        // Hack for disabling all sockets at once!

        Bundle allSocketsData = new Bundle();
        allSocketsData.putString(NotificationAction, WirelessSocketsAll);

        Intent allSocketsIntent = new Intent(_context, receiverClass);
        allSocketsIntent.putExtras(allSocketsData);

        PendingIntent allSocketsPendingIntent = PendingIntent.getBroadcast(_context, 30, allSocketsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action_all_sockets_off = new NotificationCompat.Action.Builder(R.drawable.wireless_socket_all_power_off, _context.getString(R.string.notification_action_all_wireless_sockets_off), allSocketsPendingIntent).build();

        remoteViews.setOnClickPendingIntent(R.id.wireless_socket_all_off, allSocketsPendingIntent);
        builder.addAction(action_all_sockets_off);

        // End Hack

        Notification notification = builder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(_context);
        notificationManager.notify(notificationId, notification);
    }

    @Override
    public void CreateWirelessSwitchNotification(int notificationId, ArrayList<WirelessSwitch> wirelessSwitchList, @NonNull Class<?> receiverClass) {
        if (!SettingsController.getInstance().IsWirelessSwitchNotificationEnabled()) {
            Logger.getInstance().Warning(Tag, "Not allowed to display wireless switch notification!");
            return;
        }

        if (wirelessSwitchList.size() > MaxNotificationWirelessSwitches) {
            Logger.getInstance().Warning(Tag, String.format(Locale.getDefault(), "Too many wireless switches: %d! Cutting wireless switches to display!", wirelessSwitchList.size()));

            ArrayList<WirelessSwitch> tempWirelessSwitchList = new ArrayList<>();
            for (int index = 0; index < wirelessSwitchList.size(); index++) {
                WirelessSwitch entry = wirelessSwitchList.get(index);
                boolean isVisible = SettingsController.getInstance().IsWirelessSwitchVisible(entry);
                if (isVisible) {
                    tempWirelessSwitchList.add(entry);
                }
            }

            wirelessSwitchList = tempWirelessSwitchList;

            if (wirelessSwitchList.size() > MaxNotificationWirelessSwitches) {
                tempWirelessSwitchList = new ArrayList<>();
                for (int index = 0; index < MaxNotificationWirelessSwitches; index++) {
                    WirelessSwitch entry = wirelessSwitchList.get(index);
                    tempWirelessSwitchList.add(entry);
                }
                wirelessSwitchList = tempWirelessSwitchList;
            }
        }

        int[] buttonArray = new int[]{
                R.id.wireless_switch_button_1_on, R.id.wireless_switch_button_1_off,
                R.id.wireless_switch_button_2_on, R.id.wireless_switch_button_2_off,
                R.id.wireless_switch_button_3_on, R.id.wireless_switch_button_3_off,
                R.id.wireless_switch_button_4_on, R.id.wireless_switch_button_4_off,
                R.id.wireless_switch_button_5_on, R.id.wireless_switch_button_5_off};

        boolean[] switchVisibility = new boolean[]{false, false, false, false, false};

        Intent[] switchIntents = new Intent[wirelessSwitchList.size()];
        Bundle[] switchIntentData = new Bundle[wirelessSwitchList.size()];
        PendingIntent[] switchPendingIntents = new PendingIntent[wirelessSwitchList.size()];

        NotificationCompat.Action[] wearActions = new NotificationCompat.Action[wirelessSwitchList.size()];

        Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.main_image_wireless_switches);
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().setHintHideIcon(true).setBackground(bitmap);

        RemoteViews remoteViews = new RemoteViews(_context.getPackageName(), R.layout.notification_wireless_switch);

        for (int index = 0; index < wirelessSwitchList.size(); index++) {
            WirelessSwitch wirelessSwitch = wirelessSwitchList.get(index);

            switchVisibility[index] = SettingsController.getInstance().IsWirelessSwitchVisible(wirelessSwitch);

            switchIntents[index] = new Intent(_context, receiverClass);

            switchIntentData[index] = new Bundle();
            switchIntentData[index].putString(NotificationAction, WirelessSwitchSingle);
            switchIntentData[index].putSerializable(WirelessSwitchData, wirelessSwitch);
            switchIntents[index].putExtras(switchIntentData[index]);

            switchPendingIntents[index] = PendingIntent.getBroadcast(_context, index * 2468, switchIntents[index], PendingIntent.FLAG_UPDATE_CURRENT);

            String text = String.format(Locale.getDefault(), "%s is %s", wirelessSwitch.GetName(), wirelessSwitch.GetState() ? "ON" : "OFF");

            int iconId = wirelessSwitch.GetAction() ? R.drawable.wireless_switch_on : R.drawable.wireless_switch_off;
            wearActions[index] = new NotificationCompat.Action.Builder(iconId, text, switchPendingIntents[index]).build();

            if (switchVisibility[index]) {
                if (wirelessSwitchList.get(index).GetState()) {
                    remoteViews.setViewVisibility(buttonArray[(index * 2)], View.GONE);
                    remoteViews.setViewVisibility(buttonArray[(index * 2) + 1], View.VISIBLE);
                    remoteViews.setOnClickPendingIntent(buttonArray[(index * 2) + 1], switchPendingIntents[index]);
                } else {
                    remoteViews.setViewVisibility(buttonArray[(index * 2)], View.VISIBLE);
                    remoteViews.setViewVisibility(buttonArray[(index * 2) + 1], View.GONE);
                    remoteViews.setOnClickPendingIntent(buttonArray[(index * 2)], switchPendingIntents[index]);
                }
            }
        }

        for (int visibilityIndex = 0; visibilityIndex < MaxNotificationWirelessSwitches; visibilityIndex++) {
            if (!switchVisibility[visibilityIndex]) {
                remoteViews.setViewVisibility(buttonArray[(visibilityIndex * 2)], View.GONE);
                remoteViews.setViewVisibility(buttonArray[(visibilityIndex * 2) + 1], View.GONE);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context, ChannelId);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(_context.getString(R.string.application_name))
                .setContentText("Toggle Switches")
                .setTicker("Toggle Switches")
                .setContent(remoteViews);
        builder.extend(wearableExtender);
        for (int index = 0; index < wirelessSwitchList.size(); index++) {
            if (switchVisibility[index]) {
                builder.addAction(wearActions[index]);
            }
        }

        Notification notification = builder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(_context);
        notificationManager.notify(notificationId, notification);
    }

    @Override
    public void CreateTemperatureNotification(int notificationId, @NonNull Class<?> temperatureActivity, int icon, @NonNull String title, @NonNull String body, boolean autoCancelable) {
        if (!SettingsController.getInstance().IsTemperatureNotificationEnabled()) {
            Logger.getInstance().Warning(Tag, "Not allowed to display temperature notification!");
            return;
        }

        NotificationManager notificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Logger.getInstance().Error(Tag, "NotificationManager is null!");
            return;
        }

        Notification.Builder builder = new Notification.Builder(_context);

        Intent intent = new Intent(_context, temperatureActivity);
        PendingIntent pendingIntent = PendingIntent.getActivity(_context, notificationId, intent, 0);

        Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.main_image_temperature);
        bitmap = BitmapHelper.GetCircleBitmap(bitmap);

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

    @Override
    public void CloseNotification(int notificationId) {
        _notificationManager.cancel(notificationId);
    }
}
