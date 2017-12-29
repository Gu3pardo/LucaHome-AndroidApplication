package guepardoapps.lucahome.basic.controller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;

import java.util.List;

import guepardoapps.lucahome.basic.utils.Logger;

public class AndroidSystemController {
    private static final String TAG = AndroidSystemController.class.getSimpleName();

    protected Context _context;

    public AndroidSystemController(@NonNull Context context) {
        _context = context;
    }

    public boolean IsServiceRunning(@NonNull Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) _context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager == null) {
            Logger.getInstance().Error(TAG, "ActivityManager is null!");
            return false;
        }

        for (RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean IsBaseActivityRunning() {
        ActivityManager activityManager = (ActivityManager) _context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager == null) {
            Logger.getInstance().Error(TAG, "ActivityManager is null!");
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

    @SuppressWarnings("deprecation")
    public boolean IsBaseActivityRunningPreAPI23() {
        ActivityManager activityManager = (ActivityManager) _context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager == null) {
            Logger.getInstance().Error(TAG, "ActivityManager is null!");
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

    public int CurrentAndroidApi() {
        return Build.VERSION.SDK_INT;
    }

    @TargetApi(23)
    public boolean CheckAPI23SystemPermission(int permissionRequestId) {
        if (!Settings.canDrawOverlays(_context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + _context.getPackageName()));
            ((Activity) _context).startActivityForResult(intent, permissionRequestId);
            return false;
        }
        return true;
    }
}
