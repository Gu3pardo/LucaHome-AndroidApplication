package guepardoapps.lucahome.common.extensions

import guepardoapps.lucahome.common.R
import guepardoapps.lucahome.common.models.WirelessSocket

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