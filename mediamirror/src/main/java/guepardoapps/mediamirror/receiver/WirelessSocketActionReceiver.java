package guepardoapps.mediamirror.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.service.WirelessSocketService;

public class WirelessSocketActionReceiver extends BroadcastReceiver {
    private static final String TAG = WirelessSocketActionReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle details = intent.getExtras();
        if (details == null) {
            Logger.getInstance().Error(TAG, "Details is null!");
            return;
        }

        String action = details.getString(NotificationController.ACTION);
        if (action == null) {
            Logger.getInstance().Error(TAG, "Action is null!");
            return;
        }

        switch (action) {
            case NotificationController.SOCKET_ALL:
                WirelessSocketService.getInstance().DeactivateAllWirelessSockets();
                break;

            case NotificationController.SOCKET_SINGLE:
                WirelessSocket socket = (WirelessSocket) details.getSerializable(NotificationController.SOCKET_DATA);

                if (socket == null) {
                    Logger.getInstance().Error(TAG, "Socket is null!");
                    Toasty.error(context, "Socket is null!", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    WirelessSocketService.getInstance().ChangeWirelessSocketState(socket);
                } catch (Exception exception) {
                    Logger.getInstance().Error(TAG, exception.getMessage());
                }
                break;

            default:
                Logger.getInstance().Error(TAG, "Action contains errors: " + action);
                Toasty.error(context, "Action contains errors: " + action, Toast.LENGTH_LONG).show();
                break;
        }
    }
}