package guepardoapps.lucahome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Handler;

import guepardoapps.lucahome.basic.controller.AndroidSystemController;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.service.TemperatureService;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.lucahome.service.MainService;

public class WIFIReceiver extends BroadcastReceiver {
    private static final String TAG = WIFIReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            if (!(SupplicantState.isValidState(state) && state == SupplicantState.COMPLETED)) {
                Logger.getInstance().Warning(TAG, "Not yet a valid connection!");
                return;
            }
        }

        new Handler().postDelayed(() -> {
            BroadcastController broadcastController = new BroadcastController(context);
            if (new NetworkController(context).IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
                if (!new AndroidSystemController(context).IsServiceRunning(MainService.class)) {
                    Intent startMainServiceIntent = new Intent(context, MainService.class);
                    startMainServiceIntent.putExtra(MainService.MainServiceOnStartCommandBundle, true);
                    context.startService(startMainServiceIntent);
                } else {
                    broadcastController.SendSimpleBroadcast(MainService.MainServiceStartDownloadAllBroadcast);
                }
                broadcastController.SendSimpleBroadcast(NetworkController.WIFIReceiverInHomeNetworkBroadcast);
            } else {
                TemperatureService.getInstance().CloseNotification();
                WirelessSocketService.getInstance().CloseNotification();
                broadcastController.SendSimpleBroadcast(NetworkController.WIFIReceiverNoHomeNetworkBroadcast);
            }
        }, 10 * 1000);
    }
}