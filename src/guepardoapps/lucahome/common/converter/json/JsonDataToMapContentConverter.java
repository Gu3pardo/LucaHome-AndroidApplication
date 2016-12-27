package guepardoapps.lucahome.common.converter.json;

import java.util.ArrayList;

import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.Tools;
import guepardoapps.lucahome.common.classes.*;
import guepardoapps.lucahome.common.enums.DrawingType;
import guepardoapps.lucahome.dto.MapContentDto;

public final class JsonDataToMapContentConverter {

	private static final String TAG = JsonDataToMapContentConverter.class.getName();
	private static LucaHomeLogger _logger;

	private static String _searchParameter = "{mapcontent";

	public static SerializableList<MapContentDto> GetList(String[] stringArray) {
		if (Tools.StringsAreEqual(stringArray)) {
			return ParseStringToList(stringArray[0]);
		} else {
			String usedEntry = Tools.SelectString(stringArray, _searchParameter);
			return ParseStringToList(usedEntry);
		}
	}

	public static MapContentDto Get(String value) {
		if (!value.contains("Error")) {
			if (Tools.GetStringCount(value, _searchParameter) == 1) {
				if (value.contains(_searchParameter)) {
					value = value.replace(_searchParameter, "").replace("};};", "");
					String[] data = value.split("\\};");
					MapContentDto newValue = ParseStringToValue(data);
					if (newValue != null) {
						return newValue;
					}
				}
			}
		}

		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Error(value + " has an error!");

		return null;
	}

	private static SerializableList<MapContentDto> ParseStringToList(String value) {
		if (!value.contains("Error")) {
			if (Tools.GetStringCount(value, _searchParameter) > 1) {
				if (value.contains(_searchParameter)) {
					SerializableList<MapContentDto> list = new SerializableList<MapContentDto>();

					String[] entries = value.split("\\" + _searchParameter);
					for (String entry : entries) {
						entry = entry.replace(_searchParameter, "").replace("};};", "").replace(":};", "");

						String[] data = entry.split("\\};");
						MapContentDto newValue = ParseStringToValue(data);
						if (newValue != null) {
							list.addValue(newValue);
						}
					}

					return list;
				}
			}
		}

		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Error(value + " has an error!");

		return null;
	}

	private static MapContentDto ParseStringToValue(String[] data) {
		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}

		if (data.length == 6) {
			if (data[0].contains("{id:") && data[1].contains("{position:") && data[2].contains("{type:")
					&& data[3].contains("{schedules:") && data[4].contains("{sockets:")
					&& data[5].contains("{temperatureArea:")) {
				String idString = data[0].replace("{id:", "").replace("};", "");
				int id = Integer.parseInt(idString);

				String positionString = data[1].replace("{position:", "").replace("};", "");
				String[] coordinates = positionString.split("\\|");
				int x = Integer.parseInt(coordinates[0]);
				int y = Integer.parseInt(coordinates[1]);

				int[] position = new int[2];
				position[0] = x;
				position[1] = y;

				String typeString = data[2].replace("{type:", "").replace("};", "");
				int typeInteger = Integer.parseInt(typeString);
				DrawingType drawingType = DrawingType.GetById(typeInteger);

				ArrayList<String> scheduleList = new ArrayList<String>();
				String scheduleString = data[3].replace("{schedules:", "").replace("};", "");
				String[] schedulesArray = scheduleString.split("\\|");
				for (String entry : schedulesArray) {
					if (entry.length() > 0) {
						scheduleList.add(entry);
					}
				}

				ArrayList<String> socketList = new ArrayList<String>();
				String socketString = data[4].replace("{sockets:", "").replace("};", "");
				String[] socketsArray = socketString.split("\\|");
				for (String entry : socketsArray) {
					if (entry.length() > 0) {
						socketList.add(entry);
					}
				}

				String temperatureString = data[5].replace("{temperatureArea:", "").replace("};", "");

				MapContentDto newValue = new MapContentDto(id, position, drawingType, scheduleList, socketList,
						temperatureString);

				_logger.Debug("newValue: " + newValue.toString());

				return newValue;
			}
		}

		_logger.Error("Data has an error!");

		return null;
	}
}