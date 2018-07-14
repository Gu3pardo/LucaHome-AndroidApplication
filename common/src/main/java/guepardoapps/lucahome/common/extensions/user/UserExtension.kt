package guepardoapps.lucahome.common.extensions.user

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.models.user.User

internal fun User.getJsonKey(): JsonKey {
    return this::class.annotations.find { it is JsonKey } as JsonKey
}

internal fun User.getPropertyJsonKey(propertyName: String): JsonKey {
    return this::class.java.getDeclaredField(propertyName).declaredAnnotations.find { it is JsonKey } as JsonKey
}

internal fun User.getNeededNetwork(): NeededNetwork {
    return this::class.annotations.find { it is NeededNetwork } as NeededNetwork
}

internal fun User.getPropertyNeededNetwork(propertyName: String): NeededNetwork {
    return this::class.java.getDeclaredField(propertyName).annotations.find { it is NeededNetwork } as NeededNetwork
}

internal fun User.getMethodNeededNetwork(methodName: String): NeededNetwork {
    return this::class.java.getDeclaredMethod(methodName).annotations.find { it is NeededNetwork } as NeededNetwork
}

fun User.getNeededUserRole(): NeededUserRole {
    return this::class.annotations.find { it is NeededUserRole } as NeededUserRole
}

fun User.getPropertyNeededUserRole(propertyName: String): NeededUserRole {
    return this::class.java.getDeclaredField(propertyName).annotations.find { it is NeededUserRole } as NeededUserRole
}

fun User.getMethodNeededUserRole(methodName: String): NeededUserRole {
    return this::class.java.getDeclaredMethod(methodName).annotations.find { it is NeededUserRole } as NeededUserRole
}