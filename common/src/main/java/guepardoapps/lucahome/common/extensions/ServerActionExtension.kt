package guepardoapps.lucahome.common.extensions

import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.enums.ServerAction

fun ServerAction.getNeededUserRole(): NeededUserRole {
    return ServerAction::class.annotations.find { it is NeededUserRole } as NeededUserRole
}

fun ServerAction.getNeededNetwork(): NeededNetwork {
    return ServerAction::class.annotations.find { it is NeededNetwork } as NeededNetwork
}