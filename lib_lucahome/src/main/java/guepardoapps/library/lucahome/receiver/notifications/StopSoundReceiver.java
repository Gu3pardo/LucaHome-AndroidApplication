package guepardoapps.library.lucahome.receiver.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.ServiceController;

public class StopSoundReceiver extends BroadcastReceiver {

    private static final String TAG = StopSoundReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        new LucaHomeLogger(TAG).Debug("Received stop sounds!");
        new ServiceController(context).StopSound();
    }
}