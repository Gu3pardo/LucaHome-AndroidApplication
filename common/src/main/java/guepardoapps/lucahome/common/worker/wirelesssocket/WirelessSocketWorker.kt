package guepardoapps.lucahome.common.worker.wirelesssocket

import androidx.work.Worker
import guepardoapps.lucahome.common.services.wirelesssocket.WirelessSocketService

class WirelessSocketWorker : Worker() {
    override fun doWork(): Result {
        WirelessSocketService.instance.load()
        return Result.SUCCESS
    }
}