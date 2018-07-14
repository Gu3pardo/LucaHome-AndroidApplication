package guepardoapps.lucahome.common.extensions.wirelessswitch

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.models.wirelessswitch.WirelessSwitch

internal fun WirelessSwitch.getJsonKey(): JsonKey {
    return this::class.annotations.find { it is JsonKey } as JsonKey
}

internal fun WirelessSwitch.getPropertyJsonKey(propertyName: String): JsonKey {
    return this::class.java.getDeclaredField(propertyName).declaredAnnotations.find { it is JsonKey } as JsonKey
}

internal fun WirelessSwitch.getNeededNetwork(): NeededNetwork {
    return this::class.annotations.find { it is NeededNetwork } as NeededNetwork
}

internal fun WirelessSwitch.getPropertyNeededNetwork(propertyName: String): NeededNetwork {
    return this::class.java.getDeclaredField(propertyName).annotations.find { it is NeededNetwork } as NeededNetwork
}

internal fun WirelessSwitch.getMethodNeededNetwork(methodName: String): NeededNetwork {
    return this::class.java.getDeclaredMethod(methodName).annotations.find { it is NeededNetwork } as NeededNetwork
}

fun WirelessSwitch.getNeededUserRole(): NeededUserRole {
    return this::class.annotations.find { it is NeededUserRole } as NeededUserRole
}

fun WirelessSwitch.getPropertyNeededUserRole(propertyName: String): NeededUserRole {
    return this::class.java.getDeclaredField(propertyName).annotations.find { it is NeededUserRole } as NeededUserRole
}

fun WirelessSwitch.getMethodNeededUserRole(methodName: String): NeededUserRole {
    return this::class.java.getDeclaredMethod(methodName).annotations.find { it is NeededUserRole } as NeededUserRole
}