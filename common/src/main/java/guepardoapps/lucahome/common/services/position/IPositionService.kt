package guepardoapps.lucahome.common.services.position

import guepardoapps.lucahome.common.models.position.Position

interface IPositionService {
    var currentPosition: Position

    fun updatePosition()
}