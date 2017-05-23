package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.ServerActions;
import guepardoapps.library.lucahome.converter.BooleanToSocketStateConverter;

public class WirelessSocketDto implements Serializable {

    private static final long serialVersionUID = 5579389712706446580L;

    private static final String TAG = WirelessSocketDto.class.getSimpleName();

    private int _drawable;
    private int _id;

    private String _name;
    private String _area;
    private String _code;
    private boolean _isActivated;

    private String _shortName;

    private String _notificationBroadcastReceiverString;
    private String _notificationVisibilitySharedPrefKey;

    public WirelessSocketDto(
            int drawable,
            int id,
            @NonNull String name,
            @NonNull String area,
            @NonNull String code,
            boolean isActivated) {
        _drawable = drawable;
        _id = id;

        _name = name;
        _area = area;
        _code = code;
        _isActivated = isActivated;

        _shortName = createShortName(_name);

        _notificationBroadcastReceiverString = Broadcasts.NOTIFICATION_SOCKET + _name.toUpperCase(Locale.GERMAN);
        _notificationVisibilitySharedPrefKey = "SOCKET_" + _name + "_VISIBILITY";
    }

    public WirelessSocketDto(
            int id,
            @NonNull String name,
            @NonNull String area,
            @NonNull String code,
            boolean isActivated) {
        this(-1, id, name, area, code, isActivated);
    }

    public WirelessSocketDto(
            int drawable,
            int id,
            @NonNull String name,
            boolean isActivated) {
        this(drawable, id, name, "", "", isActivated);
    }

    public WirelessSocketDto() {
        this(-1, -1, "", "", "", false);
    }

    public int GetDrawable() {
        if (_drawable != -1) {
            return _drawable;
        } else {
            return getDrawable(_name, _isActivated);
        }
    }

    public int GetId() {
        return _id;
    }

    public String GetName() {
        return _name;
    }

    public String GetArea() {
        return _area;
    }

    public String GetCode() {
        return _code;
    }

    public boolean IsActivated() {
        return _isActivated;
    }

    public String GetShortName() {
        return _shortName;
    }

    public String GetNotificationBroadcast() {
        return _notificationBroadcastReceiverString;
    }

    public String GetNotificationVisibilitySharedPrefKey() {
        return _notificationVisibilitySharedPrefKey;
    }

    public String GetCommandSet(boolean newState) {
        return ServerActions.SET_SOCKET + _name + ((newState) ? Constants.STATE_ON : Constants.STATE_OFF);
    }

    public String GetCommandAdd() {
        return ServerActions.ADD_SOCKET + _name + "&area=" + _area + "&code=" + _code;
    }

    public String GetCommandUpdate() {
        return ServerActions.UPDATE_SOCKET + _name + "&area=" + _area + "&code=" + _code + "&isactivated="
                + String.valueOf(_isActivated);
    }

    public String GetCommandDelete() {
        return ServerActions.DELETE_SOCKET + _name;
    }

    public String GetCommandWear() {
        return "ACTION:SET:SOCKET:" + _name + (_isActivated ? ":0" : ":1");
    }

    public String toString() {
        return "{" + TAG
                + ": {Name: " + _name
                + "};{Id: " + String.valueOf(_id)
                + "};{Area: " + _area
                + "};{Code: " + _code
                + "};{IsActivated: " + BooleanToSocketStateConverter.GetStringOfBoolean(_isActivated)
                + "};{SetBroadcastReceiverString: " + _notificationBroadcastReceiverString
                + "};{NotificationVisibilitySharedPrefKey: " + _notificationVisibilitySharedPrefKey + "}}";
    }

    private int getDrawable(String name, boolean isActivated) {
        if (name.contains("TV")) {
            if (isActivated) {
                return R.drawable.tv_on;
            } else {
                return R.drawable.tv_off;
            }
        } else if (name.contains("Light")) {
            if (name.contains("Sleeping")) {
                if (isActivated) {
                    return R.drawable.bed_light_on;
                } else {
                    return R.drawable.bed_light_off;
                }
            } else {
                if (isActivated) {
                    return R.drawable.light_on;
                } else {
                    return R.drawable.light_off;
                }
            }
        } else if (name.contains("Sound")) {
            if (name.contains("Sleeping")) {
                if (isActivated) {
                    return R.drawable.bed_sound_on;
                } else {
                    return R.drawable.bed_sound_off;
                }
            } else if (name.contains("Living")) {
                if (isActivated) {
                    return R.drawable.sound_on;
                } else {
                    return R.drawable.sound_off;
                }
            }
        } else if (name.contains("PC")) {
            if (isActivated) {
                return R.drawable.laptop_on;
            } else {
                return R.drawable.laptop_off;
            }
        } else if (name.contains("Printer")) {
            if (isActivated) {
                return R.drawable.printer_on;
            } else {
                return R.drawable.printer_off;
            }
        } else if (name.contains("Storage")) {
            if (isActivated) {
                return R.drawable.storage_on;
            } else {
                return R.drawable.storage_off;
            }
        } else if (name.contains("Heating")) {
            if (name.contains("Bed")) {
                if (isActivated) {
                    return R.drawable.bed_heating_on;
                } else {
                    return R.drawable.bed_heating_off;
                }
            }
        } else if (name.contains("Farm")) {
            if (isActivated) {
                return R.drawable.watering_on;
            } else {
                return R.drawable.watering_off;
            }
        } else if (name.contains("MediaMirror")) {
            if (isActivated) {
                return R.drawable.mediamirror_on;
            } else {
                return R.drawable.mediamirror_off;
            }
        } else if (name.contains("GameConsole")) {
            if (isActivated) {
                return R.drawable.gameconsole_on;
            } else {
                return R.drawable.gameconsole_off;
            }
        }
        return -1;
    }

    private String createShortName(String name) {
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
