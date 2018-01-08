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
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Schedule implements Serializable, ILucaClass {
    private static final long serialVersionUID = 7735669237381408318L;
    private static final String TAG = Schedule.class.getSimpleName();

    protected int _id;
    protected String _name;
    protected WirelessSocket _wirelessSocket;
    protected WirelessSwitch _wirelessSwitch;
    protected Weekday _weekday;
    protected SerializableTime _time;
    protected SocketAction _action;
    protected boolean _isActive;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public Schedule(
            int id,
            @NonNull String name,
            WirelessSocket wirelessSocket,
            WirelessSwitch wirelessSwitch,
            @NonNull Weekday weekday,
            @NonNull SerializableTime time,
            @NonNull SocketAction action,
            boolean isActive,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _id = id;
        _name = name;
        _wirelessSocket = wirelessSocket;
        _wirelessSwitch = wirelessSwitch;
        _weekday = weekday;
        _time = time;
        _action = action;
        _isActive = isActive;

        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    public Schedule(
            int id,
            @NonNull String name,
            boolean isActive,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        this(
                id,
                name,
                null,
                null,
                Weekday.NULL,
                new SerializableTime(),
                SocketAction.NULL,
                isActive,
                isOnServer,
                serverDbAction);
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

    public WirelessSocket GetWirelessSocket() {
        return _wirelessSocket;
    }

    public void SetWirelessSocket(@NonNull WirelessSocket wirelessSocket) {
        _wirelessSocket = wirelessSocket;
    }

    public WirelessSwitch GetWirelessSwitch() {
        return _wirelessSwitch;
    }

    public void SetWirelessSwitch(@NonNull WirelessSwitch wirelessSwitch) {
        _wirelessSwitch = wirelessSwitch;
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

    public int GetIcon() {
        return R.drawable.main_image_schedule;
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

    public String CommandSetState() {
        return String.format(Locale.getDefault(), "%s%s%s", LucaServerAction.SET_SCHEDULE.toString(), _name, (_isActive ? Constants.STATE_ON : Constants.STATE_OFF));
    }

    public String CommandChangeState() {
        return String.format(Locale.getDefault(), "%s%s%s", LucaServerAction.SET_SCHEDULE.toString(), _name, (!_isActive ? Constants.STATE_ON : Constants.STATE_OFF));
    }

    @Override
    public String CommandAdd() {
        return String.format(Locale.getDefault(), "%s%s&name=%s&socket=%s&gpio=%s&switch=%s&weekday=%s&hour=%s&minute=%s&action=%s&isTimer=%s", LucaServerAction.ADD_SCHEDULE.toString(), _id, _name, _wirelessSocket.GetName(), "", _wirelessSwitch.GetName(), _weekday, _time.HH(), _time.MM(), _action.GetFlag(), "0");
    }

    @Override
    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%s&name=%s&socket=%s&gpio=%s&switch=%s&weekday=%s&hour=%s&minute=%s&action=%s&isTimer=%s&isactive=%s", LucaServerAction.UPDATE_SCHEDULE.toString(), _id, _name, _wirelessSocket.GetName(), "", _wirelessSwitch.GetName(), _weekday, _time.HH(), _time.MM(), _action.GetFlag(), "0", (_isActive ? "1" : "0"));
    }

    @Override
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
                + "};{WirelessSocket: " + (_wirelessSocket != null ? _wirelessSocket.toString() : "")
                + "};{WirelessSwitch: " + (_wirelessSwitch != null ? _wirelessSwitch.toString() : "")
                + "};{Weekday: " + _weekday.toString()
                + "};{Time: " + _time.toString()
                + "};{Action: " + _action.toString()
                + "};{IsActive: " + (_isActive ? "Active" : "Inactive")
                + "}}";
    }
}
