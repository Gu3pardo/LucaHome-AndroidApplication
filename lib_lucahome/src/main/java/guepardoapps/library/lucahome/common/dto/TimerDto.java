package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.enums.LucaServerAction;
import guepardoapps.library.lucahome.converter.BooleanToScheduleStateConverter;

import guepardoapps.library.toolset.common.classes.SerializableTime;
import guepardoapps.library.toolset.common.enums.Weekday;

public class TimerDto extends ScheduleDto {

    private static final long serialVersionUID = 146819296314931972L;

    private static final String TAG = TimerDto.class.getSimpleName();

    public TimerDto(
            @NonNull String name,
            @NonNull WirelessSocketDto socket,
            @NonNull Weekday weekday,
            @NonNull SerializableTime time,
            boolean action,
            boolean playSound,
            boolean isActive) {
        super(name, socket, weekday, time, action, playSound, isActive);
    }

    public TimerDto(
            int drawable,
            @NonNull String name,
            @NonNull String information,
            boolean isActive) {
        super(drawable, name, information, isActive);
    }

    @Override
    public String GetCommandAdd() {
        return LucaServerAction.ADD_SCHEDULE.toString() + _name
                + "&socket=" + _socket.GetName()
                + "&gpio=" + ""
                + "&weekday=" + String.valueOf(_weekday.GetInt())
                + "&hour=" + String.valueOf(_time.Hour())
                + "&minute=" + String.valueOf(_time.Minute())
                + "&onoff=" + (_action ? "1" : "0")
                + "&isTimer=1"
                + "&playSound=" + (_playSound ? "1" : "0")
                + "&playRaspberry=1";
    }

    @Override
    public String GetCommandUpdate() {
        return LucaServerAction.UPDATE_SCHEDULE.toString() + _name
                + "&socket=" + _socket.GetName()
                + "&gpio=" + ""
                + "&weekday=" + String.valueOf(_weekday.GetInt())
                + "&hour=" + String.valueOf(_time.Hour())
                + "&minute=" + String.valueOf(_time.Minute())
                + "&onoff=" + (_action ? "1" : "0")
                + "&isTimer=1"
                + "&playSound=" + (_playSound ? "1" : "0")
                + "&playRaspberry=1"
                + "&isactive=" + String.valueOf(_isActive);
    }

    @Override
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
