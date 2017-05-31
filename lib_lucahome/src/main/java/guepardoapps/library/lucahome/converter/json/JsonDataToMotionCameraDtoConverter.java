package guepardoapps.library.lucahome.converter.json;

import java.util.Locale;

import guepardoapps.library.lucahome.common.dto.MotionCameraDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.StringHelper;

public final class JsonDataToMotionCameraDtoConverter {

	private static final String TAG = JsonDataToMotionCameraDtoConverter.class.getSimpleName();
	private static LucaHomeLogger _logger;

	private static String _searchParameter = "{MotionData:";

	public static SerializableList<MotionCameraDto> GetList(String[] stringArray) {
		if (StringHelper.StringsAreEqual(stringArray)) {
			return ParseStringToList(stringArray[0]);
		} else {
			String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
			return ParseStringToList(usedEntry);
		}
	}

	public static MotionCameraDto Get(String value) {
		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Debug("MotionCameraDto Get");
		_logger.Debug(value);

		if (!value.contains("Error")) {
			if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
				if (value.contains(_searchParameter)) {
					value = value.replace(_searchParameter, "").replace("};};", "");
					_logger.Debug(value);

					String[] data = value.split("\\};");
					MotionCameraDto newValue = ParseStringToValue(data);
					if (newValue != null) {
						return newValue;
					} else {
						_logger.Error("NewValue is null!");
					}
				} else {
					_logger.Error("Value does not contain " + _searchParameter);
				}
			} else {
				_logger.Error("Value contains more then 1x " + _searchParameter);
			}
		} else {
			_logger.Error("value contains error!");
		}

		_logger.Error(value + " has an error!");

		return null;
	}

	private static SerializableList<MotionCameraDto> ParseStringToList(String value) {
		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Debug("SerializableList<MotionCameraDto> ParseStringToList");
		_logger.Debug(value);

		if (!value.contains("Error")) {
			if (StringHelper.GetStringCount(value, _searchParameter) > 0) {
				if (value.contains(_searchParameter)) {
					SerializableList<MotionCameraDto> list = new SerializableList<>();

					String[] entries = value.split("\\" + _searchParameter);
					for (String entry : entries) {
						entry = entry.replace(_searchParameter, "").replace("};};", "");

						String[] data = entry.split("\\};");
						MotionCameraDto newValue = ParseStringToValue(data);
						if (newValue != null) {
							list.addValue(newValue);
						} else {
							_logger.Error("NewValue is null!");
						}
					}

					return list;
				} else {
					_logger.Error("Value does not contain " + _searchParameter);
				}
			} else {
				_logger.Error("Value does not contain more then 0x " + _searchParameter);
			}
		} else {
			_logger.Error("value contains error!");
		}

		_logger.Error(value + " has an error!");

		return new SerializableList<>();
	}

	private static MotionCameraDto ParseStringToValue(String[] data) {
		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Debug("MotionCameraDto ParseStringToValue");
		_logger.Debug("Data has size " + String.valueOf(data.length));
		int index = 0;
		for (String entry : data) {
			_logger.Debug("Index " + String.valueOf(index));
			_logger.Debug(entry);
			index++;
		}

		if (data.length == 4) {
			if (data[0].contains("{State:") && data[1].contains("{URL:") && data[2].contains("{MotionEvents:")
					&& data[3].contains("{Control:")) {

				String stateString = data[0].replace("{State:", "").replace("};", "");
				boolean state = stateString.contains("1");

				String url = data[1].replace("{URL:", "").replace("};", "");

				String motionEvents = data[2].replace("{MotionEvents:", "").replace("};", "");
				String[] motionEventsData = motionEvents.split("\\},");
				SerializableList<String> events = new SerializableList<>();
				for (String event : motionEventsData) {
					event = event.replace("{Event:", "").replace("},", "");
					events.addValue(event);
				}

				String controlStateString = data[3].replace("{Control:", "").replace("};", "");
				boolean controlState = controlStateString.contains("1");

				MotionCameraDto newValue = new MotionCameraDto(state, url, events, controlState);
				_logger.Debug(String.format(Locale.getDefault(), "New MotionCameraDto %s", newValue));

				return newValue;
			} else {
				_logger.Error("Data contains invalid parameter");
			}
		} else {
			_logger.Error("Data has invalid length " + String.valueOf(data.length));
		}

		_logger.Error("Data has an error!");

		return null;
	}
}