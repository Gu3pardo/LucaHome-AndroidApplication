package guepardoapps.lucahome.basic.controller;

import android.app.Instrumentation;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.utils.Logger;

public class KeyController {
    private static final String TAG = KeyController.class.getSimpleName();
    private Logger _logger;

    private static final int MAX_TIMEOUT = 3 * 1000;

    private Instrumentation _instrumentation;

    public KeyController() {
        _logger = new Logger(TAG);
        _logger.Debug("Created new " + TAG + "...");

        _instrumentation = new Instrumentation();
    }

    public void SimulateKeyPress(@NonNull final int[] keys, final int timeout) throws InterruptedException {
        _logger.Debug("SimulateKeyPress");

        if (timeout < 0) {
            _logger.Warning("Timeout cannot be negative!");
            return;
        }
        if (timeout > MAX_TIMEOUT) {
            _logger.Information("Timeout is higher the the maximum!");
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
