package guepardoapps.lucahome.common.converter;

import guepardoapps.lucahome.common.tools.LucaHomeLogger;

public final class StringToBooleanConverter {

	private static final String TAG = StringToBooleanConverter.class.getName();
	private static LucaHomeLogger _logger;

	public static boolean GetBoolean(String string) {
		if (string == "true" || string == "1") {
			return true;
		} else if (string == "false" || string == "0") {
			return false;
		} else {
			_logger = new LucaHomeLogger(TAG);
			_logger.Error(string + " is not supported!");

			return false;
		}
	}

	public static String GetString(boolean bool) {
		if (bool) {
			return "true";
		} else {
			return "false";
		}
	}
}