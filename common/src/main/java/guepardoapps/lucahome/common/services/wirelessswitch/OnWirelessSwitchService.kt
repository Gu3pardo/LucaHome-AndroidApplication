package guepardoapps.lucahome.common.services.wirelessswitch

import guepardoapps.lucahome.common.services.common.OnLucaStateService

interface OnWirelessSwitchService : OnLucaStateService {
    fun toggleFinished(success: Boolean, message: String)
}