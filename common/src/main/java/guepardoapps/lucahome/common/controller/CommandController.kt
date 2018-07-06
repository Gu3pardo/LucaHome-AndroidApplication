package guepardoapps.lucahome.common.controller

import android.app.Instrumentation
import android.os.Handler
import guepardoapps.lucahome.common.methods.hasRoot
import guepardoapps.lucahome.common.runnable.CommandRunnable
import guepardoapps.lucahome.common.utils.Logger

class CommandController : ICommandController {
    private val tag: String = CommandController::class.java.simpleName

    private val maxKeyTimeoutMs: Int = 3 * 1000

    private val handler: Handler = Handler()
    private val instrumentation: Instrumentation = Instrumentation()

    override fun rebootDevice(timeoutMs: Long): Boolean {
        if (!hasRoot()) {
            Logger.instance.warning(tag, "Device is not rooted!")
            return false
        }

        if (timeoutMs < 0) {
            Logger.instance.warning(tag, "Timeout cannot be negative!")
            return false
        }

        handler.postDelayed(CommandRunnable(arrayOf("su", "-c", "reboot")), timeoutMs)
        return true
    }

    override fun shutDownDevice(timeoutMs: Long): Boolean {
        if (!hasRoot()) {
            Logger.instance.warning(tag, "Device is not rooted!")
            return false
        }

        if (timeoutMs < 0) {
            Logger.instance.warning(tag, "Timeout cannot be negative!")
            return false
        }

        handler.postDelayed(CommandRunnable(arrayOf("su", "-c", "reboot -p")), timeoutMs)
        return true
    }

    override fun simulateKeyPress(keys: IntArray, timeout: Int) {
        if (timeout < 0) {
            Logger.instance.warning(tag, "Timeout cannot be negative!")
            return
        }

        if (timeout > maxKeyTimeoutMs) {
            Logger.instance.warning(tag, "Timeout is higher then maxKeyTimeoutMs($maxKeyTimeoutMs)!")
            return
        }

        val thread = object : Thread() {
            override fun run() {
                try {
                    for (key in keys) {
                        instrumentation.sendKeyDownUpSync(key)
                        Thread.sleep(timeout.toLong())
                    }
                } catch (exception: InterruptedException) {
                    Logger.instance.error(tag, exception)
                }
            }
        }

        thread.start()
    }
}