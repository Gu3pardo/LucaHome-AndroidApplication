package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.enums.Weekday;

public class LucaMenu implements Serializable {
    private static final long serialVersionUID = 3749147384275047381L;
    private static final String TAG = LucaMenu.class.getSimpleName();

    private int _id;
    private String _title;
    private String _description;
    private Weekday _weekday;
    private SerializableDate _date;

    public LucaMenu(
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

    public void SetTitle(@NonNull String title) {
        _title = title;
    }

    public String GetDescription() {
        return _description;
    }

    public void SetDescription(@NonNull String description) {
        _description = description;
    }

    public Weekday GetWeekday() {
        return _weekday;
    }

    public void SetWeekday(@NonNull Weekday weekday) {
        _weekday = weekday;
    }

    public SerializableDate GetDate() {
        return _date;
    }

    public void SetDate(@NonNull SerializableDate date) {
        _date = date;
    }

    public String GetDateString() {
        return String.format(Locale.getDefault(), "%s, %s", _weekday, _date.DDMMYYYY());
    }

    public String GetSingleLineString() {
        return String.format(Locale.getDefault(), "%s: %s, %s", GetDateString(), _title, _description);
    }

    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%s&day=%d&month=%d&year=%d&title=%s&description=%s", LucaServerAction.UPDATE_MENU.toString(), _weekday.GetEnglishDay(), _date.DayOfMonth(), _date.Month(), _date.Year(), _title, _description);
    }

    public String CommandClear() {
        return String.format(Locale.getDefault(), "%s%s", LucaServerAction.UPDATE_MENU.toString(), _weekday.GetEnglishDay());
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Id: %d );(Title: %s );(Description: %s );(Weekday: %s );(Date: %s ))", TAG, _id, _title, _description, _weekday, _date);
    }
}
