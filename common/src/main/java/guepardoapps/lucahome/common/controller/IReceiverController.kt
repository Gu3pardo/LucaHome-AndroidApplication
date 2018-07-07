package guepardoapps.lucahome.common.controller

import android.content.BroadcastReceiver

interface IReceiverController {
    fun registerReceiver(registerReceiver: BroadcastReceiver, actions: Array<String>)
    fun unregisterReceiver(unregisterReceiver: BroadcastReceiver)
    fun dispose()
}