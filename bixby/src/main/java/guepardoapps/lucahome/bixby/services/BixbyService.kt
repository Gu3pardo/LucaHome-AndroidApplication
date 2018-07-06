package guepardoapps.lucahome.bixby.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import guepardoapps.lucahome.bixby.tasks.DelayedBackButtonTask
import guepardoapps.lucahome.common.controller.ISystemInfoController
import guepardoapps.lucahome.common.controller.SystemInfoController
import guepardoapps.lucahome.common.utils.Logger

class BixbyService : AccessibilityService() {
    private val tag = BixbyService::class.java.simpleName

    private val bixbyPackage = "com.samsung.android.app.spage"
    val serviceId = "guepardoapps.lucahome.bixby.services/.$tag"

    private var lastRunMillis: Long = 0
    private var maxRunFreqMs: Long = 500

    private lateinit var bixbyPairService: BixbyPairService
    private lateinit var systemInfoController: ISystemInfoController

    override fun onCreate() {
        super.onCreate()
        Logger.instance.warning(tag, "onCreate")
        bixbyPairService = BixbyPairService.instance
        systemInfoController = SystemInfoController(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.instance.warning(tag, "onDestroy")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Logger.instance.debug(tag, "onServiceConnected")
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent?) {
        if (!systemInfoController.isPackageInstalled(bixbyPackage)) {
            Logger.instance.verbose(tag, "Bixby seems to be not available on this device!")
            return
        }

        val rootInActiveWindow = rootInActiveWindow
        val activeWindowPackage: String? = rootInActiveWindow?.packageName?.toString()
        if (activeWindowPackage.isNullOrEmpty()) {
            Logger.instance.warning(tag, "Could not read activeWindowPackate")
            return
        }

        val currentMillis = System.currentTimeMillis()
        val runTooSoon = (currentMillis - lastRunMillis) > maxRunFreqMs

        if (runTooSoon || bixbyPackage != activeWindowPackage) {
            return
        }

        try {
            bixbyPairService.bixbyButtonPressed()
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        }

        lastRunMillis = currentMillis

        DelayedBackButtonTask(this).execute()
    }

    override fun onInterrupt() {
        Logger.instance.warning(tag, "onInterrupt")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Logger.instance.verbose(tag, "onUnbind")
        return super.onUnbind(intent)
    }
}