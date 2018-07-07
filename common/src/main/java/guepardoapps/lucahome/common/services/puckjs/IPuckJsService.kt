package guepardoapps.lucahome.common.services.puckjs

import guepardoapps.lucahome.common.models.puckjs.PuckJs
import guepardoapps.lucahome.common.services.common.ILucaService
import java.util.*

interface IPuckJsService : ILucaService<PuckJs> {
    var onPuckJsService: OnPuckJsService?

    fun get(uuid: UUID): PuckJs?
}