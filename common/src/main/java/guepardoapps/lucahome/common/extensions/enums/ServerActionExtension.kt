package guepardoapps.lucahome.common.extensions.enums

import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.enums.common.ServerAction

fun ServerAction.getNeededUserRole(): NeededUserRole {
    return this::class.annotations.find { it is NeededUserRole } as NeededUserRole
}

internal fun ServerAction.getNeededNetwork(): NeededNetwork {
    return this::class.annotations.find { it is NeededNetwork } as NeededNetwork
}