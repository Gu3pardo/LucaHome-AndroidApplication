package guepardoapps.library.lucahome.common.dto;

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

	private String _name;
	private String _area;
	private String _code;
	private boolean _isActivated;

	private String _shortName;

	private String _setBroadcastReceiverString;
	private String _notificationBroadcastReceiverString;
	private String _deleteBroadcastReceiverString;

	private String _notificationVisibilitySharedPrefKey;

	public WirelessSocketDto(String name, String area, String code, boolean isActivated) {
		_drawable = -1;

		_name = name;
		_area = area;
		_code = code;
		_isActivated = isActivated;

		_shortName = CreateShortName(_name);

		_setBroadcastReceiverString = Broadcasts.SET_SOCKET + _name.toUpperCase(Locale.GERMAN);
		_notificationBroadcastReceiverString = Broadcasts.NOTIFICATION_SOCKET + _name.toUpperCase(Locale.GERMAN);
		_deleteBroadcastReceiverString = Broadcasts.DELETE_SOCKET + _name.toUpperCase(Locale.GERMAN);

		_notificationVisibilitySharedPrefKey = "SOCKET_" + _name + "_VISIBILITY";
	}

	public WirelessSocketDto(int drawable, String name, boolean isActivated) {
		_drawable = drawable;

		_name = name;
		_area = "";
		_code = "";
		_isActivated = isActivated;

		_shortName = CreateShortName(_name);

		_setBroadcastReceiverString = Broadcasts.SET_SOCKET + _name.toUpperCase(Locale.GERMAN);
		_notificationBroadcastReceiverString = Broadcasts.NOTIFICATION_SOCKET + _name.toUpperCase(Locale.GERMAN);
		_deleteBroadcastReceiverString = Broadcasts.DELETE_SOCKET + _name.toUpperCase(Locale.GERMAN);

		_notificationVisibilitySharedPrefKey = "SOCKET_" + _name + "_VISIBILITY";
	}

	public WirelessSocketDto() {
		_drawable = -1;

		_name = "";
		_area = "";
		_code = "";
		_isActivated = false;

		_shortName = "";

		_setBroadcastReceiverString = "";
		_notificationBroadcastReceiverString = "";
		_deleteBroadcastReceiverString = "";

		_notificationVisibilitySharedPrefKey = "";
	}

	public int GetDrawable() {
		if (_drawable != -1) {
			return _drawable;
		} else {
			return getDrawable(_name, _isActivated);
		}
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

	public boolean GetIsActivated() {
		return _isActivated;
	}

	public String GetShortName() {
		return _shortName;
	}

	public String GetSetBroadcast() {
		return _setBroadcastReceiverString;
	}

	public String GetNotificationBroadcast() {
		return _notificationBroadcastReceiverString;
	}

	public String GetDeleteBroadcast() {
		return _deleteBroadcastReceiverString;
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
		return "{" + TAG + ": {Name: " + _name + "};{Area: " + _area + "};{Code: " + _code + "};{IsActivated: "
				+ BooleanToSocketStateConverter.GetStringOfBoolean(_isActivated) + "};{SetBroadcastReceiverString: "
				+ _setBroadcastReceiverString + "};{NotificationBroadcastReceiverString: "
				+ _notificationBroadcastReceiverString + "};{DeleteBroadcastReceiverString: "
				+ _deleteBroadcastReceiverString + "};{NotificationVisibilitySharedPrefKey: "
				+ _notificationVisibilitySharedPrefKey + "}}";
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

	private String CreateShortName(String name) {
		String shortName = "";

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
