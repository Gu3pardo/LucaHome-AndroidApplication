package guepardoapps.lucahome.common.extensions.temperature

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.models.temperature.Temperature

internal fun Temperature.getJsonKey(): JsonKey {
    return this::class.annotations.find { it is JsonKey } as JsonKey
}

internal fun Temperature.getPropertyJsonKey(propertyName: String): JsonKey {
    return this::class.java.getDeclaredField(propertyName).declaredAnnotations.find { it is JsonKey } as JsonKey
}

internal fun Temperature.getNeededNetwork(): NeededNetwork {
    return this::class.annotations.find { it is NeededNetwork } as NeededNetwork
}

internal fun Temperature.getPropertyNeededNetwork(propertyName: String): NeededNetwork {
    return this::class.java.getDeclaredField(propertyName).annotations.find { it is NeededNetwork } as NeededNetwork
}

internal fun Temperature.getMethodNeededNetwork(methodName: String): NeededNetwork {
    return this::class.java.getDeclaredMethod(methodName).annotations.find { it is NeededNetwork } as NeededNetwork
}

fun Temperature.getNeededUserRole(): NeededUserRole {
    return this::class.annotations.find { it is NeededUserRole } as NeededUserRole
}

fun Temperature.getPropertyNeededUserRole(propertyName: String): NeededUserRole {
    return this::class.java.getDeclaredField(propertyName).annotations.find { it is NeededUserRole } as NeededUserRole
}

fun Temperature.getMethodNeededUserRole(methodName: String): NeededUserRole {
    return this::class.java.getDeclaredMethod(methodName).annotations.find { it is NeededUserRole } as NeededUserRole
}