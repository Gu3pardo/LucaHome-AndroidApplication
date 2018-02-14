package guepardoapps.lucahome.common.controller;

import android.support.annotation.NonNull;

public interface ICommandController {
    boolean RebootDevice(int timeout);

    boolean ShutDownDevice(int timeout);

    boolean HasRoot();

    boolean PerformCommand(@NonNull String[] command);
}
