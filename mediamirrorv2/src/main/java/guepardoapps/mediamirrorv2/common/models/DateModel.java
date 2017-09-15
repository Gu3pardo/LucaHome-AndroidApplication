package guepardoapps.mediamirrorv2.common.models;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class DateModel implements Serializable {

    private static final long serialVersionUID = -3097274504214063795L;

    private static final String TAG = DateModel.class.getSimpleName();

    private String _weekday;
    private String _date;
    private String _time;

    public DateModel(
            @NonNull String weekday,
            @NonNull String date,
            @NonNull String time) {
        _weekday = weekday;
        _date = date;
        _time = time;
    }

    public String GetWeekday() {
        return _weekday;
    }

    public String GetDate() {
        return _date;
    }

    public String GetTime() {
        return _time;
    }

    @Override
    public String toString() {
        return TAG
                + ":{Weekday:" + _weekday
                + ";Date:" + _date
                + ";Time:" + _time + "}";
    }
}
