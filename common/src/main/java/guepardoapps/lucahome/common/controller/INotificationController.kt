package guepardoapps.lucahome.common.controller

import android.support.annotation.NonNull
import guepardoapps.lucahome.common.models.common.NotificationContent
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket
import guepardoapps.lucahome.common.models.wirelessswitch.WirelessSwitch

interface INotificationController {
    fun create(@NonNull notificationContent: NotificationContent)

    fun cameraNotification(@NonNull notificationContent: NotificationContent)
    fun wirelessSocketNotification(id: Int, list: MutableList<WirelessSocket>, receiver: Class<*>)
    fun wirelessSwitchNotification(id: Int, list: MutableList<WirelessSwitch>, receiver: Class<*>)

    fun close(id: Int)
}