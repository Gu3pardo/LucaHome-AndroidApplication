package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.sql.Date;
import java.util.Locale;

import guepardoapps.library.toolset.common.classes.SerializableTime;

@SuppressWarnings("deprecation")
public class ChangeDto implements Serializable {

    private static final long serialVersionUID = 8796770534384442492L;

    private int _id;

    private String _type;
    private Date _date;
    private SerializableTime _time;
    private String _user;

    public ChangeDto(
            int id,
            @NonNull String type,
            @NonNull Date date,
            @NonNull SerializableTime time,
            @NonNull String user) {
        _id = id;
        _type = type;
        _date = date;
        _date.setYear(_date.getYear() - 1900);
        _time = time;
        _user = user;
    }

    public int GetId() {
        return _id;
    }

    public String GetType() {
        return _type;
    }

    public Date GetDate() {
        return _date;
    }

    public String GetDateString() {
        return String.format(
                Locale.getDefault(),
                "%d|%d|%d",
                _date.getDay(), _date.getMonth(), _date.getYear());
    }

    public SerializableTime GetTime() {
        return _time;
    }

    public String GetUser() {
        return _user;
    }

    public String toString() {
        return "{Change: {Id: " + String.valueOf(_id)
                + "};{Type: " + _type
                + "};{Date: " + _date.toString()
                + "};{Time: " + _time.toString()
                + "};{User: " + _user + "}}";
    }
}
