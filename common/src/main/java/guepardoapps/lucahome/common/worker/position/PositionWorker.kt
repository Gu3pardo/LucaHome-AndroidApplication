package guepardoapps.lucahome.common.worker.position

import androidx.work.Worker
import guepardoapps.lucahome.common.services.position.PositionService

class PositionWorker : Worker() {
    override fun doWork(): Result {
        PositionService.instance.updatePosition()
        return Result.SUCCESS
    }
}