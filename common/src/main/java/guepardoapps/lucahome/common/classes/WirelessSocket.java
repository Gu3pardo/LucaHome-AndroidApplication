package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class WirelessSocket implements Serializable, ILucaClass {
    private static final long serialVersionUID = 5579389712706446580L;
    private static final String TAG = WirelessSocket.class.getSimpleName();

    public static final String SETTINGS_HEADER = "SharedPref_Notification_";

    protected int _id;

    protected String _name;
    protected String _area;
    protected String _code;
    protected boolean _isActivated;

    protected SerializableDate _lastTriggerDate;
    protected SerializableTime _lastTriggerTime;
    protected String _lastTriggerUser;

    protected String _shortName;

    protected boolean _isOnServer;
    protected LucaServerDbAction _serverDbAction;

    public WirelessSocket(
            int id,
            @NonNull String name,
            @NonNull String area,
            @NonNull String code,
            boolean isActivated,
            @NonNull SerializableDate lastTriggerDate,
            @NonNull SerializableTime lastTriggerTime,
            @NonNull String lastTriggerUser,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _id = id;

        _name = name;
        _area = area;
        _code = code;
        _isActivated = isActivated;

        _lastTriggerDate = lastTriggerDate;
        _lastTriggerTime = lastTriggerTime;
        _lastTriggerUser = lastTriggerUser;

        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;

        _shortName = createShortName(_name);
    }

    @Override
    public int GetId() {
        return _id;
    }

    public String GetName() {
        return _name;
    }

    public void SetName(@NonNull String name) {
        _name = name;
    }

    public String GetArea() {
        return _area;
    }

    public void SetArea(@NonNull String area) {
        _area = area;
    }

    public String GetCode() {
        return _code;
    }

    public void SetCode(@NonNull String code) {
        _code = code;
    }

    public boolean IsActivated() {
        return _isActivated;
    }

    public void SetActivated(boolean isActivated) {
        _isActivated = isActivated;
    }

    public SerializableDate GetLastTriggerDate() {
        return _lastTriggerDate;
    }

    public void SetLastTriggerDate(@NonNull SerializableDate lastTriggerDate) {
        _lastTriggerDate = lastTriggerDate;
    }

    public SerializableTime GetLastTriggerTime() {
        return _lastTriggerTime;
    }

    public void SetLastTriggerTime(@NonNull SerializableTime lastTriggerTime) {
        _lastTriggerTime = lastTriggerTime;
    }

    public String GetLastTriggerUser() {
        return _lastTriggerUser;
    }

    public void SetLastTriggeUser(@NonNull String lastTriggerUser) {
        _lastTriggerUser = lastTriggerUser;
    }

    public String GetActivationString() {
        return _isActivated ? "On" : "Off";
    }

    public String GetShortName() {
        return _shortName;
    }

    public String GetSettingsKey() {
        return String.format(Locale.getDefault(), "%s%s", SETTINGS_HEADER, _name);
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

    public String CommandSetState() throws Exception {
        return String.format(Locale.getDefault(), "%s%s%s", LucaServerAction.SET_SOCKET.toString(), _name, ((_isActivated) ? Constants.STATE_ON : Constants.STATE_OFF));
    }

    public String CommandChangeState() throws Exception {
        return String.format(Locale.getDefault(), "%s%s%s", LucaServerAction.SET_SOCKET.toString(), _name, ((!_isActivated) ? Constants.STATE_ON : Constants.STATE_OFF));
    }

    @Override
    public String CommandAdd() {
        return String.format(Locale.getDefault(), "%s%s&area=%s&code=%s", LucaServerAction.ADD_SOCKET.toString(), _name, _area, _code);
    }

    @Override
    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%s&area=%s&code=%s&isactivated=%s", LucaServerAction.ADD_SOCKET.toString(), _name, _area, _code, (_isActivated ? "1" : "0"));
    }

    @Override
    public String CommandDelete() {
        return String.format(Locale.getDefault(), "%s%s", LucaServerAction.DELETE_SOCKET.toString(), _name);
    }

    public int GetWallpaper() {
        /*TODO Add wallpaper for the sockets*/
        return GetDrawable();
    }

    public int GetDrawable() {
        if (_name.contains("TV")) {
            if (_isActivated) {
                return R.drawable.tv_on;
            } else {
                return R.drawable.tv_off;
            }
        } else if (_name.contains("Light")) {
            if (_name.contains("Sleeping")) {
                if (_isActivated) {
                    return R.drawable.bed_light_on;
                } else {
                    return R.drawable.bed_light_off;
                }
            } else {
                if (_isActivated) {
                    return R.drawable.light_on;
                } else {
                    return R.drawable.light_off;
                }
            }
        } else if (_name.contains("Sound")) {
            if (_name.contains("Sleeping")) {
                if (_isActivated) {
                    return R.drawable.bed_sound_on;
                } else {
                    return R.drawable.bed_sound_off;
                }
            } else if (_name.contains("Living")) {
                if (_isActivated) {
                    return R.drawable.sound_on;
                } else {
                    return R.drawable.sound_off;
                }
            }
        } else if (_name.contains("PC") || _name.contains("WorkStation")) {
            if (_isActivated) {
                return R.drawable.laptop_on;
            } else {
                return R.drawable.laptop_off;
            }
        } else if (_name.contains("Printer")) {
            if (_isActivated) {
                return R.drawable.printer_on;
            } else {
                return R.drawable.printer_off;
            }
        } else if (_name.contains("Storage")) {
            if (_isActivated) {
                return R.drawable.storage_on;
            } else {
                return R.drawable.storage_off;
            }
        } else if (_name.contains("Heating")) {
            if (_name.contains("Bed")) {
                if (_isActivated) {
                    return R.drawable.bed_heating_on;
                } else {
                    return R.drawable.bed_heating_off;
                }
            }
        } else if (_name.contains("Farm")) {
            if (_isActivated) {
                return R.drawable.watering_on;
            } else {
                return R.drawable.watering_off;
            }
        } else if (_name.contains("MediaServer")) {
            if (_isActivated) {
                return R.drawable.mediamirror_on;
            } else {
                return R.drawable.mediamirror_off;
            }
        } else if (_name.contains("GameConsole")) {
            if (_isActivated) {
                return R.drawable.gameconsole_on;
            } else {
                return R.drawable.gameconsole_off;
            }
        } else if (_name.contains("RaspberryPi")) {
            if (_isActivated) {
                return R.drawable.raspberry_on;
            } else {
                return R.drawable.raspberry_off;
            }
        }
        return R.drawable.socket;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Name: %s );(Area: %s );(Code: %s );(IsActivated: %s ))", TAG, _name, _area, _code, (_isActivated ? "1" : "0"));
    }

    protected String createShortName(String name) {
        String shortName;

        if (name.contains("_")) {
            shortName = name.substring(0, name.indexOf("_"));
        } else {
            if (name.length() > 3) {
                shortName = name.substring(0, 3);
            } else {
                shortName = name.substring(0, name.length());
            }
        }

        if (shortName.length() > 3) {
            shortName = shortName.substring(0, 3);
        }

        return shortName;
    }
}
