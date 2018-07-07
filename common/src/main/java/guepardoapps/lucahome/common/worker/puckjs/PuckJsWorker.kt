package guepardoapps.lucahome.common.worker.puckjs

import androidx.work.Worker
import guepardoapps.lucahome.common.services.puckjs.PuckJsService

class PuckJsWorker : Worker() {
    override fun doWork(): Result {
        PuckJsService.instance.load()
        return Result.SUCCESS
    }
}