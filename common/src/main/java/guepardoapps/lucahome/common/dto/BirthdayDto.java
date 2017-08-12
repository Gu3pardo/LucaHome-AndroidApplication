package guepardoapps.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;

public class BirthdayDto implements Serializable {
    private static final long serialVersionUID = 8796770534436481492L;
    private static final String TAG = BirthdayDto.class.getSimpleName();

    public enum Action {Add, Update}

    private int _id;
    private String _name;
    private SerializableDate _date;
    private Action _action;

    public BirthdayDto(
            int id,
            String name,
            SerializableDate date,
            @NonNull Action action) {
        _id = id;
        _name = name;
        _date = date;
        _action = action;
    }

    public int GetId() {
        return _id;
    }

    public String GetName() {
        return _name;
    }

    public SerializableDate GetDate() {
        return _date;
    }

    public Action GetAction() {
        return _action;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Name: %s );(Birthday: %s );(Action: %s ))", TAG, _name, _date, _action);
    }
}
