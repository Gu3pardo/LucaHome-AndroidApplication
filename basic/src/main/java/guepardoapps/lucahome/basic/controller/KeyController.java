package guepardoapps.lucahome.basic.controller;

import android.app.Instrumentation;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class KeyController {
    private static final String TAG = KeyController.class.getSimpleName();

    private static final int MAX_TIMEOUT = 3 * 1000;

    private Instrumentation _instrumentation;

    public KeyController() {
        _instrumentation = new Instrumentation();
    }

    public void SimulateKeyPress(@NonNull final int[] keys, final int timeout) throws InterruptedException {
        if (timeout < 0) {
            Logger.getInstance().Warning(TAG, "Timeout cannot be negative!");
            return;
        }
        if (timeout > MAX_TIMEOUT) {
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    for (int key : keys) {
                        _instrumentation.sendKeyDownUpSync(key);
                        Thread.sleep(timeout);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }
}
