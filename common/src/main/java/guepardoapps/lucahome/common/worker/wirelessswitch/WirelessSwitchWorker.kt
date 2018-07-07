package guepardoapps.lucahome.common.worker.wirelessswitch

import androidx.work.Worker
import guepardoapps.lucahome.common.services.wirelessswitch.WirelessSwitchService

class WirelessSwitchWorker : Worker() {
    override fun doWork(): Result {
        WirelessSwitchService.instance.load()
        return Result.SUCCESS
    }
}