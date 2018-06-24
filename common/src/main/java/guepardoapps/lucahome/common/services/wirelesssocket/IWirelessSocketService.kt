package guepardoapps.lucahome.common.services.wirelesssocket

import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket
import guepardoapps.lucahome.common.services.common.ILucaService
import java.util.*

interface IWirelessSocketService : ILucaService<WirelessSocket> {
    var onWirelessSocketService: OnWirelessSocketService?

    fun get(uuid: UUID): WirelessSocket?

    fun setState(entry: WirelessSocket, newState: Boolean)

    fun deactivateAll()
}