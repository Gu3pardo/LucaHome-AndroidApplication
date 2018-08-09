package guepardoapps.lucahome.common.extensions.wirelesssocket

import guepardoapps.lucahome.common.R
import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket

internal fun WirelessSocket.getJsonKey(): JsonKey {
    return this::class.annotations.find { it is JsonKey } as JsonKey
}

internal fun WirelessSocket.getPropertyJsonKey(propertyName: String): JsonKey {
    return this::class.java.getDeclaredField(propertyName).declaredAnnotations.find { it is JsonKey } as JsonKey
}

internal fun WirelessSocket.getNeededNetwork(): NeededNetwork {
    return this::class.annotations.find { it is NeededNetwork } as NeededNetwork
}

internal fun WirelessSocket.getPropertyNeededNetwork(propertyName: String): NeededNetwork {
    return this::class.java.getDeclaredField(propertyName).annotations.find { it is NeededNetwork } as NeededNetwork
}

internal fun WirelessSocket.getMethodNeededNetwork(methodName: String): NeededNetwork {
    return this::class.java.getDeclaredMethod(methodName).annotations.find { it is NeededNetwork } as NeededNetwork
}

fun WirelessSocket.getNeededUserRole(): NeededUserRole {
    return this::class.annotations.find { it is NeededUserRole } as NeededUserRole
}

fun WirelessSocket.getPropertyNeededUserRole(propertyName: String): NeededUserRole {
    return this::class.java.getDeclaredField(propertyName).annotations.find { it is NeededUserRole } as NeededUserRole
}

fun WirelessSocket.getMethodNeededUserRole(methodName: String): NeededUserRole {
    return this::class.java.getDeclaredMethod(methodName).annotations.find { it is NeededUserRole } as NeededUserRole
}

fun WirelessSocket.getDrawable(): Int {
    if (name.contains("TV")) {
        if (state) {
            return R.drawable.wireless_socket_tv_on;
        }
        return R.drawable.wireless_socket_tv_off;

    } else if (name.contains("Light")) {
        if (name.contains("Sleeping")) {
            if (state) {
                return R.drawable.wireless_socket_bed_light_on;
            }
            return R.drawable.wireless_socket_bed_light_off;

        } else {
            if (state) {
                return R.drawable.wireless_socket_light_on;
            }
            return R.drawable.wireless_socket_light_off;
        }

    } else if (name.contains("Sound")) {
        if (name.contains("Sleeping")) {
            if (state) {
                return R.drawable.wireless_socket_bed_sound_on;
            }
            return R.drawable.wireless_socket_bed_sound_off;

        } else if (name.contains("Living")) {
            if (state) {
                return R.drawable.wireless_socket_sound_on;
            }
            return R.drawable.wireless_socket_sound_off;
        }

    } else if (name.contains("PC") || name.contains("WorkStation")) {
        if (state) {
            return R.drawable.wireless_socket_laptop_on;
        }
        return R.drawable.wireless_socket_laptop_off;

    } else if (name.contains("Printer")) {
        if (state) {
            return R.drawable.wireless_socket_printer_on;
        }
        return R.drawable.wireless_socket_printer_off;

    } else if (name.contains("Storage")) {
        if (state) {
            return R.drawable.wireless_socket_storage_on;
        }
        return R.drawable.wireless_socket_storage_off;

    } else if (name.contains("Heating")) {
        if (name.contains("Bed")) {
            if (state) {
                return R.drawable.wireless_socket_bed_heating_on;
            }
            return R.drawable.wireless_socket_bed_heating_off;
        }

    } else if (name.contains("Farm")) {
        if (state) {
            return R.drawable.wireless_socket_watering_on;
        }
        return R.drawable.wireless_socket_watering_off;

    } else if (name.contains("MediaServer")) {
        if (state) {
            return R.drawable.wireless_socket_mediamirror_on;
        }
        return R.drawable.wireless_socket_mediamirror_off;

    } else if (name.contains("GameConsole")) {
        if (state) {
            return R.drawable.wireless_socket_gameconsole_on;
        }
        return R.drawable.wireless_socket_gameconsole_off;

    } else if (name.contains("RaspberryPi")) {
        if (state) {
            return R.drawable.wireless_socket_raspberry_on;
        }
        return R.drawable.wireless_socket_raspberry_off;
    }

    return R.drawable.wireless_socket;
}