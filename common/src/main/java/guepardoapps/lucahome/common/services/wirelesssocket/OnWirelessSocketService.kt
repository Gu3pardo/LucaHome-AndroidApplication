package guepardoapps.lucahome.common.services.wirelesssocket

import guepardoapps.lucahome.common.services.common.OnLucaStateService

interface OnWirelessSocketService : OnLucaStateService {
    fun setFinished(success: Boolean, message: String)
}