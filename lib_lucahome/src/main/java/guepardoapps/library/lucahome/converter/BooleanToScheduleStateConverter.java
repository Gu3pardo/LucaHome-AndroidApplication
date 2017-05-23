package guepardoapps.library.lucahome.converter;

import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

public final class BooleanToScheduleStateConverter {

    private static final String TAG = BooleanToScheduleStateConverter.class.getSimpleName();

    public static boolean GetBooleanState(@NonNull String state) {
        if (state.matches(Constants.ACTIVE)) {
            return true;
        } else if (state.matches(Constants.INACTIVE)) {
            return false;
        } else {
            new LucaHomeLogger(TAG).Error(state + " is not supported!");
            return false;
        }
    }

    public static String GetStringOfBoolean(boolean state) {
        if (state) {
            return Constants.ACTIVE;
        } else {
            return Constants.INACTIVE;
        }
    }
}