package guepardoapps.lucahome.common.controller

import android.content.ContentResolver
import android.content.pm.ApplicationInfo
import android.view.Display

interface ISystemInfoController {
    fun getApkList(): List<ApplicationInfo>
    fun getApkPackageNameList(): List<String>
    fun isPackageInstalled(packageName: String): Boolean

    fun isServiceRunning(serviceClassName: String): Boolean
    fun isServiceRunning(serviceClass: Class<*>): Boolean
    fun isAccessibilityServiceEnabled(serviceId: String): Boolean
    fun isBaseActivityRunning(): Boolean

    fun currentAndroidApi(): Int

    fun checkAPI23SystemPermission(permissionRequestId: Int): Boolean
    fun mayReadNotifications(contentResolver: ContentResolver, packageName: String): Boolean

    fun displayDimension(): Display
    fun isScreeOn(): Boolean
    fun setScreenOff(removeFlags: IntArray, timeoutMs: Int)

    fun getDisplayBrightness(): Int
    fun setBrightness(brightness: Int)
    fun setBrightness(brightness: Double)
}