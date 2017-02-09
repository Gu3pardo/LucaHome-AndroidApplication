package guepardoapps.lucahome.common.dto;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.constants.ServerActions;
import guepardoapps.lucahome.common.converter.BooleanToSocketStateConverter;

public class WirelessSocketDto implements Serializable {

	private static final long serialVersionUID = 5579389712706446580L;

	@SuppressWarnings("unused")
	private static final String TAG = WirelessSocketDto.class.getName();

	private String _name;
	private String _area;
	private String _code;
	private boolean _isActivated;

	private String _setBroadcastReceiverString;
	private String _notificationBroadcastReceiverString;
	private String _deleteBroadcastReceiverString;

	private String _notificationVisibilitySharedPrefKey;

	public WirelessSocketDto(String name, String area, String code, boolean isActivated) {
		_name = name;
		_area = area;
		_code = code;
		_isActivated = isActivated;

		_setBroadcastReceiverString = Broadcasts.SET_SOCKET + _name.toUpperCase(Locale.GERMAN);
		_notificationBroadcastReceiverString = Broadcasts.NOTIFICATION_SOCKET + _name.toUpperCase(Locale.GERMAN);
		_deleteBroadcastReceiverString = Broadcasts.DELETE_SOCKET + _name.toUpperCase(Locale.GERMAN);

		_notificationVisibilitySharedPrefKey = "SOCKET_" + _code + "_VISIBILITY";
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

	public String toString() {
		return "{WirelessSocket: {Name: " + _name + "};{Area: " + _area + "};{Code: " + _code + "};{IsActivated: "
				+ BooleanToSocketStateConverter.GetStringOfBoolean(_isActivated) + "};{SetBroadcastReceiverString: "
				+ _setBroadcastReceiverString + "};{NotificationBroadcastReceiverString: "
				+ _notificationBroadcastReceiverString + "};{DeleteBroadcastReceiverString: "
				+ _deleteBroadcastReceiverString + "};{NotificationVisibilitySharedPrefKey: "
				+ _notificationVisibilitySharedPrefKey + "}}";
	}
}
