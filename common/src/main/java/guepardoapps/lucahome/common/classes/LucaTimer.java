package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.enums.SocketAction;
import guepardoapps.lucahome.common.enums.Weekday;

public class LucaTimer extends Schedule {
    private static final long serialVersionUID = 146819296314931972L;
    private static final String TAG = LucaTimer.class.getSimpleName();

    public LucaTimer(
            int id,
            @NonNull String name,
            WirelessSocket wirelessSocket,
            WirelessSwitch wirelessSwitch,
            @NonNull Weekday weekday,
            @NonNull SerializableTime time,
            SocketAction action,
            boolean isActive,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        super(id, name, wirelessSocket, wirelessSwitch, weekday, time, action, isActive, isOnServer, serverDbAction);
    }

    @Override
    public String CommandAdd() {
        return String.format(Locale.getDefault(), "%s%s&name=%s&socket=%s&gpio=%s&switch=%s&weekday=%s&hour=%s&minute=%s&action=%s&isTimer=%s", LucaServerAction.ADD_SCHEDULE.toString(), _id, _name, _wirelessSocket.GetName(), "", _wirelessSwitch.GetName(), _weekday, _time.HH(), _time.MM(), _action.GetFlag(), "1");
    }

    @Override
    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%s&name=%s&socket=%s&gpio=%s&switch=%s&weekday=%s&hour=%s&minute=%s&action=%s&isTimer=%s&isactive=%s", LucaServerAction.UPDATE_SCHEDULE.toString(), _id, _name, _wirelessSocket.GetName(), "", _wirelessSwitch.GetName(), _weekday, _time.HH(), _time.MM(), _action.GetFlag(), "1", (_isActive ? "1" : "0"));
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
