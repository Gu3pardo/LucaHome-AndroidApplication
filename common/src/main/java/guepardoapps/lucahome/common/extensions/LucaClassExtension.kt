package guepardoapps.lucahome.common.extensions

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.models.ILucaClass
import guepardoapps.lucahome.common.models.WirelessSocket

fun ILucaClass.getJsonKey(): JsonKey {
    return WirelessSocket::class.annotations.find { it is JsonKey } as JsonKey
}

fun ILucaClass.getPropertyJsonKey(propertyName: String): JsonKey {
    return WirelessSocket::class.java.getDeclaredField(propertyName).declaredAnnotations.find { it is JsonKey } as JsonKey
}

fun ILucaClass.getNeededNetwork(): NeededNetwork {
    return WirelessSocket::class.annotations.find { it is NeededNetwork } as NeededNetwork
}