package guepardoapps.mediamirrorv2.converter;

import android.support.annotation.NonNull;

import java.util.Calendar;

public class DateConverter {
    public static String GetDate(@NonNull Calendar date) {
        int day = date.get(Calendar.DAY_OF_MONTH);
        int month = date.get(Calendar.MONTH);
        int year = date.get(Calendar.YEAR);

        String dayString = String.valueOf(day);
        if (dayString.length() == 1) {
            dayString = "0" + dayString;
        }
        String monthString = MonthConverter.GetMonth(month);

        String string = "";
        string += dayString + "." + monthString + " " + String.valueOf(year);

        return string;
    }
}
