package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.enums.LucaServerAction;
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
    protected boolean _playSound;
    protected boolean _isActive;

    public ScheduleDto(
            @NonNull String name,
            @NonNull WirelessSocketDto socket,
            @NonNull Weekday weekday,
            @NonNull SerializableTime time,
            boolean action,
            boolean playSound,
            boolean isActive) {
        _drawable = -1;

        _name = name;
        _information = "";
        _socket = socket;
        _weekday = weekday;
        _time = time;
        _action = action;
        _playSound = playSound;
        _isActive = isActive;
    }

    public ScheduleDto(
            int drawable,
            @NonNull String name,
            @NonNull String information,
            boolean isActive) {
        _drawable = drawable;

        _name = name;
        _information = information;
        _socket = new WirelessSocketDto();
        _weekday = Weekday.NULL;
        _time = new SerializableTime();
        _action = false;
        _playSound = false;
        _isActive = isActive;
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

    public boolean GetPlaySound() {
        return _playSound;
    }

    public boolean IsActive() {
        return _isActive;
    }

    public String GetIsActiveString() {
        return BooleanToScheduleStateConverter.GetStringOfBoolean(_isActive);
    }

    public String GetCommandSet(boolean newState) {
        return LucaServerAction.SET_SCHEDULE.toString() + _name + ((newState) ? Constants.STATE_ON : Constants.STATE_OFF);
    }

    public String GetCommandAdd() {
        return LucaServerAction.ADD_SCHEDULE.toString() + _name
                + "&socket=" + _socket.GetName()
                + "&gpio=" + ""
                + "&weekday=" + String.valueOf(_weekday.GetInt())
                + "&hour=" + String.valueOf(_time.Hour())
                + "&minute=" + String.valueOf(_time.Minute())
                + "&onoff=" + (_action ? "1" : "0")
                + "&isTimer=0"
                + "&playSound=" + (_playSound ? "1" : "0")
                + "&playRaspberry=1";
    }

    public String GetCommandUpdate() {
        return LucaServerAction.UPDATE_SCHEDULE.toString() + _name
                + "&socket=" + _socket.GetName()
                + "&gpio=" + ""
                + "&weekday=" + String.valueOf(_weekday.GetInt())
                + "&hour=" + String.valueOf(_time.Hour())
                + "&minute=" + String.valueOf(_time.Minute())
                + "&onoff=" + (_action ? "1" : "0")
                + "&isTimer=0"
                + "&playSound=" + (_playSound ? "1" : "0")
                + "&playRaspberry=1"
                + "&isactive=" + String.valueOf(_isActive);
    }

    public String GetCommandDelete() {
        return LucaServerAction.DELETE_SCHEDULE.toString() + _name;
    }

    public String GetCommandWear() {
        return "ACTION:SET:SCHEDULE:" + _name + (_isActive ? ":1" : ":0");
    }

    public String toString() {
        return "{" + TAG
                + ": {Name: " + _name
                + "};{WirelessSocket: " + (_socket == null ? "" : _socket.toString())
                + "};{Weekday: " + _weekday.toString()
                + "};{Time: " + _time.toString()
                + "};{Action: " + String.valueOf(_action)
                + "};{playSound: " + String.valueOf(_playSound)
                + "};{IsActive: " + BooleanToScheduleStateConverter.GetStringOfBoolean(_isActive)
                + "}}";
    }
}
