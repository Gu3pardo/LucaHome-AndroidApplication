package guepardoapps.lucahome.common.extensions

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.models.common.ILucaClass

fun ILucaClass.getJsonKey(): JsonKey {
    return ILucaClass::class.annotations.find { it is JsonKey } as JsonKey
}

fun ILucaClass.getPropertyJsonKey(propertyName: String): JsonKey {
    return ILucaClass::class.java.getDeclaredField(propertyName).declaredAnnotations.find { it is JsonKey } as JsonKey
}

fun ILucaClass.getNeededNetwork(): NeededNetwork {
    return ILucaClass::class.annotations.find { it is NeededNetwork } as NeededNetwork
}

fun ILucaClass.getPropertyNeededNetwork(propertyName: String): NeededNetwork {
    return ILucaClass::class.java.getDeclaredField(propertyName).annotations.find { it is NeededNetwork } as NeededNetwork
}

fun ILucaClass.getMethodNeededNetwork(methodName: String): NeededNetwork {
    return ILucaClass::class.java.getDeclaredMethod(methodName).annotations.find { it is NeededNetwork } as NeededNetwork
}

fun ILucaClass.getNeededUserRole(): NeededUserRole {
    return ILucaClass::class.annotations.find { it is NeededUserRole } as NeededUserRole
}

fun ILucaClass.getPropertyNeededUserRole(propertyName: String): NeededUserRole {
    return ILucaClass::class.java.getDeclaredField(propertyName).annotations.find { it is NeededUserRole } as NeededUserRole
}

fun ILucaClass.getMethodNeededUserRole(methodName: String): NeededUserRole {
    return ILucaClass::class.java.getDeclaredMethod(methodName).annotations.find { it is NeededUserRole } as NeededUserRole
}