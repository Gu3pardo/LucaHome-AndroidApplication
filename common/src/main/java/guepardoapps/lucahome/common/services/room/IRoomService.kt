package guepardoapps.lucahome.common.services.room

import guepardoapps.lucahome.common.models.room.Room
import guepardoapps.lucahome.common.services.common.ILucaService
import java.util.*

interface IRoomService : ILucaService<Room> {
    var onRoomService: OnRoomService?

    fun get(uuid: UUID): Room?
}