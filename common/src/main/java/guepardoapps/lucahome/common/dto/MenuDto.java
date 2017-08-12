package guepardoapps.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.common.enums.Weekday;

public class MenuDto implements Serializable {
    private static final long serialVersionUID = 3996770534436483719L;
    private static final String TAG = MenuDto.class.getSimpleName();

    private int _id;
    private String _title;
    private String _description;
    private Weekday _weekday;
    private SerializableDate _date;

    public MenuDto(
            int id,
            @NonNull String title,
            @NonNull String description,
            @NonNull Weekday weekday,
            @NonNull SerializableDate date) {
        _id = id;
        _title = title;
        _description = description;
        _weekday = weekday;
        _date = date;
    }

    public int GetId() {
        return _id;
    }

    public String GetTitle() {
        return _title;
    }

    public String GetDescription() {
        return _description;
    }

    public String GetDateString() {
        return String.format(Locale.getDefault(), "%s, %s", _weekday, _date.DDMMYYYY());
    }

    public Weekday GetWeekday() {
        return _weekday;
    }

    public SerializableDate GetDate() {
        return _date;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Title: %s );(Description: %s );(Weekday: %s );(Date: %s ))", TAG, _title, _description, _weekday, _date);
    }
}
