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
import android.support.annotation.NonNull
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.widget.RemoteViews
import guepardoapps.lucahome.common.R
import guepardoapps.lucahome.common.extensions.circleBitmap
import guepardoapps.lucahome.common.models.common.NotificationContent
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket
import guepardoapps.lucahome.common.models.wirelessswitch.WirelessSwitch

class NotificationController(@NonNull private val context: Context) : INotificationController {
    private val channelId: String = "guepardoapps.lucahome.common"
    private val channelName: String = "GuepardoHome"
    private val channelDescription: String = "Notifications for application GuepardoHome"

    private var notificationManager: NotificationManager? = null

    init {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @Suppress("DEPRECATION")
    override fun create(@NonNull notificationContent: NotificationContent) {
        var bitmap = BitmapFactory.decodeResource(context.resources, notificationContent.largeIconId)
        bitmap = bitmap.circleBitmap(bitmap.height, bitmap.width, Color.BLACK)

        val intent = Intent(context, notificationContent.receiver)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification: Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = Notification.Builder(context, channelId)
                    .setContentTitle(notificationContent.title)
                    .setContentText(notificationContent.text)
                    .setSmallIcon(notificationContent.iconId)
                    .setLargeIcon(bitmap)
                    .setChannelId(channelId)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(notificationContent.cancelable)
                    .build()
        } else {
            notification = Notification.Builder(context)
                    .setContentTitle(notificationContent.title)
                    .setContentText(notificationContent.text)
                    .setSmallIcon(notificationContent.iconId)
                    .setLargeIcon(bitmap)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(notificationContent.cancelable)
                    .build()
        }

        notificationManager?.notify(notificationContent.id, notification)
    }

    override fun cameraNotification(@NonNull notificationContent: NotificationContent) {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.notification_camera)
        val wearableExtender = NotificationCompat.WearableExtender().setHintHideIcon(true).setBackground(bitmap)
        val remoteViews = RemoteViews(context.packageName), R.layout.notification_camera)

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

        // TOD replace notificationmanage
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationContent.id, notification)
    }

    override fun wirelessSocketNotification(id: Int, list: MutableList<WirelessSocket>, receiver: Class<*>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun wirelessSwitchNotification(id: Int, list: MutableList<WirelessSwitch>, receiver: Class<*>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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