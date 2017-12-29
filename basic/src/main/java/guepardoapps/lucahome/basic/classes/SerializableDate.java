package guepardoapps.lucahome.basic.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;

public class SerializableDate implements Serializable {
    private static final String TAG = SerializableDate.class.getSimpleName();

    private int _year;
    private int _month;
    private int _dayOfMonth;

    public SerializableDate(int year, int month, int dayOfMonth) {
        _year = year;
        _month = month;
        _dayOfMonth = dayOfMonth;
    }

    public SerializableDate(@NonNull String date) {
        String[] dateArray = date.split("\\-");
        if (dateArray.length == 3) {
            try {
                _year = Integer.parseInt(dateArray[0].replace("-", ""));
                _month = Integer.parseInt(dateArray[1].replace("-", ""));
                _dayOfMonth = Integer.parseInt(dateArray[2].replace("-", ""));
            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.getMessage());

                Calendar now = Calendar.getInstance();
                _year = now.get(Calendar.YEAR);
                _month = now.get(Calendar.MONTH) + 1;
                _dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
            }
        } else {
            Logger.getInstance().Warning(TAG, String.format(Locale.getDefault(), "Invalid data count %d!", dateArray.length));

            Calendar now = Calendar.getInstance();
            _year = now.get(Calendar.YEAR);
            _month = now.get(Calendar.MONTH) + 1;
            _dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        }
    }

    public SerializableDate() {
        Calendar now = Calendar.getInstance();
        _year = now.get(Calendar.YEAR);
        _month = now.get(Calendar.MONTH) + 1;
        _dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
    }

    public int Year() {
        return _year;
    }

    public int Month() {
        return _month;
    }

    public int DayOfMonth() {
        return _dayOfMonth;
    }

    public String YYYY() {
        return String.format(Locale.getDefault(), "%04d", _year);
    }

    public String MM() {
        return String.format(Locale.getDefault(), "%02d", _month);
    }

    public String DD() {
        return String.format(Locale.getDefault(), "%02d", _dayOfMonth);
    }

    public String YYYYMMDD() {
        return YYYY() + "-" + MM() + "-" + DD();
    }

    public String DDMMYYYY() {
        return DD() + "." + MM() + "." + YYYY();
    }

    public boolean isAfter(@NonNull SerializableDate compareDate) {
        return compareDate.calculateMillis() > calculateMillis();
    }

    public boolean isBefore(@NonNull SerializableDate compareDate) {
        return compareDate.calculateMillis() < calculateMillis();
    }

    public boolean isAfterNow() {
        return nowTimeInMillis() > calculateMillis();
    }

    public boolean isBeforeNow() {
        return nowTimeInMillis() < calculateMillis();
    }

    @Override
    public String toString() {
        return YYYYMMDD();
    }

    private long nowTimeInMillis() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private long calculateMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(_year, _month - 1, _dayOfMonth);
        return calendar.getTimeInMillis();
    }
}
