package guepardoapps.lucahome.basic.dto;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import android.support.annotation.NonNull;
import android.text.format.DateFormat;

@SuppressWarnings({"deprecation", "unused"})
public class CalendarEntryDto implements Serializable {
    private static final long serialVersionUID = 4362557847817468433L;

    private static final String TAG = CalendarEntryDto.class.getSimpleName();

    private String _title;
    private Date _begin;
    private Date _end;
    private boolean _allDay;

    public CalendarEntryDto(
            @NonNull String title,
            @NonNull Date begin,
            @NonNull Date end,
            boolean allDay) {
        _title = title;
        _begin = begin;
        _end = end;
        _allDay = allDay;
    }

    public String GetTitle() {
        return _title;
    }

    public String GetMirrorText() {
        CharSequence beginResult = DateFormat.format("dd.MM.yy HH:mm", _begin);
        CharSequence endResult = DateFormat.format("dd.MM.yy HH:mm", _end);
        return String.format("%s   %s to %s", _title, beginResult, endResult);
    }

    public Date GetBegin() {
        return _begin;
    }

    public Date GetEnd() {
        return _end;
    }

    public boolean IsAllDay() {
        return _allDay;
    }

    public boolean IsToday() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        return _begin.getDay() == now.getDay() && _begin.getMonth() == now.getMonth() && _begin.getYear() == now.getYear();
    }

    public boolean BeginIsAfterNow() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        return _begin.after(now);
    }

    public boolean EndIsAfterNow() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        return _end.after(now);
    }

    @Override
    public String toString() {
        return "{" + TAG
                + ": {Title: " + _title
                + "};{Date: {Begin: " + _begin.toString() + "};{End: " + _end.toString() + "}};"
                + "{IsAllDay: " + String.valueOf(_allDay) + "};}";
    }
}
