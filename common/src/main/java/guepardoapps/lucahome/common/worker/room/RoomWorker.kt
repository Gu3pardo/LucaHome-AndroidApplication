package guepardoapps.lucahome.common.worker.room

import androidx.work.Worker
import guepardoapps.lucahome.common.services.room.RoomService

class RoomWorker : Worker() {
    override fun doWork(): Result {
        RoomService.instance.load()
        return Result.SUCCESS
    }
}