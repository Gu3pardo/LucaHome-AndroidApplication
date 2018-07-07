package guepardoapps.lucahome.common.services.position

import guepardoapps.lucahome.common.models.position.Position

interface OnPositionService {
    fun positionUpdated(position: Position)
}