package guepardoapps.lucahome.common.services.change

import guepardoapps.lucahome.common.models.change.Change
import guepardoapps.lucahome.common.services.common.ILucaService
import java.util.*

interface IChangeService : ILucaService<Change> {
    var onChangeService: OnChangeService?

    fun get(uuid: UUID): Change?
    fun get(type: String): Change?
}