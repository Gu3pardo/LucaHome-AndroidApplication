package guepardoapps.lucahome.common.controller;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.List;
import java.util.Locale;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"deprecation", "WeakerAccess"})
public class AndroidSystemController implements IAndroidSystemController {
    private static final String Tag = AndroidSystemController.class.getSimpleName();

    protected Context _context;

    public AndroidSystemController(@NonNull Context context) {
        _context = context;
    }

    @Override
    public boolean IsServiceRunning(@NonNull String serviceClassName) {
        ActivityManager activityManager = (ActivityManager) _context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager == null) {
            Logger.getInstance().Error(Tag, "ActivityManager is null!");
            return false;
        }

        for (RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClassName.equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean IsServiceRunning(@NonNull Class<?> serviceClass) {
        return IsServiceRunning(serviceClass.getName());
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public boolean IsBaseActivityRunning() {
        ActivityManager activityManager = (ActivityManager) _context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager == null) {
            Logger.getInstance().Error(Tag, "ActivityManager is null!");
            return false;
        }

        List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();

        for (ActivityManager.AppTask task : tasks) {
            if (_context.getPackageName().equalsIgnoreCase(task.getTaskInfo().baseActivity.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean IsBaseActivityRunningPreAPI23() {
        ActivityManager activityManager = (ActivityManager) _context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager == null) {
            Logger.getInstance().Error(Tag, "ActivityManager is null!");
            return false;
        }

        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (RunningTaskInfo task : tasks) {
            if (_context.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int CurrentAndroidApi() {
        return Build.VERSION.SDK_INT;
    }

    @Override
    @TargetApi(23)
    public boolean CheckAPI23SystemPermission(int permissionRequestId) {
        if (!Settings.canDrawOverlays(_context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + _context.getPackageName()));
            ((Activity) _context).startActivityForResult(intent, permissionRequestId);
            return false;
        }
        return true;
    }

    @Override
    public boolean IsAccessibilityServiceEnabled(@NonNull String serviceId) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "IsAccessibilityServiceEnabled for serviceId %s", serviceId));

        AccessibilityManager accessibilityManager = (AccessibilityManager) _context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager == null) {
            Logger.getInstance().Error(Tag, "accessibilityManager is null!");
            return false;
        }

        List<AccessibilityServiceInfo> runningServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo service : runningServices) {
            Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Found active service %s", service.getId()));
            if (serviceId.equals(service.getId())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean MayReadNotifications(@NonNull ContentResolver contentResolver, @NonNull String packageName) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "MayReadNotifications for packageName %s", packageName));

        try {
            return Settings.Secure.getString(contentResolver, "enabled_notification_listeners").contains(packageName);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
        }
        return false;
    }
}
