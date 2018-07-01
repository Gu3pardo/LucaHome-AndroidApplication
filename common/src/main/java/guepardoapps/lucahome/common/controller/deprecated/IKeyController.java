package guepardoapps.lucahome.common.controller.deprecated;

import android.support.annotation.NonNull;

@SuppressWarnings({"unused"})
public interface IKeyController {
    void SimulateKeyPress(@NonNull final int[] keys, final int timeout) throws InterruptedException;
}
