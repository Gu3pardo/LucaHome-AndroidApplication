package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"WeakerAccess"})
public class WirelessSocket implements ILucaClass {
    private static final String Tag = WirelessSocket.class.getSimpleName();

    public static final String SettingsHeader = "SharedPref_Notification_Socket_";

    protected UUID _uuid;
    protected UUID _roomUuid;
    protected String _name;
    protected String _code;
    protected boolean _isActivated;

    protected Calendar _lastTriggerDateTime;
    protected String _lastTriggerUser;

    protected boolean _isOnServer;
    protected LucaServerDbAction _serverDbAction;

    public WirelessSocket(
            @NonNull UUID uuid,
            @NonNull UUID roomUuid,
            @NonNull String name,
            @NonNull String code,
            boolean isActivated,
            @NonNull Calendar lastTriggerDateTime,
            @NonNull String lastTriggerUser,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _roomUuid = roomUuid;
        _name = name;
        _code = code;
        _isActivated = isActivated;
        _lastTriggerDateTime = lastTriggerDateTime;
        _lastTriggerUser = lastTriggerUser;
        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    public void SetRoomUuid(@NonNull UUID roomUuid) {
        _roomUuid = roomUuid;
    }

    @Override
    public UUID GetRoomUuid() {
        return _roomUuid;
    }

    public void SetName(@NonNull String name) {
        _name = name;
    }

    public String GetName() {
        return _name;
    }

    public void SetCode(@NonNull String code) {
        _code = code;
    }

    public String GetCode() {
        return _code;
    }

    public void SetActivated(boolean isActivated) {
        _isActivated = isActivated;
    }

    public boolean IsActivated() {
        return _isActivated;
    }

    public void SetLastTriggerDateTime(@NonNull Calendar lastTriggerDateTime) {
        _lastTriggerDateTime = lastTriggerDateTime;
    }

    public Calendar GetLastTriggerDateTime() {
        return _lastTriggerDateTime;
    }

    public void SetLastTriggerUser(@NonNull String lastTriggerUser) {
        _lastTriggerUser = lastTriggerUser;
    }

    public String GetLastTriggerUser() {
        return _lastTriggerUser;
    }

    public String GetSettingsKey() {
        return String.format(Locale.getDefault(), "%s%s", SettingsHeader, _name);
    }

    @Override
    public void SetIsOnServer(boolean isOnServer) {
        _isOnServer = isOnServer;
    }

    @Override
    public boolean GetIsOnServer() {
        return _isOnServer;
    }

    @Override
    public void SetServerDbAction(@NonNull LucaServerDbAction serverDbAction) {
        _serverDbAction = serverDbAction;
    }

    @Override
    public LucaServerDbAction GetServerDbAction() {
        return _serverDbAction;
    }

    public String GetCommandSetState() throws NoSuchMethodException {
        return String.format(Locale.getDefault(),
                "%s%s%s",
                LucaServerActionTypes.SET_WIRELESS_SOCKET.toString(), _uuid, ((_isActivated) ? Constants.StateOn : Constants.StateOff));
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&roomuuid=%s&name=%s&code=%s",
                LucaServerActionTypes.ADD_WIRELESS_SOCKET.toString(), _uuid, _roomUuid, _name, _code);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&roomuuid=%s&name=%s&code=%s&isactivated=%s",
                LucaServerActionTypes.UPDATE_WIRELESS_SOCKET.toString(), _uuid, _roomUuid, _name, _code, (_isActivated ? "1" : "0"));
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.DELETE_WIRELESS_SOCKET.toString(), _uuid);
    }

    public int GetDrawable() {
        if (_name.contains("TV")) {
            if (_isActivated) {
                return R.drawable.wireless_socket_tv_on;
            }
            return R.drawable.wireless_socket_tv_off;

        } else if (_name.contains("Light")) {
            if (_name.contains("Sleeping")) {
                if (_isActivated) {
                    return R.drawable.wireless_socket_bed_light_on;
                }
                return R.drawable.wireless_socket_bed_light_off;

            } else {
                if (_isActivated) {
                    return R.drawable.wireless_socket_light_on;
                }
                return R.drawable.wireless_socket_light_off;
            }

        } else if (_name.contains("Sound")) {
            if (_name.contains("Sleeping")) {
                if (_isActivated) {
                    return R.drawable.wireless_socket_bed_sound_on;
                }
                return R.drawable.wireless_socket_bed_sound_off;

            } else if (_name.contains("Living")) {
                if (_isActivated) {
                    return R.drawable.wireless_socket_sound_on;
                }
                return R.drawable.wireless_socket_sound_off;
            }

        } else if (_name.contains("PC") || _name.contains("WorkStation")) {
            if (_isActivated) {
                return R.drawable.wireless_socket_laptop_on;
            }
            return R.drawable.wireless_socket_laptop_off;

        } else if (_name.contains("Printer")) {
            if (_isActivated) {
                return R.drawable.wireless_socket_printer_on;
            }
            return R.drawable.wireless_socket_printer_off;

        } else if (_name.contains("Storage")) {
            if (_isActivated) {
                return R.drawable.wireless_socket_storage_on;
            }
            return R.drawable.wireless_socket_storage_off;

        } else if (_name.contains("Heating")) {
            if (_name.contains("Bed")) {
                if (_isActivated) {
                    return R.drawable.wireless_socket_bed_heating_on;
                }
                return R.drawable.wireless_socket_bed_heating_off;
            }

        } else if (_name.contains("Farm")) {
            if (_isActivated) {
                return R.drawable.wireless_socket_watering_on;
            }
            return R.drawable.wireless_socket_watering_off;

        } else if (_name.contains("MediaServer")) {
            if (_isActivated) {
                return R.drawable.wireless_socket_mediamirror_on;
            }
            return R.drawable.wireless_socket_mediamirror_off;

        } else if (_name.contains("GameConsole")) {
            if (_isActivated) {
                return R.drawable.wireless_socket_gameconsole_on;
            }
            return R.drawable.wireless_socket_gameconsole_off;

        } else if (_name.contains("RaspberryPi")) {
            if (_isActivated) {
                return R.drawable.wireless_socket_raspberry_on;
            }
            return R.drawable.wireless_socket_raspberry_off;
        }

        return R.drawable.wireless_socket;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"RoomUuid\":\"%s\",\"Name\":\"%s\",\"Code\":\"%s\",\"IsActivated\":\"%s\"}",
                Tag, _uuid, _roomUuid, _name, _code, (_isActivated ? "1" : "0"));
    }
}
