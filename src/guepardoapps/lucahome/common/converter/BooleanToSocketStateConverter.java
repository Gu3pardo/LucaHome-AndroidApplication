package guepardoapps.lucahome.common.converter;

import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.constants.Constants;

public final class BooleanToSocketStateConverter {

	private static final String TAG = BooleanToSocketStateConverter.class.getName();
	private static LucaHomeLogger _logger;

	public static boolean GetBooleanState(String state) {
		if (state == Constants.ACTIVATED) {
			return true;
		} else if (state == Constants.DEACTIVATED) {
			return false;
		} else {
			_logger = new LucaHomeLogger(TAG);
			_logger.Error(state + " is not supported!");

			return false;
		}
	}

	public static String GetStringOfBoolean(boolean state) {
		if (state) {
			return Constants.ACTIVATED;
		} else {
			return Constants.DEACTIVATED;
		}
	}
}