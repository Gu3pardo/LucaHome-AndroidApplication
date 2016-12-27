package guepardoapps.lucahome.common.converter.json;

import java.util.Calendar;

import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.Tools;
import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.dto.BirthdayDto;

public final class JsonDataToBirthdayConverter {

	private static final String TAG = JsonDataToBirthdayConverter.class.getName();
	private static LucaHomeLogger _logger;

	private static String _searchParameter = "{birthday:";

	public static SerializableList<BirthdayDto> GetList(String[] stringArray) {
		if (Tools.StringsAreEqual(stringArray)) {
			return ParseStringToList(stringArray[0]);
		} else {
			String usedEntry = Tools.SelectString(stringArray, _searchParameter);
			return ParseStringToList(usedEntry);
		}
	}

	public static BirthdayDto Get(String value) {
		if (!value.contains("Error")) {
			if (Tools.GetStringCount(value, _searchParameter) == 1) {
				if (value.contains(_searchParameter)) {
					value = value.replace(_searchParameter, "").replace("};};", "");

					String[] data = value.split("\\};");
					BirthdayDto newValue = ParseStringToValue(data);
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

	private static SerializableList<BirthdayDto> ParseStringToList(String value) {
		if (!value.contains("Error")) {
			if (Tools.GetStringCount(value, _searchParameter) > 1) {
				if (value.contains(_searchParameter)) {
					SerializableList<BirthdayDto> list = new SerializableList<BirthdayDto>();

					String[] entries = value.split("\\" + _searchParameter);
					for (String entry : entries) {
						entry = entry.replace(_searchParameter, "").replace("};};", "");

						String[] data = entry.split("\\};");
						BirthdayDto newValue = ParseStringToValue(data);
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

	private static BirthdayDto ParseStringToValue(String[] data) {
		if (data.length == 5) {
			if (data[0].contains("{id:") && data[1].contains("{name:") && data[2].contains("{day:")
					&& data[3].contains("{month:") && data[4].contains("{year:")) {

				String idString = data[0].replace("{id:", "").replace("};", "");
				int id = Integer.parseInt(idString);

				String name = data[1].replace("{name:", "").replace("};", "");

				String dayString = data[2].replace("{day:", "").replace("};", "");
				int day = Integer.parseInt(dayString);
				String monthString = data[3].replace("{month:", "").replace("};", "");
				int month = Integer.parseInt(monthString);
				String yearString = data[4].replace("{year:", "").replace("};", "");
				int year = Integer.parseInt(yearString);
				Calendar birthday = Calendar.getInstance();
				birthday.set(Calendar.DAY_OF_MONTH, day);
				birthday.set(Calendar.MONTH, month - 1);
				birthday.set(Calendar.YEAR, year);

				BirthdayDto newValue = new BirthdayDto(name, birthday, id);
				return newValue;
			}
		}

		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Error("Data has an error!");

		return null;
	}
}