package guepardoapps.lucahome.common.services.wirelessswitch

import guepardoapps.lucahome.common.models.wirelessswitch.WirelessSwitch
import guepardoapps.lucahome.common.services.common.ILucaService
import java.util.*

interface IWirelessSwitchService : ILucaService<WirelessSwitch> {
    var onWirelessSwitchService: OnWirelessSwitchService?

    fun get(uuid: UUID): WirelessSwitch?
    fun toggle(entry: WirelessSwitch)
}