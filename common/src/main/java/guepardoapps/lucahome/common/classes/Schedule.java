package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.enums.SocketAction;
import guepardoapps.lucahome.common.enums.Weekday;

public class Schedule implements Serializable {
    private static final long serialVersionUID = 7735669237381408318L;
    private static final String TAG = Schedule.class.getSimpleName();

    protected int _id;

    protected String _name;
    protected String _information;
    protected WirelessSocket _socket;
    protected Weekday _weekday;
    protected SerializableTime _time;
    protected SocketAction _action;
    protected boolean _isActive;

    public Schedule(
            int id,
            @NonNull String name,
            @NonNull String information,
            WirelessSocket socket,
            @NonNull Weekday weekday,
            @NonNull SerializableTime time,
            @NonNull SocketAction action,
            boolean isActive) {
        _id = id;
        _name = name;
        _information = information;
        _socket = socket;
        _weekday = weekday;
        _time = time;
        _action = action;
        _isActive = isActive;
    }

    public Schedule(
            int id,
            @NonNull String name,
            WirelessSocket socket,
            @NonNull Weekday weekday,
            @NonNull SerializableTime time,
            @NonNull SocketAction action,
            boolean isActive) {
        this(
                id,
                name,
                "",
                socket,
                weekday,
                time,
                action,
                isActive);
    }

    public Schedule(
            int id,
            @NonNull String name,
            @NonNull String information,
            boolean isActive) {
        this(
                id,
                name,
                information,
                null,
                Weekday.NULL,
                new SerializableTime(),
                SocketAction.NULL,
                isActive);
    }

    public int GetId() {
        return _id;
    }

    public String GetName() {
        return _name;
    }

    public void SetName(@NonNull String name) {
        _name = name;
    }

    public String GetInformation() {
        return _information;
    }

    public void SetInformation(@NonNull String information) {
        _information = information;
    }

    public WirelessSocket GetSocket() {
        return _socket;
    }

    public void SetSocket(@NonNull WirelessSocket socket) {
        _socket = socket;
    }

    public Weekday GetWeekday() {
        return _weekday;
    }

    public void SetWeekday(@NonNull Weekday weekday) {
        _weekday = weekday;
    }

    public SerializableTime GetTime() {
        return _time;
    }

    public void SetTime(@NonNull SerializableTime time) {
        _time = time;
    }

    public SocketAction GetAction() {
        return _action;
    }

    public void SetAction(@NonNull SocketAction action) {
        _action = action;
    }

    public boolean IsActive() {
        return _isActive;
    }

    public void SetActive(boolean isActive) {
        _isActive = isActive;
    }

    public String GetIsActiveString() {
        return (_isActive ? "Active" : "Inactive");
    }

    public int Icon() {
        return R.drawable.main_image_schedule;
    }

    public String CommandSetState() {
        return String.format(Locale.getDefault(), "%s%s%s", LucaServerAction.SET_SCHEDULE.toString(), _name, (_isActive ? Constants.STATE_ON : Constants.STATE_OFF));
    }

    public String CommandChangeState() {
        return String.format(Locale.getDefault(), "%s%s%s", LucaServerAction.SET_SCHEDULE.toString(), _name, (!_isActive ? Constants.STATE_ON : Constants.STATE_OFF));
    }

    public String CommandAdd() {
        return String.format(Locale.getDefault(), "%s%s&socket=%s&gpio=%s&weekday=%s&hour=%s&minute=%s&onoff=%s&isTimer=%s&playSound=%s&playRaspberry=%s", LucaServerAction.ADD_SCHEDULE.toString(), _name, _socket.GetName(), "", _weekday, _time.HH(), _time.MM(), _action.GetFlag(), "0", "0", "1");
    }

    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%s&socket=%s&gpio=%s&weekday=%s&hour=%s&minute=%s&onoff=%s&isTimer=%s&playSound=%s&playRaspberry=%s", LucaServerAction.UPDATE_SCHEDULE.toString(), _name, _socket.GetName(), "", _weekday, _time.HH(), _time.MM(), _action.GetFlag(), "0", "0", "1");
    }

    public String CommandDelete() {
        return String.format(Locale.getDefault(), "%s%s", LucaServerAction.DELETE_SCHEDULE.toString(), _name);
    }

    public String CommandWear() {
        return String.format(Locale.getDefault(), "ACTION:SET:SCHEDULE:%s%s", _name, (_isActive ? ":1" : ":0"));
    }

    @Override
    public String toString() {
        return "{" + TAG
                + ": {Id: " + String.valueOf(_id)
                + "};{Name: " + _name
                + "};{WirelessSocket: " + (_socket != null ? _socket.toString() : "")
                + "};{Weekday: " + _weekday.toString()
                + "};{Time: " + _time.toString()
                + "};{Action: " + _action.toString()
                + "};{IsActive: " + (_isActive ? "Active" : "Inactive")
                + "}}";
    }
}
