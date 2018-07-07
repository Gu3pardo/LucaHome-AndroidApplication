package guepardoapps.lucahome.common.controller

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v4.app.NotificationCompat
import android.view.View
import android.widget.RemoteViews
import guepardoapps.lucahome.common.R
import guepardoapps.lucahome.common.extensions.common.circleBitmap
import guepardoapps.lucahome.common.extensions.wirelesssocket.getDrawable
import guepardoapps.lucahome.common.models.common.NotificationContent
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket
import guepardoapps.lucahome.common.models.wirelessswitch.WirelessSwitch
import guepardoapps.lucahome.common.utils.Logger

class NotificationController(@NonNull private val context: Context) : INotificationController {
    private val tag: String = NotificationController::class.java.simpleName

    private val channelId: String = "guepardoapps.lucahome.common"
    private val channelName: String = "GuepardoHome"
    private val channelDescription: String = "Notifications for application GuepardoHome"

    private val notificationAction = "NotificationAction"

    private val wirelessSocketData = "WirelessSocketData"
    private val wirelessSocketSingle = "WirelessSocketSingle"
    private val wirelessSocketAll = "WirelessSocketAll"

    private val notificationWirelessSocketMaxCount = 10
    private val notificationWirelessSwitchMaxCount = 5

    private var notificationManager: NotificationManager? = null

    init {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    override fun create(@NonNull notificationContent: NotificationContent) {
        var bitmap = BitmapFactory.decodeResource(context.resources, notificationContent.largeIconId)
        bitmap = bitmap.circleBitmap(bitmap.height, bitmap.width, Color.BLACK)

        val intent = Intent(context, notificationContent.receiver)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        @Suppress("DEPRECATION")
        val wearableExtender = Notification.WearableExtender().setHintHideIcon(true).setBackground(bitmap)

        val notification: Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = Notification.Builder(context, channelId)
                    .setContentTitle(notificationContent.title)
                    .setContentText(notificationContent.text)
                    .setSmallIcon(notificationContent.iconId)
                    .setLargeIcon(bitmap)
                    .setChannelId(channelId)
                    .setContentIntent(pendingIntent)
                    .extend(wearableExtender)
                    .setAutoCancel(notificationContent.cancelable)
                    .build()
        } else {
            @Suppress("DEPRECATION")
            notification = Notification.Builder(context)
                    .setContentTitle(notificationContent.title)
                    .setContentText(notificationContent.text)
                    .setSmallIcon(notificationContent.iconId)
                    .setLargeIcon(bitmap)
                    .setContentIntent(pendingIntent)
                    .extend(wearableExtender)
                    .setAutoCancel(notificationContent.cancelable)
                    .build()
        }

        notificationManager?.notify(notificationContent.id, notification)
    }

    override fun cameraNotification(@NonNull notificationContent: NotificationContent) {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.notification_camera)

        @Suppress("DEPRECATION")
        val wearableExtender = NotificationCompat.WearableExtender().setHintHideIcon(true).setBackground(bitmap)

        val remoteViews = RemoteViews(context.packageName, R.layout.notification_camera)

        // Action for button show camera
        val goToSecurityIntent = Intent(context, notificationContent.receiver)
        val goToSecurityPendingIntent = PendingIntent.getActivity(context, 0, goToSecurityIntent, 0)
        val goToSecurityWearAction = NotificationCompat.Action.Builder(R.drawable.notification_camera, "Go to security", goToSecurityPendingIntent).build()

        remoteViews.setOnClickPendingIntent(R.id.notification_camera_security_button, goToSecurityPendingIntent)

        val builder = NotificationCompat.Builder(context, channelId)
        builder.setSmallIcon(R.drawable.notification_camera)
                .setContentTitle(notificationContent.title)
                .setContentText(notificationContent.text)
                .setContent(remoteViews)
                .extend(wearableExtender)
                .addAction(goToSecurityWearAction)

        val notification = builder.build()

        notificationManager?.notify(notificationContent.id, notification)
    }

    override fun wirelessSocketNotification(id: Int, list: MutableList<WirelessSocket>, receiver: Class<*>) {
        val visibleList: List<WirelessSocket> = list.filter { value -> value.showInNotification }
        val checkedVisibleList: List<WirelessSocket> =
                if (visibleList.size > notificationWirelessSocketMaxCount)
                    visibleList.subList(0, notificationWirelessSocketMaxCount - 1)
                else
                    visibleList

        if (checkedVisibleList.isEmpty()) {
            Logger.instance.warning(tag, "CheckedVisibleList is empty!")
            return
        }

        val imageButtonArray = intArrayOf(
                R.id.wireless_socket_1_on, R.id.wireless_socket_1_off,
                R.id.wireless_socket_2_on, R.id.wireless_socket_2_off,
                R.id.wireless_socket_3_on, R.id.wireless_socket_3_off,
                R.id.wireless_socket_4_on, R.id.wireless_socket_4_off,
                R.id.wireless_socket_5_on, R.id.wireless_socket_5_off,
                R.id.wireless_socket_6_on, R.id.wireless_socket_6_off,
                R.id.wireless_socket_7_on, R.id.wireless_socket_7_off,
                R.id.wireless_socket_8_on, R.id.wireless_socket_8_off,
                R.id.wireless_socket_9_on, R.id.wireless_socket_9_off,
                R.id.wireless_socket_10_on, R.id.wireless_socket_10_off)

        val wearActions = arrayOfNulls<NotificationCompat.Action>(checkedVisibleList.size)

        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.main_image_wireless_sockets)
        @Suppress("DEPRECATION")
        val wearableExtender = NotificationCompat.WearableExtender().setHintHideIcon(true).setBackground(bitmap)

        val remoteViews = RemoteViews(context.packageName, R.layout.notification_wireless_socket)

        for (index in 0..(checkedVisibleList.size - 1)) {
            val wirelessSocket = checkedVisibleList[index]

            val bundle = Bundle()
            bundle.putString(notificationAction, wirelessSocketSingle)
            bundle.putSerializable(wirelessSocketData, wirelessSocket)

            val intent = Intent(context, receiver)
            intent.putExtras(bundle)

            val pendingIntent = PendingIntent.getBroadcast(context, index * 1234, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val iconId = wirelessSocket.getDrawable()
            val socketBitmap = BitmapFactory.decodeResource(context.resources, iconId)
            val text = if (wirelessSocket.state) "ON" else "OFF"

            wearActions[index] = NotificationCompat.Action.Builder(iconId, text, pendingIntent).build()

            if (wirelessSocket.state) {
                remoteViews.setViewVisibility(imageButtonArray[(index * 2)], View.GONE)

                remoteViews.setViewVisibility(imageButtonArray[(index * 2) + 1], View.VISIBLE)
                remoteViews.setOnClickPendingIntent(imageButtonArray[(index * 2) + 1], pendingIntent)
                remoteViews.setImageViewBitmap(imageButtonArray[(index * 2) + 1], socketBitmap)
            } else {
                remoteViews.setViewVisibility(imageButtonArray[(index * 2)], View.VISIBLE)
                remoteViews.setOnClickPendingIntent(imageButtonArray[(index * 2)], pendingIntent)
                remoteViews.setImageViewBitmap(imageButtonArray[(index * 2)], socketBitmap)

                remoteViews.setViewVisibility(imageButtonArray[(index * 2) + 1], View.GONE)
            }
        }

        for (index in (checkedVisibleList.size - 1)..notificationWirelessSocketMaxCount) {
            remoteViews.setViewVisibility(imageButtonArray[(index * 2)], View.GONE)
            remoteViews.setViewVisibility(imageButtonArray[(index * 2) + 1], View.GONE)
        }

        val builder = NotificationCompat.Builder(context, channelId)
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.application_name))
                .setContentText("Set Wireless Sockets")
                .setTicker("Set Wireless Sockets")
                .setContent(remoteViews)
                .extend(wearableExtender)
        for (wearAction in wearActions) {
            builder.addAction(wearAction)
        }

        // Hack for disabling all wireless sockets at once!

        val allWirelessSocketBundle = Bundle()
        allWirelessSocketBundle.putString(notificationAction, wirelessSocketAll)

        val allWirelessSocketIntent = Intent(context, receiver)
        allWirelessSocketIntent.putExtras(allWirelessSocketBundle)

        val allWirelessSocketPendingIntent = PendingIntent.getBroadcast(context, 30, allWirelessSocketIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.wireless_socket_all_off, allWirelessSocketPendingIntent)

        val allWirelessSocketWearAction = NotificationCompat.Action.Builder(R.drawable.wireless_socket_all_power_off, context.getString(R.string.notification_action_all_wireless_sockets_off), allWirelessSocketPendingIntent).build()
        builder.addAction(allWirelessSocketWearAction)

        // End Hack

        val notification = builder.build()
        notificationManager?.notify(id, notification)
    }

    override fun wirelessSwitchNotification(id: Int, list: MutableList<WirelessSwitch>, receiver: Class<*>) {
        // TODO
        /*
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
         */
    }

    override fun close(id: Int) {
        notificationManager?.cancel(id)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(channelId, channelName, importance)

        channel.description = channelDescription
        channel.enableLights(false)
        //channel.lightColor = Color.GREEN
        channel.enableVibration(false)
        //channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationManager?.createNotificationChannel(channel)
    }
}