package guepardoapps.lucahome.common.controller;

import android.support.annotation.NonNull;

public interface IKeyController {
    void SimulateKeyPress(@NonNull final int[] keys, final int timeout) throws InterruptedException;
}
