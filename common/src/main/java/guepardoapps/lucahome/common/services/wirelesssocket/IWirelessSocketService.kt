package guepardoapps.lucahome.common.services.wirelesssocket

import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket
import guepardoapps.lucahome.common.services.common.ILucaService

interface IWirelessSocketService : ILucaService<WirelessSocket> {
    fun setState(entry: WirelessSocket, newState: Boolean)
    fun deactivateAll()
}