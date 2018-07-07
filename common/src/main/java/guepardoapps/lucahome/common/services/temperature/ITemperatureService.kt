package guepardoapps.lucahome.common.services.temperature

import guepardoapps.lucahome.common.models.temperature.Temperature
import guepardoapps.lucahome.common.services.common.ILucaService
import java.util.*

interface ITemperatureService : ILucaService<Temperature> {
    var onTemperatureService: OnTemperatureService?

    fun get(uuid: UUID): Temperature?
}