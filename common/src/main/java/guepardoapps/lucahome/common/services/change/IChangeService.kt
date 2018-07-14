package guepardoapps.lucahome.common.services.change

import guepardoapps.lucahome.common.models.change.Change
import guepardoapps.lucahome.common.services.common.ILucaService

interface IChangeService : ILucaService<Change> {
    fun get(type: String): Change?
}