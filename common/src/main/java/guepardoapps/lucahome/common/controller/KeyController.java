package guepardoapps.lucahome.common.controller;

import android.app.Instrumentation;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"unused", "WeakerAccess"})
public class KeyController implements IKeyController {
    private static final String Tag = KeyController.class.getSimpleName();

    private static final int MaxTimeout = 3 * 1000;

    private Instrumentation _instrumentation;

    public KeyController() {
        _instrumentation = new Instrumentation();
    }

    @Override
    public void SimulateKeyPress(@NonNull final int[] keys, final int timeout) {
        if (timeout < 0) {
            Logger.getInstance().Warning(Tag, "Timeout cannot be negative!");
            return;
        }

        if (timeout > MaxTimeout) {
            Logger.getInstance().Warning(Tag, "Timeout is higher then MaxTimeout!");
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
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }
        };

        thread.start();
    }
}
