package guepardoapps.lucahome.common.worker.temperature

import androidx.work.Worker
import guepardoapps.lucahome.common.services.temperature.TemperatureService

class TemperatureWorker : Worker() {
    override fun doWork(): Result {
        TemperatureService.instance.load()
        return Result.SUCCESS
    }
}