package guepardoapps.lucahome.common.models

import guepardoapps.lucahome.common.enums.LucaServerActionTypes
import guepardoapps.lucahome.common.enums.ServerDatabaseAction
import java.util.*

class WirelessSocket(override val uuid: UUID,
                     override var roomUuid: UUID,
                     var name: String,
                     var code: String,
                     var state: Boolean,
                     val lastTriggerDateTime: Calendar,
                     val lastTriggerUser: String,
                     override var isOnServer: Boolean,
                     override var serverDatabaseAction: ServerDatabaseAction = ServerDatabaseAction.Null) : ILucaClass {
    private val tag: String = WirelessSocket::class.java.simpleName

    private val settingsHeader: String = "SharedPref_Notification_Socket_"
    var settingsKey: String

    var commandSetState: String

    init {
        settingsKey = "$settingsHeader$uuid"
        commandSetState = "${LucaServerActionTypes.SET_WIRELESS_SOCKET.command}$uuid"
    }

    public String GetCommandSetState() throws NoSuchMethodException
    {
        return String.format(Locale.getDefault(),
                "%s%s%s",
                LucaServerActionTypes.SET_WIRELESS_SOCKET.toString(), _uuid, ((_state) ? Constants . StateOn : Constants . StateOff));
    }

    @Override
    public String GetCommandAdd()
    {
        return String.format(Locale.getDefault(),
                "%s%s&roomuuid=%s&name=%s&code=%s",
                LucaServerActionTypes.ADD_WIRELESS_SOCKET.toString(), _uuid, _roomUuid, _name, _code);
    }

    @Override
    public String GetCommandUpdate()
    {
        return String.format(Locale.getDefault(),
                "%s%s&roomuuid=%s&name=%s&code=%s&isactivated=%s",
                LucaServerActionTypes.UPDATE_WIRELESS_SOCKET.toString(), _uuid, _roomUuid, _name, _code, (_state ? "1" : "0"));
    }

    @Override
    public String GetCommandDelete()
    {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.DELETE_WIRELESS_SOCKET.toString(), _uuid);
    }

    public int GetDrawable()
    {
        if (_name.contains("TV")) {
            if (_state) {
                return R.drawable.wireless_socket_tv_on;
            }
            return R.drawable.wireless_socket_tv_off;

        } else if (_name.contains("Light")) {
            if (_name.contains("Sleeping")) {
                if (_state) {
                    return R.drawable.wireless_socket_bed_light_on;
                }
                return R.drawable.wireless_socket_bed_light_off;

            } else {
                if (_state) {
                    return R.drawable.wireless_socket_light_on;
                }
                return R.drawable.wireless_socket_light_off;
            }

        } else if (_name.contains("Sound")) {
            if (_name.contains("Sleeping")) {
                if (_state) {
                    return R.drawable.wireless_socket_bed_sound_on;
                }
                return R.drawable.wireless_socket_bed_sound_off;

            } else if (_name.contains("Living")) {
                if (_state) {
                    return R.drawable.wireless_socket_sound_on;
                }
                return R.drawable.wireless_socket_sound_off;
            }

        } else if (_name.contains("PC") || _name.contains("WorkStation")) {
            if (_state) {
                return R.drawable.wireless_socket_laptop_on;
            }
            return R.drawable.wireless_socket_laptop_off;

        } else if (_name.contains("Printer")) {
            if (_state) {
                return R.drawable.wireless_socket_printer_on;
            }
            return R.drawable.wireless_socket_printer_off;

        } else if (_name.contains("Storage")) {
            if (_state) {
                return R.drawable.wireless_socket_storage_on;
            }
            return R.drawable.wireless_socket_storage_off;

        } else if (_name.contains("Heating")) {
            if (_name.contains("Bed")) {
                if (_state) {
                    return R.drawable.wireless_socket_bed_heating_on;
                }
                return R.drawable.wireless_socket_bed_heating_off;
            }

        } else if (_name.contains("Farm")) {
            if (_state) {
                return R.drawable.wireless_socket_watering_on;
            }
            return R.drawable.wireless_socket_watering_off;

        } else if (_name.contains("MediaServer")) {
            if (_state) {
                return R.drawable.wireless_socket_mediamirror_on;
            }
            return R.drawable.wireless_socket_mediamirror_off;

        } else if (_name.contains("GameConsole")) {
            if (_state) {
                return R.drawable.wireless_socket_gameconsole_on;
            }
            return R.drawable.wireless_socket_gameconsole_off;

        } else if (_name.contains("RaspberryPi")) {
            if (_state) {
                return R.drawable.wireless_socket_raspberry_on;
            }
            return R.drawable.wireless_socket_raspberry_off;
        }

        return R.drawable.wireless_socket;
    }

    @Override
    public String toString()
    {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"RoomUuid\":\"%s\",\"Name\":\"%s\",\"Code\":\"%s\",\"State\":\"%s\"}",
                Tag, _uuid, _roomUuid, _name, _code, (_state ? "1" : "0"));
    }
}
