package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableTime;

public class Change implements Serializable {
    private static final long serialVersionUID = 8796770534384442492L;
    private static final String TAG = Change.class.getSimpleName();

    private int _id;

    private String _type;
    private SerializableDate _date;
    private SerializableTime _time;
    private String _user;

    public Change(
            int id,
            @NonNull String type,
            @NonNull SerializableDate date,
            @NonNull SerializableTime time,
            @NonNull String user) {
        _id = id;
        _type = type;
        _date = date;
        _time = time;
        _user = user;
    }

    public int GetId() {
        return _id;
    }

    public String GetType() {
        return _type;
    }

    public SerializableDate GetDate() {
        return _date;
    }

    public SerializableTime GetTime() {
        return _time;
    }

    public String GetUser() {
        return _user;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s: {Id: %d};{Type: %s};{Date: %s};{Time: %s};{User: %s}}", TAG, _id, _type, _date, _time, _user);
    }
}
