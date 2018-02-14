package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings({"WeakerAccess"})
public class CalendarEntry implements Serializable {
    private static final String Tag = CalendarEntry.class.getSimpleName();

    private UUID _uuid;
    private String _title;
    private Calendar _startDate;
    private Calendar _endDate;
    private boolean _isAllDay;

    public CalendarEntry(
            @NonNull UUID uuid,
            @NonNull String title,
            @NonNull Calendar startDate,
            @NonNull Calendar endDate,
            boolean isAllDay) {
        _uuid = uuid;
        _title = title;
        _startDate = startDate;
        _endDate = endDate;
        _isAllDay = isAllDay;
    }

    public UUID GetUuid() {
        return _uuid;
    }

    public String GetTitle() {
        return _title;
    }

    public Calendar GetStartDate() {
        return _startDate;
    }

    public Calendar GetEndDate() {
        return _endDate;
    }

    public boolean IsAllDay() {
        return _isAllDay;
    }

    public boolean IsNow() {
        return StartIsBeforeNow() && EndIsAfterNow();
    }

    public boolean StartIsBeforeNow() {
        Calendar calendar = Calendar.getInstance();
        return _startDate.get(Calendar.YEAR) < calendar.get(Calendar.YEAR)
                || (_startDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _startDate.get(Calendar.MONTH) < calendar.get(Calendar.MONTH))
                || (_startDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _startDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _startDate.get(Calendar.DAY_OF_MONTH) < calendar.get(Calendar.DAY_OF_MONTH))
                || (_startDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _startDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _startDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                && _startDate.get(Calendar.HOUR_OF_DAY) < calendar.get(Calendar.HOUR_OF_DAY))
                || (_startDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _startDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _startDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                && _startDate.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY)
                && _startDate.get(Calendar.MINUTE) < calendar.get(Calendar.MINUTE))
                || (_startDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _startDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _startDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                && _startDate.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY)
                && _startDate.get(Calendar.MINUTE) == calendar.get(Calendar.MINUTE)
                && _startDate.get(Calendar.SECOND) < calendar.get(Calendar.SECOND));
    }

    public boolean StartIsAfterNow() {
        Calendar calendar = Calendar.getInstance();
        return _startDate.get(Calendar.YEAR) > calendar.get(Calendar.YEAR)
                || (_startDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _startDate.get(Calendar.MONTH) > calendar.get(Calendar.MONTH))
                || (_startDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _startDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _startDate.get(Calendar.DAY_OF_MONTH) > calendar.get(Calendar.DAY_OF_MONTH))
                || (_startDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _startDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _startDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                && _startDate.get(Calendar.HOUR_OF_DAY) > calendar.get(Calendar.HOUR_OF_DAY))
                || (_startDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _startDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _startDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                && _startDate.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY)
                && _startDate.get(Calendar.MINUTE) > calendar.get(Calendar.MINUTE))
                || (_startDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _startDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _startDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                && _startDate.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY)
                && _startDate.get(Calendar.MINUTE) == calendar.get(Calendar.MINUTE)
                && _startDate.get(Calendar.SECOND) > calendar.get(Calendar.SECOND));
    }

    public boolean EndIsBeforeNow() {
        Calendar calendar = Calendar.getInstance();
        return _endDate.get(Calendar.YEAR) < calendar.get(Calendar.YEAR)
                || (_endDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _endDate.get(Calendar.MONTH) < calendar.get(Calendar.MONTH))
                || (_endDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _endDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _endDate.get(Calendar.DAY_OF_MONTH) < calendar.get(Calendar.DAY_OF_MONTH))
                || (_endDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _endDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _endDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                && _endDate.get(Calendar.HOUR_OF_DAY) < calendar.get(Calendar.HOUR_OF_DAY))
                || (_endDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _endDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _endDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                && _endDate.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY)
                && _endDate.get(Calendar.MINUTE) < calendar.get(Calendar.MINUTE))
                || (_endDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _endDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _endDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                && _endDate.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY)
                && _endDate.get(Calendar.MINUTE) == calendar.get(Calendar.MINUTE)
                && _endDate.get(Calendar.SECOND) < calendar.get(Calendar.SECOND));
    }

    public boolean EndIsAfterNow() {
        Calendar calendar = Calendar.getInstance();
        return _endDate.get(Calendar.YEAR) > calendar.get(Calendar.YEAR)
                || (_endDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _endDate.get(Calendar.MONTH) > calendar.get(Calendar.MONTH))
                || (_endDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _endDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _endDate.get(Calendar.DAY_OF_MONTH) > calendar.get(Calendar.DAY_OF_MONTH))
                || (_endDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _endDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _endDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                && _endDate.get(Calendar.HOUR_OF_DAY) > calendar.get(Calendar.HOUR_OF_DAY))
                || (_endDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _endDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _endDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                && _endDate.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY)
                && _endDate.get(Calendar.MINUTE) > calendar.get(Calendar.MINUTE))
                || (_endDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && _endDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && _endDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                && _endDate.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY)
                && _endDate.get(Calendar.MINUTE) == calendar.get(Calendar.MINUTE)
                && _endDate.get(Calendar.SECOND) > calendar.get(Calendar.SECOND));
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Title\":\"%s\",\"StartData\":\"%s\",\"EndDate\":\"%s\",\"IsAllDay\":\"%s\"}",
                Tag, _uuid, _title, _startDate, _endDate, _isAllDay);
    }
}
