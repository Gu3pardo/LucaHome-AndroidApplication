package guepardoapps.lucahome.common.controller

import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.annotation.NonNull
import android.view.Display
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import guepardoapps.lucahome.common.utils.Logger

class SystemInfoController(@NonNull private val context: Context) : ISystemInfoController {
    private val tag: String = SystemInfoController::class.java.simpleName

    private val minScreenOffTimeoutMs = 5 * 1000
    private val minBrightnessLevel = 0.1
    private val maxBrightnessLevel = 1.0

    private val accessibilityManager: AccessibilityManager? = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    private val activityManager: ActivityManager? = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val packageManager: PackageManager = context.packageManager

    override fun getApkList(): List<ApplicationInfo> {
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    }

    override fun getApkPackageNameList(): List<String> {
        return getApkList().map { value -> value.packageName }
    }

    override fun isPackageInstalled(packageName: String): Boolean {
        return getApkPackageNameList().any { value -> value == packageName }
    }

    @Suppress("DEPRECATION")
    override fun isServiceRunning(serviceClassName: String): Boolean {
        return activityManager?.getRunningServices(Integer.MAX_VALUE)!!
                .any { serviceInfo -> serviceClassName == serviceInfo.service.className }
    }

    override fun isServiceRunning(serviceClass: Class<*>): Boolean {
        return isServiceRunning(serviceClass.name)
    }

    override fun isAccessibilityServiceEnabled(serviceId: String): Boolean {
        return accessibilityManager?.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)!!
                .any { serviceInfo -> serviceInfo.id == serviceId }
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun isBaseActivityRunning(): Boolean {
        return activityManager?.appTasks!!
                .any { task -> task.taskInfo.baseActivity.packageName == context.packageName }
    }

    override fun currentAndroidApi(): Int {
        return Build.VERSION.SDK_INT
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun checkAPI23SystemPermission(permissionRequestId: Int): Boolean {
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
            (context as Activity).startActivityForResult(intent, permissionRequestId)
            return false
        }
        return true
    }

    override fun mayReadNotifications(contentResolver: ContentResolver, packageName: String): Boolean {
        return try {
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners").contains(packageName)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            false
        }
    }

    override fun displayDimension(): Display {
        val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return windowManager.defaultDisplay
    }

    override fun isScreeOn(): Boolean {
        val displayManager: DisplayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        return displayManager.displays.any { display -> display.state == Display.STATE_ON }
    }

    override fun setScreenOff(removeFlags: IntArray, timeoutMs: Int) {
        if (!isScreeOn()) {
            Logger.instance.info(tag, "Screen is already off")
            return
        }

        if (context !is Activity) {
            Logger.instance.error(tag, "Context is not of type activity")
            return
        }

        if (removeFlags.isEmpty()) {
            Logger.instance.error(tag, "No removeFlags")
            return
        }

        if (timeoutMs < minScreenOffTimeoutMs) {
            Logger.instance.error(tag, "Timeout $timeoutMs is too low (Min:$minScreenOffTimeoutMs)")
            return
        }

        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, timeoutMs)
        val window = context.window
        removeFlags.forEach { flag -> window.clearFlags(flag) }
    }

    override fun getDisplayBrightness(): Int {
        return try {
            val contentResolver = context.contentResolver
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            -1
        }
    }

    override fun setBrightness(brightness: Int) {
        setBrightness((brightness / 255.toDouble()))
    }

    override fun setBrightness(brightness: Double) {
        if (brightness > maxBrightnessLevel) {
            Logger.instance.error(tag, "Brightness too high: $brightness!")
            return
        }

        if (brightness < minBrightnessLevel) {
            Logger.instance.error(tag, "Brightness too low: $brightness!")
            return
        }

        if (context !is Activity) {
            Logger.instance.error(tag, "Context is not of type activity")
            return
        }

        try {
            val contentResolver = context.contentResolver
            val window = context.window
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, (brightness * 255).toInt())
            val layoutParams = window.attributes
            layoutParams.screenBrightness = brightness.toFloat()
            window.attributes = layoutParams
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        }
    }
}