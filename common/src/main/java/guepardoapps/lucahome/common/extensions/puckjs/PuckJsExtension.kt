package guepardoapps.lucahome.common.extensions.puckjs

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.models.puckjs.PuckJs

internal fun PuckJs.getJsonKey(): JsonKey {
    return this::class.annotations.find { it is JsonKey } as JsonKey
}

internal fun PuckJs.getPropertyJsonKey(propertyName: String): JsonKey {
    return this::class.java.getDeclaredField(propertyName).declaredAnnotations.find { it is JsonKey } as JsonKey
}

internal fun PuckJs.getNeededNetwork(): NeededNetwork {
    return this::class.annotations.find { it is NeededNetwork } as NeededNetwork
}

internal fun PuckJs.getPropertyNeededNetwork(propertyName: String): NeededNetwork {
    return this::class.java.getDeclaredField(propertyName).annotations.find { it is NeededNetwork } as NeededNetwork
}

internal fun PuckJs.getMethodNeededNetwork(methodName: String): NeededNetwork {
    return this::class.java.getDeclaredMethod(methodName).annotations.find { it is NeededNetwork } as NeededNetwork
}

fun PuckJs.getNeededUserRole(): NeededUserRole {
    return this::class.annotations.find { it is NeededUserRole } as NeededUserRole
}

fun PuckJs.getPropertyNeededUserRole(propertyName: String): NeededUserRole {
    return this::class.java.getDeclaredField(propertyName).annotations.find { it is NeededUserRole } as NeededUserRole
}

fun PuckJs.getMethodNeededUserRole(methodName: String): NeededUserRole {
    return this::class.java.getDeclaredMethod(methodName).annotations.find { it is NeededUserRole } as NeededUserRole
}