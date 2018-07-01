package guepardoapps.lucahome.common.controller.deprecated;

import android.content.BroadcastReceiver;
import android.support.annotation.NonNull;

public interface IReceiverController {
    void RegisterReceiver(@NonNull BroadcastReceiver receiver, @NonNull String[] actions);

    void UnregisterReceiver(@NonNull BroadcastReceiver receiver);

    void Dispose();
}
