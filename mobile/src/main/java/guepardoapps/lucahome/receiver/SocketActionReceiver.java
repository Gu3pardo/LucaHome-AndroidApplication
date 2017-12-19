package guepardoapps.lucahome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Locale;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.service.WirelessSocketService;

public class SocketActionReceiver extends BroadcastReceiver {
    private static final String TAG = SocketActionReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger logger = new Logger(TAG);
        logger.Debug("Received new socket change!");

        Bundle details = intent.getExtras();
        if (details == null) {
            logger.Warning("Details is null!");
            return;
        }

        String action = details.getString(NotificationController.ACTION);
        if (action == null) {
            logger.Warning("Action is null!");
            return;
        }

        if (action.contains(NotificationController.SOCKET_ALL)) {
            WirelessSocketService.getInstance().DeactivateAllWirelessSockets();

        } else if (action.contains(NotificationController.SOCKET_SINGLE)) {
            WirelessSocket socket = (WirelessSocket) details.getSerializable(NotificationController.SOCKET_DATA);

            if (socket == null) {
                logger.Error("Socket is null!");
                Toasty.error(context, "Socket is null!", Toast.LENGTH_LONG).show();
                return;
            }

            logger.Debug(String.format(Locale.getDefault(), "socket: %s", socket));
            try {
                WirelessSocketService.getInstance().ChangeWirelessSocketState(socket);
            } catch (Exception exception) {
                logger.Error(exception.getMessage());
            }

        } else {
            logger.Error("Action contains errors: " + action);
            Toasty.error(context, "Action contains errors: " + action, Toast.LENGTH_LONG).show();
        }
    }
}