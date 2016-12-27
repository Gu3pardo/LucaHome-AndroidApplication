package guepardoapps.lucahome.common.converter.json;

import java.util.ArrayList;

import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.Tools;
import guepardoapps.lucahome.common.classes.Sound;

public final class JsonDataToSoundConverter {

	private static final String TAG = JsonDataToSoundConverter.class.getName();
	private static LucaHomeLogger _logger;

	private static String _searchParameter = "{soundfile:";

	public static ArrayList<Sound> GetList(String value) {
		if (Tools.GetStringCount(value, _searchParameter) > 1) {
			if (value.contains(_searchParameter)) {
				ArrayList<Sound> list = new ArrayList<Sound>();

				String[] entries = value.split("\\" + _searchParameter);
				for (String entry : entries) {
					Sound newValue = Get(entry);
					if (newValue != null) {
						list.add(newValue);
					}
				}
				return list;
			}
		}

		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Error("GetList " + value + " has an error!");

		return null;
	}

	public static Sound Get(String value) {
		value = value.replace(_searchParameter, "").replace("};};", "").replace("};", "");

		if (value.endsWith(".mp3") && !value.contains("{") && !value.contains("}")) {

			Sound newValue = new Sound(value, false);
			return newValue;
		}

		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Error("Get: " + value + " has an error!");

		return null;
	}
}