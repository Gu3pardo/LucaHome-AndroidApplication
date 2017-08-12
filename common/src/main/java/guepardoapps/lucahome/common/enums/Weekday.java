package guepardoapps.lucahome.common.enums;

import android.support.annotation.NonNull;

import java.io.Serializable;

public enum Weekday implements Serializable {

    NULL("NULL", -1, "", ""),
    SU("SU", 0, "Sunday", "Sonntag"),
    MO("MO", 1, "Monday", "Montag"),
    TU("TU", 2, "Tuesday", "Dienstag"),
    WE("WE", 3, "Wednesday", "Mittwoch"),
    TH("TH", 4, "Thursday", "Donnerstag"),
    FR("FR", 5, "Friday", "Freitag"),
    SA("SA", 6, "Saturday", "Samstag");

    private String _shortStringDay;
    private int _intDay;
    private String _englishStringDay;
    private String _germanStringDay;

    Weekday(
            @NonNull String shortStringDay,
            int intDay,
            @NonNull String englishStringDay,
            @NonNull String germanStringDay) {
        _shortStringDay = shortStringDay;
        _intDay = intDay;
        _englishStringDay = englishStringDay;
        _germanStringDay = germanStringDay;
    }

    @Override
    public String toString() {
        return _shortStringDay;
    }

    public int GetInt() {
        return _intDay;
    }

    public String GetEnglishDay() {
        return _englishStringDay;
    }

    public String GetGermanDay() {
        return _germanStringDay;
    }

    public static Weekday GetById(int day) {
        for (Weekday e : values()) {
            if (e._intDay == day) {
                return e;
            }
        }
        return NULL;
    }

    public static Weekday GetByEnglishString(String day) {
        for (Weekday e : values()) {
            if (e._englishStringDay.contains(day)) {
                return e;
            }
        }
        return NULL;
    }

    public static Weekday GetByGermanString(String day) {
        for (Weekday e : values()) {
            if (e._germanStringDay.contains(day)) {
                return e;
            }
        }
        return NULL;
    }
}
