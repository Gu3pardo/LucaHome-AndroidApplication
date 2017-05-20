package guepardoapps.library.lucahome.converter;

import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

public final class BooleanToSocketStateConverter {

    private static final String TAG = BooleanToSocketStateConverter.class.getSimpleName();

    public static boolean GetBooleanState(String state) {
        if (state.matches(Constants.ACTIVATED)) {
            return true;
        } else if (state.matches(Constants.DEACTIVATED)) {
            return false;
        } else {
            LucaHomeLogger logger = new LucaHomeLogger(TAG);
            logger.Error(state + " is not supported!");
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