package guepardoapps.library.lucahome.common.dto;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.ServerActions;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.converter.BooleanToScheduleStateConverter;

import guepardoapps.library.toolset.common.classes.SerializableTime;
import guepardoapps.library.toolset.common.enums.Weekday;

public class ScheduleDto implements Serializable {

	private static final long serialVersionUID = 7735669237381408318L;

	private static final String TAG = ScheduleDto.class.getSimpleName();

	protected int _drawable;

	protected String _name;
	protected String _information;
	protected WirelessSocketDto _socket;
	protected Weekday _weekday;
	protected SerializableTime _time;
	protected boolean _action;
	protected boolean _isTimer;
	protected boolean _playSound;
	protected RaspberrySelection _playRaspberry;
	protected boolean _isActive;

	private String _setBroadcastReceiverString;
	protected String _deleteBroadcastReceiverString;

	public ScheduleDto(String name, WirelessSocketDto socket, Weekday weekday, SerializableTime time, boolean action,
			boolean isTimer, boolean playSound, RaspberrySelection playRaspberry, boolean isActive) {
		_drawable = -1;

		_name = name;
		_information = "";
		_socket = socket;
		_weekday = weekday;
		_time = time;
		_action = action;
		_isTimer = isTimer;
		_playSound = playSound;
		_playRaspberry = playRaspberry;
		_isActive = isActive;

		_setBroadcastReceiverString = Broadcasts.SET_SCHEDULE + _name.toUpperCase(Locale.GERMAN);
		_deleteBroadcastReceiverString = Broadcasts.DELETE_SCHEDULE + _name.toUpperCase(Locale.GERMAN);
	}

	public ScheduleDto(int drawable, String name, String information, boolean isActive) {
		_drawable = drawable;

		_name = name;
		_information = information;
		_socket = new WirelessSocketDto();
		_weekday = Weekday.NULL;
		_time = new SerializableTime();
		_action = false;
		_isTimer = false;
		_playSound = false;
		_playRaspberry = RaspberrySelection.DUMMY;
		_isActive = isActive;

		_setBroadcastReceiverString = Broadcasts.SET_SCHEDULE + _name.toUpperCase(Locale.GERMAN);
		_deleteBroadcastReceiverString = Broadcasts.DELETE_SCHEDULE + _name.toUpperCase(Locale.GERMAN);
	}

	public int GetDrawable() {
		return _drawable;
	}

	public String GetName() {
		return _name;
	}

	public String GetInformation() {
		return _information;
	}

	public WirelessSocketDto GetSocket() {
		return _socket;
	}

	public Weekday GetWeekday() {
		return _weekday;
	}

	public SerializableTime GetTime() {
		return _time;
	}

	public boolean GetAction() {
		return _action;
	}

	public boolean IsTimer() {
		return _isTimer;
	}

	public boolean GetPlaySound() {
		return _playSound;
	}

	public RaspberrySelection GetPlayRaspberry() {
		return _playRaspberry;
	}

	public boolean IsActive() {
		return _isActive;
	}

	public String GetIsActiveString() {
		return BooleanToScheduleStateConverter.GetStringOfBoolean(_isActive);
	}

	public String GetSetBroadcast() {
		return _setBroadcastReceiverString;
	}

	public String GetDeleteBroadcast() {
		return _deleteBroadcastReceiverString;
	}

	public String GetCommandSet(boolean newState) {
		return ServerActions.SET_SCHEDULE + _name + ((newState) ? Constants.STATE_ON : Constants.STATE_OFF);
	}

	public String GetCommandAdd() {
		return ServerActions.ADD_SCHEDULE + _name + "&socket=" + _socket.GetName() + "&gpio=" + "" + "&weekday="
				+ String.valueOf(_weekday.GetInt()) + "&hour=" + String.valueOf(_time.Hour()) + "&minute="
				+ String.valueOf(_time.Minute()) + "&onoff=" + (_action ? "1" : "0") + "&isTimer="
				+ (_isTimer ? "1" : "0") + "&playSound=" + (_playSound ? "1" : "0") + "&playRaspberry="
				+ String.valueOf(_playRaspberry.GetInt());
	}

	public String GetCommandUpdate() {
		return ServerActions.UPDATE_SCHEDULE + _name + "&socket=" + _socket.GetName() + "&gpio=" + "" + "&weekday="
				+ String.valueOf(_weekday.GetInt()) + "&hour=" + String.valueOf(_time.Hour()) + "&minute="
				+ String.valueOf(_time.Minute()) + "&onoff=" + (_action ? "1" : "0") + "&isTimer="
				+ (_isTimer ? "1" : "0") + "&playSound=" + (_playSound ? "1" : "0") + "&playRaspberry="
				+ String.valueOf(_playRaspberry.GetInt()) + "&isactive=" + String.valueOf(_isActive);
	}

	public String GetCommandDelete() {
		return ServerActions.DELETE_SCHEDULE + _name;
	}

	public String GetCommandWear() {
		return "ACTION:SET:SCHEDULE:" + _name + (_isActive ? ":1" : ":0");
	}

	public String toString() {
		String socket = "";
		if (_socket != null) {
			socket = _socket.toString();
		}
		return "{" + TAG + ": {Name: " + _name + "};{WirelessSocket: " + socket + "};{Weekday: " + _weekday.toString()
				+ "};{Time: " + _time.toString() + "};{Action: " + String.valueOf(_action) + "};{isTimer: "
				+ String.valueOf(_isTimer) + "};{playSound: " + String.valueOf(_playSound) + "};{playRaspberry: "
				+ _playRaspberry.toString() + "};{IsActive: "
				+ BooleanToScheduleStateConverter.GetStringOfBoolean(_isActive) + "};{SetBroadcastReceiverString: "
				+ _setBroadcastReceiverString + "};{DeleteBroadcastReceiverString: " + _deleteBroadcastReceiverString
				+ "}}";
	}
}
