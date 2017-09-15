package guepardoapps.mediamirrorv2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();
    private static final int DELAY = 5 * 1000;

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();

        if (action == null) {
            return;
        }

        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                Intent startIntent = new Intent();
                startIntent.setClassName("guepardoapps.mediamirrorv2", "guepardoapps.mediamirrorv2.activity.FullscreenActivity");
                startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startIntent);
            }, DELAY);
        }
    }
}
