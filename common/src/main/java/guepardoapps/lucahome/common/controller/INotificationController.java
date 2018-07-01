package guepardoapps.lucahome.common.controller;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket;

public interface INotificationController {
    String NotificationAction = "NotificationAction";

    String WirelessSocketsAll = "WirelessSocketsAll";
    String WirelessSocketSingle = "WirelessSocketSingle";
    String WirelessSocketData = "WirelessSocketData";

    String WirelessSwitchSingle = "WirelessSwitchSingle";
    String WirelessSwitchData = "WirelessSwitchData";

    void CreateSimpleNotification(int notificationId, int smallIcon, @NonNull Class<?> receiverActivity, Bitmap photo, @NonNull String title, @NonNull String body, boolean autoCancelable);

    void CreateCameraNotification(int notificationId, @NonNull Class<?> receiverActivity);

    void CreateWirelessSocketNotification(int notificationId, @NonNull MutableList<WirelessSocket> wirelessSocketList, @NonNull Class<?> receiverClass);

    void CreateWirelessSwitchNotification(int notificationId, MutableList<WirelessSwitch> wirelessSwitchList, @NonNull Class<?> receiverClass);

    void CreateTemperatureNotification(int notificationId, @NonNull Class<?> temperatureActivity, int icon, @NonNull String title, @NonNull String body, boolean autoCancelable);

    void CloseNotification(int notificationId);
}
