package guepardoapps.lucahome.common.controller;

import android.support.annotation.NonNull;

public interface IBroadcastController {
    void SendSimpleBroadcast(@NonNull String broadcast);

    void SendIntBroadcast(@NonNull String broadcast, @NonNull String bundleName, int data);

    void SendIntParcelableBroadcast(@NonNull String broadcast, @NonNull String[] bundleNamesInteger, @NonNull int[] dataInteger, @NonNull String[] bundleNamesParcelable, @NonNull Object[] dataParcelables);

    void SendIntArrayBroadcast(@NonNull String broadcast, @NonNull String[] bundleNames, @NonNull int[] data);

    void SendDoubleBroadcast(@NonNull String broadcast, @NonNull String bundleName, double data);

    void SendBooleanBroadcast(@NonNull String broadcast, @NonNull String bundleName, boolean data);

    void SendStringBroadcast(@NonNull String broadcast, @NonNull String bundleName, @NonNull String data);

    void SendStringArrayBroadcast(@NonNull String broadcast, @NonNull String[] bundleNames, @NonNull String[] data);

    void SendSerializableBroadcast(@NonNull String broadcast, @NonNull String bundleName, @NonNull Object model);

    void SendSerializableArrayBroadcast(@NonNull String broadcast, @NonNull String[] bundleNames, @NonNull Object[] models);
}
