package guepardoapps.library.lucahome.converter;

import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.TimeString;

public final class TimeToStringConverter {

    private static final String TAG = TimeToStringConverter.class.getSimpleName();

    public static TimeString GetTimeOfString(@NonNull String timeString) {
        if (timeString.contains(":")) {
            String[] partString = timeString.split(":");
            if (partString.length == 2) {
                return new TimeString(partString[0], partString[1]);
            }
        }

        new LucaHomeLogger(TAG).Error("timeString has an error: " + timeString);
        return new TimeString("-1", "-1");
    }

    public static String GetStringOfTime(@NonNull TimeString timeString) {
        return timeString.toString();
    }
}