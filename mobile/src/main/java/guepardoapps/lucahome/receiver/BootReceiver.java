package guepardoapps.lucahome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Locale;

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
        logger.Debug("BootReceiver onReceive.\nContext is " + context.toString());

        String action = intent.getAction();
        if (action == null) {
            logger.Warning("action is null!");
            return;
        }

        if (!action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            logger.Warning(String.format(Locale.getDefault(), "Action is invalid : %s", action));
            return;
        }

        if (new NetworkController(context).IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            logger.Debug("We are in the homeNetwork!");

            if (!new AndroidSystemController(context).IsServiceRunning(MainService.class)) {
                logger.Debug("MainService not running! Starting...");

                Intent startMainServiceIntent = new Intent(context, MainService.class);
                startMainServiceIntent.putExtra(MainService.MainServiceOnStartCommandBundle, true);

                context.startService(startMainServiceIntent);
            } else {
                logger.Debug("MainService running! Sending broadcast...");
                new BroadcastController(context).SendSimpleBroadcast(MainService.MainServiceStartDownloadAllBroadcast);
            }
        }
    }
}