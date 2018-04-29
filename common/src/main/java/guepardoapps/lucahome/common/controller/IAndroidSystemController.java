package guepardoapps.lucahome.common.controller;

import android.content.ContentResolver;
import android.support.annotation.NonNull;

@SuppressWarnings({"unused"})
public interface IAndroidSystemController {
    boolean IsServiceRunning(@NonNull String serviceClassName);

    boolean IsServiceRunning(@NonNull Class<?> serviceClass);

    boolean IsBaseActivityRunning();

    int CurrentAndroidApi();

    boolean CheckAPI23SystemPermission(int permissionRequestId);

    boolean IsAccessibilityServiceEnabled(@NonNull String serviceId);

    boolean MayReadNotifications(@NonNull ContentResolver contentResolver, @NonNull String packageName);
}
