package guepardoapps.lucahome.common.converter;

import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.toolset.common.classes.TimeString;

public final class TimeToStringConverter {

	private static final String TAG = TimeToStringConverter.class.getName();
	private static LucaHomeLogger _logger;

	public static TimeString GetTimeOfString(String timeString) {
		if (timeString.contains(":")) {
			String[] partString = timeString.split(":");
			if (partString.length == 2) {
				return new TimeString(partString[0], partString[1]);
			}
		}

		_logger = new LucaHomeLogger(TAG);
		_logger.Error("timeString has an error: " + timeString);

		return null;
	}

	public static String GetStringOfTime(TimeString timeString) {
		if (timeString != null) {
			return timeString.toString();
		}

		_logger = new LucaHomeLogger(TAG);
		_logger.Error("timeString is null!");

		return "Error converting time to string!";
	}
}