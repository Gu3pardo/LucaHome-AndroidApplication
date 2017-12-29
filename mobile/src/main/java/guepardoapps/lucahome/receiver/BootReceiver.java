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
        String action = intent.getAction();
        if (action == null) {
            Logger.getInstance().Warning(TAG, "action is null!");
            return;
        }

        if (!action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Logger.getInstance().Warning(TAG, String.format(Locale.getDefault(), "Action is invalid : %s", action));
            return;
        }

        if (new NetworkController(context).IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!new AndroidSystemController(context).IsServiceRunning(MainService.class)) {
                Intent startMainServiceIntent = new Intent(context, MainService.class);
                startMainServiceIntent.putExtra(MainService.MainServiceOnStartCommandBundle, true);
                context.startService(startMainServiceIntent);
            } else {
                new BroadcastController(context).SendSimpleBroadcast(MainService.MainServiceStartDownloadAllBroadcast);
            }
        }
    }
}