package guepardoapps.lucahome.common.converter.json;

import java.sql.Time;

import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.common.dto.ScheduleDto;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.enums.RaspberrySelection;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.common.tools.StringHelper;

import guepardoapps.toolset.common.enums.Weekday;

public final class JsonDataToScheduleConverter {

	private static final String TAG = JsonDataToScheduleConverter.class.getName();
	private static LucaHomeLogger _logger;

	private static String _searchParameter = "{schedule:";

	public static SerializableList<ScheduleDto> GetList(String[] stringArray,
			SerializableList<WirelessSocketDto> socketList) {
		if (StringHelper.StringsAreEqual(stringArray)) {
			return ParseStringToList(stringArray[0], socketList);
		} else {
			String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
			return ParseStringToList(usedEntry, socketList);
		}
	}

	public static ScheduleDto Get(String value, SerializableList<WirelessSocketDto> socketList) {
		if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
			if (value.contains(_searchParameter)) {
				value = value.replace(_searchParameter, "").replace("};};", "");

				String[] data = value.split("\\};");
				ScheduleDto newValue = ParseStringToValue(data, socketList);
				if (newValue != null) {
					return newValue;
				}
			}
		}

		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Error(value + " has an error!");

		return null;
	}

	private static SerializableList<ScheduleDto> ParseStringToList(String value,
			SerializableList<WirelessSocketDto> socketList) {
		if (StringHelper.GetStringCount(value, _searchParameter) > 0) {
			if (value.contains(_searchParameter)) {
				SerializableList<ScheduleDto> list = new SerializableList<ScheduleDto>();

				String[] entries = value.split("\\" + _searchParameter);
				for (String entry : entries) {
					entry = entry.replace(_searchParameter, "").replace("};};", "");

					String[] data = entry.split("\\};");
					ScheduleDto newValue = ParseStringToValue(data, socketList);
					if (newValue != null) {
						list.addValue(newValue);
					}
				}
				return list;
			}
		}

		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Error(value + " has an error!");

		return null;
	}

	private static ScheduleDto ParseStringToValue(String[] data, SerializableList<WirelessSocketDto> socketList) {
		if (data.length == 11) {
			if (data[0].contains("{Name:") && data[1].contains("{Socket:") && data[2].contains("{Gpio:")
					&& data[3].contains("{Weekday:") && data[4].contains("{Hour:") && data[5].contains("{Minute:")
					&& data[6].contains("{OnOff:") && data[7].contains("{IsTimer:") && data[8].contains("{PlaySound:")
					&& data[9].contains("{Raspberry:") && data[10].contains("{State:")) {

				String Name = data[0].replace("{Name:", "").replace("};", "");

				String socketName = data[1].replace("{Socket:", "").replace("};", "");
				WirelessSocketDto socket = null;
				for (int index = 0; index < socketList.getSize(); index++) {
					if (socketList.getValue(index).GetName().contains(socketName)) {
						socket = socketList.getValue(index);
						break;
					}
				}

				String WeekdayString = data[3].replace("{Weekday:", "").replace("};", "");
				int weekdayInteger = Integer.parseInt(WeekdayString);
				Weekday weekday = Weekday.GetById(weekdayInteger);

				String HourString = data[4].replace("{Hour:", "").replace("};", "");
				int Hour = Integer.parseInt(HourString);
				String MinuteString = data[5].replace("{Minute:", "").replace("};", "");
				int Minute = Integer.parseInt(MinuteString);
				@SuppressWarnings("deprecation")
				Time time = new Time(Hour, Minute, 0);

				String ActionString = data[6].replace("{OnOff:", "").replace("};", "");
				boolean action = ActionString.contains("1");

				String IsTimerString = data[7].replace("{IsTimer:", "").replace("};", "");
				boolean isTimer = IsTimerString.contains("1");

				String PlaySoundString = data[8].replace("{PlaySound:", "").replace("};", "");
				boolean playSound = PlaySoundString.contains("1");

				String PlayRaspberryString = data[9].replace("{Raspberry:", "").replace("};", "");
				RaspberrySelection playRaspberry = RaspberrySelection.GetById(Integer.parseInt(PlayRaspberryString));

				String IsActiveString = data[10].replace("{State:", "").replace("};", "");
				boolean isActive = IsActiveString.contains("1");

				if (!isTimer) {
					ScheduleDto newValue = new ScheduleDto(Name, socket, weekday, time, action, isTimer, playSound,
							playRaspberry, isActive);
					return newValue;
				} else {
					return null;
				}
			}
		}

		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Error("Data has an error!");

		return null;
	}
}