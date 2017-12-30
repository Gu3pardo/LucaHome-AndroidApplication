package guepardoapps.lucahome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.service.WirelessSwitchService;

public class SwitchActionReceiver extends BroadcastReceiver {
    private static final String TAG = SwitchActionReceiver.class.getSimpleName();

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
            case NotificationController.SWITCH_ALL:
                WirelessSwitchService.getInstance().ToggleAllWirelessSwitches();
                break;

            case NotificationController.SWITCH_SINGLE:
                WirelessSwitch wirelessSwitch = (WirelessSwitch) details.getSerializable(NotificationController.SWITCH_DATA);

                if (wirelessSwitch == null) {
                    Logger.getInstance().Error(TAG, "WirelessSwitch is null!");
                    Toasty.error(context, "WirelessSwitch is null!", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    WirelessSwitchService.getInstance().ToggleWirelessSwitch(wirelessSwitch);
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