package guepardoapps.library.lucahome.common.dto;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.constants.ServerActions;

import java.io.Serializable;

public class MenuDto implements Serializable {

    private static final String TAG = MenuDto.class.getSimpleName();

    private static final long serialVersionUID = 9010866792096047325L;

    private String _weekday;
    private int _day;
    private int _month;
    private int _year;
    private String _title;
    private String _description;

    private boolean _needsUpdate = false;

    public MenuDto(@NonNull String weekday, int day, int month, int year,
                   @NonNull String title, @NonNull String description) {
        _weekday = weekday;

        _day = day;
        _month = month;
        _year = year;

        _title = title;
        _description = description;

        if (_day == -1 || _month == -1 || _year == -1) {
            _title = "Nothing defined!";
            _description = "-";
        }

        if (_description.length() == 0) {
            _description = "-";
        }
    }

    public String GetWeekday() {
        return _weekday;
    }

    public int GetDay() {
        return _day;
    }

    public void SetDay(int day) {
        _day = day;
    }

    public int GetMonth() {
        return _month;
    }

    public void SetMonth(int month) {
        _month = month;
    }

    public int GetYear() {
        return _year;
    }

    public void SetYear(int year) {
        _year = year;
    }

    @SuppressLint("DefaultLocale")
    public String GetDate() {
        if (_day == -1 || _month == -1 || _year == -1) {
            return "";
        }
        return String.format("%2d:%2d:%4d", _day, _month, _year).replace(" ", "0");
    }

    public String GetTitle() {
        return _title;
    }

    public void SetTitle(@NonNull String title) {
        _title = title;
    }

    public String GetDescription() {
        return _description;
    }

    public void SetDescription(@NonNull String description) {
        if (description.length() < 1) {
            description = "-";
        }

        _description = description;
    }

    public String GetCommandUpdate() {
        if (_description == null) {
            _description = "-";
        }

        if (_description.length() < 1) {
            _description = "-";
        }

        return ServerActions.UPDATE_MENU + _weekday + "&day=" + String.valueOf(_day) + "&month="
                + String.valueOf(_month) + "&year=" + String.valueOf(_year) + "&title=" + _title + "&description="
                + _description;
    }

    public String GetCommandClear() {
        return ServerActions.CLEAR_MENU + _weekday;
    }

    public boolean NeedsUpdate() {
        return _needsUpdate;
    }

    public void SetNeedsUpdate(boolean needsUpdate) {
        _needsUpdate = needsUpdate;
    }

    @Override
    public String toString() {
        return "{" + TAG + ":{Weekday:" + _weekday + "};{Day:" + String.valueOf(_day) + "};{Month:"
                + String.valueOf(_month) + "};{Year:" + String.valueOf(_year) + "};{Title:" + _title + "};{Description:"
                + _description + "};}";
    }
}
