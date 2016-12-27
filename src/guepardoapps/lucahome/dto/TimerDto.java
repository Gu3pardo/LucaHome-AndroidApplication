package guepardoapps.lucahome.dto;

import java.sql.Time;

import guepardoapps.lucahome.common.converter.BooleanToScheduleStateConverter;
import guepardoapps.lucahome.common.enums.RaspberrySelection;

import guepardoapps.toolset.common.enums.Weekday;

public class TimerDto extends ScheduleDto {

	private static final long serialVersionUID = 146819296314931972L;

	@SuppressWarnings("unused")
	private static final String TAG = TimerDto.class.getName();

	public TimerDto(String name, WirelessSocketDto socket, Weekday weekday, Time time, boolean action, boolean playSound,
			RaspberrySelection playRaspberry, boolean isActive) {
		super(name, socket, weekday, time, action, true, playSound, playRaspberry, isActive);
	}

	@Override
	public String toString() {
		return "{Timer: {Name: " + _name + "};{WirelessSocket: " + _socket.toString() + "};{Weekday: "
				+ _weekday.toString() + "};{Time: " + _time.toString() + "};{Action: " + String.valueOf(_action)
				+ "};{isTimer: " + String.valueOf(_isTimer) + "};{playSound: " + String.valueOf(_playSound)
				+ "};{playRaspberry: " + _playRaspberry.toString() + "};{IsActive: "
				+ BooleanToScheduleStateConverter.GetStringOfBoolean(_isActive) + "};{DeleteBroadcastReceiverString: "
				+ _deleteBroadcastReceiverString + "}}";
	}
}
