package guepardoapps.lucahome.receiver;

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

public class SocketActionReceiver extends BroadcastReceiver {
    private static final String TAG = SocketActionReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle details = intent.getExtras();
        if (details == null) {
            Logger.getInstance().Warning(TAG, "Details is null!");
            return;
        }

        String action = details.getString(NotificationController.ACTION);
        if (action == null) {
            Logger.getInstance().Warning(TAG, "Action is null!");
            return;
        }

        if (action.contains(NotificationController.SOCKET_ALL)) {
            WirelessSocketService.getInstance().DeactivateAllWirelessSockets();

        } else if (action.contains(NotificationController.SOCKET_SINGLE)) {
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

        } else {
            Logger.getInstance().Error(TAG, "Action contains errors: " + action);
            Toasty.error(context, "Action contains errors: " + action, Toast.LENGTH_LONG).show();
        }
    }
}