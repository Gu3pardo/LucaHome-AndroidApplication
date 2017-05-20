package guepardoapps.library.lucahome.common.dto;

import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.converter.BooleanToScheduleStateConverter;

import guepardoapps.library.toolset.common.classes.SerializableTime;
import guepardoapps.library.toolset.common.enums.Weekday;

public class TimerDto extends ScheduleDto {

	private static final long serialVersionUID = 146819296314931972L;

	@SuppressWarnings("unused")
	private static final String TAG = TimerDto.class.getSimpleName();

	public TimerDto(String name, WirelessSocketDto socket, Weekday weekday, SerializableTime time, boolean action,
			boolean playSound, RaspberrySelection playRaspberry, boolean isActive) {
		super(name, socket, weekday, time, action, true, playSound, playRaspberry, isActive);
	}

	public TimerDto(int drawable, String name, String information, boolean isActive) {
		super(drawable, name, information, isActive);
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
