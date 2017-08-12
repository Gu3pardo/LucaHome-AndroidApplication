package guepardoapps.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.enums.SocketAction;
import guepardoapps.lucahome.common.enums.Weekday;

public class ScheduleDto implements Serializable {
    private static final long serialVersionUID = 950669237381408318L;
    private static final String TAG = ScheduleDto.class.getSimpleName();

    public enum Action {Add, Update}

    private int _id;

    private String _name;
    private WirelessSocket _socket;
    private Weekday _weekday;
    private SerializableTime _time;
    private SocketAction _socketAction;

    private Action _action;

    public ScheduleDto(
            int id,
            @NonNull String name,
            WirelessSocket socket,
            Weekday weekday,
            @NonNull SerializableTime time,
            @NonNull SocketAction socketAction,
            @NonNull Action action) {
        _id = id;
        _name = name;
        _socket = socket;
        _weekday = weekday;
        _time = time;
        _socketAction = socketAction;
        _action = action;
    }

    public int GetId() {
        return _id;
    }

    public String GetName() {
        return _name;
    }

    public WirelessSocket GetSocket() {
        return _socket;
    }

    public Weekday GetWeekday() {
        return _weekday;
    }

    public SerializableTime GetTime() {
        return _time;
    }

    public SocketAction GetSocketAction() {
        return _socketAction;
    }

    public Action GetAction() {
        return _action;
    }

    @Override
    public String toString() {
        return "{" + TAG
                + ": {Id: " + String.valueOf(_id)
                + "};{Name: " + _name
                + "};{WirelessSocket: " + (_socket != null ? _socket.toString() : "")
                + "};{Weekday: " + (_weekday != null ? _weekday.toString() : "")
                + "};{Time: " + _time.toString()
                + "};{SocketAction: " + _socketAction.toString()
                + "};{Action: " + _action.toString()
                + "}}";
    }
}
