package guepardoapps.lucahome.common.services.intent

import android.app.IntentService
import android.content.Intent
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.receiver.PeriodicActionReceiver
import guepardoapps.lucahome.common.services.change.ChangeService
import guepardoapps.lucahome.common.services.puckjs.PuckJsService
import guepardoapps.lucahome.common.services.room.RoomService
import guepardoapps.lucahome.common.services.temperature.TemperatureService
import guepardoapps.lucahome.common.services.wirelesssocket.WirelessSocketService
import guepardoapps.lucahome.common.services.wirelessswitch.WirelessSwitchService
import guepardoapps.lucahome.common.utils.Logger

internal class PeriodicActionService : IntentService("PeriodicActionService") {
    private val tag: String = PeriodicActionService::class.java.simpleName

    override fun onHandleIntent(intent: Intent?) {
        Logger.instance.verbose(tag, "onHandleIntent")

        val serverAction: ServerAction = intent?.getSerializableExtra(PeriodicActionReceiver.intentKey) as ServerAction
        if (serverAction == ServerAction.NULL) {
            Logger.instance.error(tag, "ServerAction is Null!")
            return
        }

        when (serverAction) {
            ServerAction.ChangeGet -> ChangeService.instance.load()
            ServerAction.PuckJsGet -> PuckJsService.instance.load()
            ServerAction.RoomGet -> RoomService.instance.load()
            ServerAction.TemperatureGet -> TemperatureService.instance.load()
            ServerAction.WirelessSocketGet -> WirelessSocketService.instance.load()
            ServerAction.WirelessSwitchGet -> WirelessSwitchService.instance.load()
            else -> Logger.instance.error(tag, "Not supported serverAction $serverAction")
        }
    }
}