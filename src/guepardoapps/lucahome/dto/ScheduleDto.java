package guepardoapps.lucahome.dto;

import java.io.Serializable;
import java.sql.Time;
import java.util.Locale;

import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.converter.BooleanToScheduleStateConverter;
import guepardoapps.lucahome.common.enums.RaspberrySelection;

import guepardoapps.toolset.common.enums.Weekday;

public class ScheduleDto implements Serializable {

	private static final long serialVersionUID = 7735669237381408318L;

	@SuppressWarnings("unused")
	private static final String TAG = ScheduleDto.class.getName();

	protected String _name;
	protected WirelessSocketDto _socket;
	protected Weekday _weekday;
	protected Time _time;
	protected boolean _action;
	protected boolean _isTimer;
	protected boolean _playSound;
	protected RaspberrySelection _playRaspberry;
	protected boolean _isActive;

	private String _setBroadcastReceiverString;
	protected String _deleteBroadcastReceiverString;

	public ScheduleDto(String name, WirelessSocketDto socket, Weekday weekday, Time time, boolean action,
			boolean isTimer, boolean playSound, RaspberrySelection playRaspberry, boolean isActive) {
		_name = name;
		_socket = socket;
		_weekday = weekday;
		_time = time;
		_action = action;
		_isTimer = isTimer;
		_playSound = playSound;
		_playRaspberry = playRaspberry;
		_isActive = isActive;

		_setBroadcastReceiverString = Constants.BROADCAST_SET_SCHEDULE + _name.toUpperCase(Locale.GERMAN);
		_deleteBroadcastReceiverString = Constants.BROADCAST_DELETE_SCHEDULE + _name.toUpperCase(Locale.GERMAN);
	}

	public String GetName() {
		return _name;
	}

	public WirelessSocketDto GetSocket() {
		return _socket;
	}

	public Weekday GetWeekday() {
		return _weekday;
	}

	public Time GetTime() {
		return _time;
	}

	public boolean GetAction() {
		return _action;
	}

	public boolean GetIsTimer() {
		return _isTimer;
	}

	public boolean GetPlaySound() {
		return _playSound;
	}

	public RaspberrySelection GetPlayRaspberry() {
		return _playRaspberry;
	}

	public boolean GetIsActive() {
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
		return Constants.ACTION_SET_SCHEDULE + _name + ((newState) ? Constants.STATE_ON : Constants.STATE_OFF);
	}

	@SuppressWarnings("deprecation")
	public String GetCommandAdd() {
		return Constants.ACTION_ADD_SCHEDULE + _name + "&socket=" + _socket.GetName() + "&gpio=" + "" + "&weekday="
				+ String.valueOf(_weekday.GetInt()) + "&hour=" + String.valueOf(_time.getHours()) + "&minute="
				+ String.valueOf(_time.getMinutes()) + "&onoff=" + (_action ? "1" : "0") + "&isTimer="
				+ (_isTimer ? "1" : "0") + "&playSound=" + (_playSound ? "1" : "0") + "&playRaspberry="
				+ String.valueOf(_playRaspberry.GetInt());
	}

	@SuppressWarnings("deprecation")
	public String GetCommandUpdate() {
		return Constants.ACTION_UPDATE_SCHEDULE + _name + "&socket=" + _socket.GetName() + "&gpio=" + "" + "&weekday="
				+ String.valueOf(_weekday.GetInt()) + "&hour=" + String.valueOf(_time.getHours()) + "&minute="
				+ String.valueOf(_time.getMinutes()) + "&onoff=" + (_action ? "1" : "0") + "&isTimer="
				+ (_isTimer ? "1" : "0") + "&playSound=" + (_playSound ? "1" : "0") + "&playRaspberry="
				+ String.valueOf(_playRaspberry.GetInt()) + "&isactive=" + String.valueOf(_isActive);
	}

	public String GetCommandDelete() {
		return Constants.ACTION_DELETE_SCHEDULE + _name;
	}

	public String toString() {
		String socket = "";
		if (_socket != null) {
			socket = _socket.toString();
		}
		return "{Schedule: {Name: " + _name + "};{WirelessSocket: " + socket + "};{Weekday: " + _weekday.toString()
				+ "};{Time: " + _time.toString() + "};{Action: " + String.valueOf(_action) + "};{isTimer: "
				+ String.valueOf(_isTimer) + "};{playSound: " + String.valueOf(_playSound) + "};{playRaspberry: "
				+ _playRaspberry.toString() + "};{IsActive: "
				+ BooleanToScheduleStateConverter.GetStringOfBoolean(_isActive) + "};{SetBroadcastReceiverString: "
				+ _setBroadcastReceiverString + "};{DeleteBroadcastReceiverString: " + _deleteBroadcastReceiverString
				+ "}}";
	}
}
