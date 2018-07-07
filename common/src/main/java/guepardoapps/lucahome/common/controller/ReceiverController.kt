package guepardoapps.lucahome.common.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import guepardoapps.lucahome.common.utils.Logger

class ReceiverController(private val context: Context) : IReceiverController {
    private val tag: String = ReceiverController::class.java.simpleName

    private val registeredReceiver: ArrayList<BroadcastReceiver> = arrayListOf()

    override fun registerReceiver(registerReceiver: BroadcastReceiver, actions: Array<String>) {
        val intentFilter = IntentFilter()
        for (action in actions) {
            intentFilter.addAction(action)
        }

        unregisterReceiver(registerReceiver)

        context.registerReceiver(registerReceiver, intentFilter)
        registeredReceiver.add(registerReceiver)
    }

    override fun unregisterReceiver(unregisterReceiver: BroadcastReceiver) {
        for (receiver in registeredReceiver) {
            if (receiver == unregisterReceiver) {
                try {
                    context.unregisterReceiver(receiver)
                    registeredReceiver.remove(receiver)
                } catch (exception: Exception) {
                    Logger.instance.error(tag, exception)
                }
                break
            }
        }
    }

    override fun dispose() {
        for (receiver in registeredReceiver) {
            unregisterReceiver(receiver)
        }
    }
}