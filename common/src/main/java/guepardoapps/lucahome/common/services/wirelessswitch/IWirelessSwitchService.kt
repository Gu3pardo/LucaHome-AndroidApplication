package guepardoapps.lucahome.common.services.wirelessswitch

import guepardoapps.lucahome.common.models.wirelessswitch.WirelessSwitch
import guepardoapps.lucahome.common.services.common.ILucaService

interface IWirelessSwitchService : ILucaService<WirelessSwitch> {
    fun toggle(entry: WirelessSwitch)
}