package guepardoapps.lucahome.common.converter.json;

import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.Tools;
import guepardoapps.lucahome.dto.InformationDto;

public final class JsonDataToInformationConverter {

	private static final String TAG = JsonDataToInformationConverter.class.getName();
	private static LucaHomeLogger _logger;

	private static String _searchParameter = "{information:";

	public static InformationDto Get(String[] stringArray) {
		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
			_logger.Debug("InformationDto entries:");
			for (String entry : stringArray) {
				_logger.Debug(entry);
			}
		}

		if (Tools.StringsAreEqual(stringArray)) {
			return ParseStringToValue(stringArray[0]);
		} else {
			String usedEntry = Tools.SelectString(stringArray, _searchParameter);
			return ParseStringToValue(usedEntry);
		}
	}

	private static InformationDto ParseStringToValue(String value) {
		if (Tools.GetStringCount(value, _searchParameter) == 1) {
			if (value.contains(_searchParameter)) {
				value = value.replace(_searchParameter, "").replace("};};", "");

				String[] data = value.split("\\};");
				if (data.length == 10) {
					if (data[0].contains("{Author:") && data[1].contains("{Company:") && data[2].contains("{Contact:")
							&& data[3].contains("{Build Date:") && data[4].contains("{Server Version:")
							&& data[5].contains("{Website Version:") && data[6].contains("{Temperature Log Version:")
							&& data[7].contains("{Android App Version:") && data[8].contains("{Android Wear Version:")
							&& data[9].contains("{Android Access Version:")) {

						String Author = data[0].replace("{Author:", "").replace("};", "");
						String Company = data[1].replace("{Company:", "").replace("};", "");
						String Contact = data[2].replace("{Contact:", "").replace("};", "");
						String Build_Date = data[3].replace("{Build Date:", "").replace("};", "");
						String Server_Version = data[4].replace("{Server Version:", "").replace("};", "");
						String Website_Version = data[5].replace("{Website Version:", "").replace("};", "");
						String Temperature_Log_Version = data[6].replace("{Temperature Log Version:", "").replace("};",
								"");
						String Android_App_Version = data[7].replace("{Android App Version:", "").replace("};", "");
						String Android_Wear_Version = data[8].replace("{Android Wear Version:", "").replace("};", "");
						String Android_Access_Version = data[9].replace("{Android Access Version:", "").replace("};",
								"");

						InformationDto newValue = new InformationDto(Author, Company, Contact, Build_Date,
								Server_Version, Website_Version, Temperature_Log_Version, Android_App_Version,
								Android_Wear_Version, Android_Access_Version);

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
}