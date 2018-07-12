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
        actions.forEach { action -> intentFilter.addAction(action) }

        unregisterReceiver(registerReceiver)

        context.registerReceiver(registerReceiver, intentFilter)
        registeredReceiver.add(registerReceiver)
    }

    override fun unregisterReceiver(unregisterReceiver: BroadcastReceiver) {
        try {
            registeredReceiver
                    .remove(registeredReceiver
                            .find { x -> x == registeredReceiver })
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        }
    }

    override fun dispose() {
        for (receiver in registeredReceiver) {
            unregisterReceiver(receiver)
        }
    }
}