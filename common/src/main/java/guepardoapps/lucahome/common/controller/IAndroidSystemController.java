package guepardoapps.lucahome.common.controller;

import android.content.ContentResolver;
import android.support.annotation.NonNull;

public interface IAndroidSystemController {
    boolean IsServiceRunning(@NonNull String serviceClassName);

    boolean IsServiceRunning(@NonNull Class<?> serviceClass);

    boolean IsBaseActivityRunning();

    boolean IsBaseActivityRunningPreAPI23();

    int CurrentAndroidApi();

    boolean CheckAPI23SystemPermission(int permissionRequestId);

    boolean IsAccessibilityServiceEnabled(@NonNull String serviceId);

    boolean MayReadNotifications(@NonNull ContentResolver contentResolver, @NonNull String packageName);
}
