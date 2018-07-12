package guepardoapps.lucahome.common.services.change

import guepardoapps.lucahome.common.models.change.Change
import guepardoapps.lucahome.common.models.common.RxOptional
import guepardoapps.lucahome.common.services.common.ILucaService
import io.reactivex.subjects.PublishSubject
import java.util.*

interface IChangeService : ILucaService<Change> {
    val changePublishSubject: PublishSubject<RxOptional<List<Change>>>

    fun get(uuid: UUID): Change?
    fun get(type: String): Change?
}