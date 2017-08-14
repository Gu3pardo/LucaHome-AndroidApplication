package guepardoapps.lucahome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import guepardoapps.lucahome.basic.controller.AndroidSystemController;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.service.MainService;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger logger = new Logger(TAG);
        logger.Debug("WIFIReceiver onReceive.\nContext is " + context.toString());

        AndroidSystemController androidSystemController = new AndroidSystemController(context);
        BroadcastController broadcastController = new BroadcastController(context);
        NetworkController networkController = new NetworkController(context);

        if (networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            logger.Debug("We are in the homeNetwork!");

            if (!androidSystemController.IsServiceRunning(MainService.class)) {
                context.startService(new Intent(context, MainService.class));
            }

            broadcastController.SendSimpleBroadcast(MainService.MainServiceStartDownloadAllBroadcast);
        }
    }
}