package guepardoapps.lucahome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;

import guepardoapps.lucahome.basic.controller.AndroidSystemController;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.lucahome.service.MainService;

public class WIFIReceiver extends BroadcastReceiver {
    private static final String TAG = WIFIReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger logger = new Logger(TAG);
        logger.Debug("WIFIReceiver onReceive.\nContext is " + context.toString());

        String action = intent.getAction();
        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

            if (!(SupplicantState.isValidState(state) && state == SupplicantState.COMPLETED)) {
                logger.Warning("Not yet a valid connection!");
                return;
            }
        }

        logger.Information("valid connection!");

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
        } else {
            logger.Debug("We are NOT in the homeNetwork!");
            WirelessSocketService.getInstance().CloseNotification();
        }
    }
}