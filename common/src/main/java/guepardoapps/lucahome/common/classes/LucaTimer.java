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
            @NonNull String information,
            @NonNull WirelessSocket socket,
            @NonNull Weekday weekday,
            @NonNull SerializableTime time,
            SocketAction action,
            boolean isActive) {
        super(id, name, information, socket, weekday, time, action, isActive);
    }

    public LucaTimer(
            int id,
            @NonNull String name,
            @NonNull WirelessSocket socket,
            @NonNull Weekday weekday,
            @NonNull SerializableTime time,
            SocketAction action,
            boolean isActive) {
        super(id, name, socket, weekday, time, action, isActive);
    }

    @Override
    public String CommandAdd() {
        return String.format(Locale.getDefault(), "%s%s&socket=%s&gpio=%s&weekday=%s&hour=%s&minute=%s&onoff=%s&isTimer=%s&playSound=%s&playRaspberry=%s", LucaServerAction.ADD_SCHEDULE.toString(), _name, _socket.GetName(), "", _weekday, _time.HH(), _time.MM(), _action.GetFlag(), "1", "0", "1");
    }

    @Override
    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%s&socket=%s&gpio=%s&weekday=%s&hour=%s&minute=%s&onoff=%s&isTimer=%s&playSound=%s&playRaspberry=%s", LucaServerAction.UPDATE_SCHEDULE.toString(), _name, _socket.GetName(), "", _weekday, _time.HH(), _time.MM(), _action.GetFlag(), "1", "0", "1");
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
