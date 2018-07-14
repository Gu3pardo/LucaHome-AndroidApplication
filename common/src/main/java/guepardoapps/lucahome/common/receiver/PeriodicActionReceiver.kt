package guepardoapps.lucahome.common.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.services.intent.PeriodicActionService
import guepardoapps.lucahome.common.utils.Logger

internal class PeriodicActionReceiver : BroadcastReceiver() {
    private val tag: String = PeriodicActionReceiver::class.java.simpleName

    companion object {
        const val action: String = "guepardoapps.lucahome.common.receiver.reload"
        const val intentKey: String = "ServerAction"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Logger.instance.verbose(tag, "onReceive")

        val serverAction: ServerAction = intent?.getSerializableExtra(intentKey) as ServerAction
        if (serverAction == ServerAction.NULL) {
            Logger.instance.error(tag, "ServerAction is Null!")
            return
        }

        val reloadIntent = Intent(context, PeriodicActionService::class.java)
        reloadIntent.putExtra(intentKey, serverAction)
        context?.startService(reloadIntent)
    }
}