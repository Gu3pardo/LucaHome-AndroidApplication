package guepardoapps.library.lucahome.receiver.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.enums.LucaServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaNotificationController;
import guepardoapps.library.lucahome.controller.ServiceController;
import guepardoapps.library.lucahome.services.sockets.SocketActionService;

public class SocketActionReceiver extends BroadcastReceiver {

    private static final String TAG = SocketActionReceiver.class.getSimpleName();

    private ServiceController _serviceController;

    @Override
    public void onReceive(Context context, Intent intent) {
        LucaHomeLogger logger = new LucaHomeLogger(TAG);
        logger.Debug("Received new socket change!");

        Bundle details = intent.getExtras();
        String action = details.getString(Bundles.ACTION);
        if (action == null) {
            logger.Warn("Action is NULL!");
            action = "";
        } else {
            logger.Debug("Action is: " + action);
        }

        if (action.contains(LucaNotificationController.SOCKET_ALL)) {
            if (_serviceController == null) {
                _serviceController = new ServiceController(context);
            }
            _serviceController.StartRestService(
                    "SHOW_NOTIFICATION_SOCKET",
                    LucaServerAction.DEACTIVATE_ALL_SOCKETS.toString(),
                    "");
        } else if (action.contains(LucaNotificationController.SOCKET_SINGLE)) {
            WirelessSocketDto socket = (WirelessSocketDto) details.getSerializable(Bundles.SOCKET_DATA);
            logger.Debug(String.format(Locale.getDefault(), "socket: %s", socket));

            Intent serviceIntent = new Intent(context, SocketActionService.class);

            Bundle serviceData = new Bundle();
            serviceData.putSerializable(Bundles.SOCKET_DATA, socket);
            serviceIntent.putExtras(serviceData);

            context.startService(serviceIntent);
        } else {
            logger.Error("Action contains errors: " + action);
            Toasty.error(context, "Action contains errors: " + action, Toast.LENGTH_LONG).show();
        }
    }
}