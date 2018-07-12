package guepardoapps.lucahome.common.services.position

import guepardoapps.lucahome.common.models.common.RxOptional
import guepardoapps.lucahome.common.models.position.Position
import io.reactivex.subjects.PublishSubject

interface IPositionService {
    val currentPositionPublishSubject: PublishSubject<RxOptional<Position>>

    fun updatePosition()
}