package guepardoapps.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public class WirelessSocketDto implements Serializable {
    private static final long serialVersionUID = 5573384712706411180L;
    private static final String TAG = WirelessSocketDto.class.getSimpleName();

    public enum Action {Add, Update}

    private int _id;

    private String _name;
    private String _area;
    private String _code;
    private boolean _isActivated;
    private Action _action;

    public WirelessSocketDto(
            int id,
            @NonNull String name,
            @NonNull String area,
            @NonNull String code,
            boolean isActivated,
            Action action) {
        _id = id;

        _name = name;
        _area = area;
        _code = code;
        _isActivated = isActivated;

        _action = action;
    }

    public int GetId() {
        return _id;
    }

    public String GetName() {
        return _name;
    }

    public String GetArea() {
        return _area;
    }

    public String GetCode() {
        return _code;
    }

    public boolean IsActivated() {
        return _isActivated;
    }

    public Action GetAction() {
        return _action;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Name: %s );(Area: %s );(Code: %s );(IsActivated: %s );(Action: %s ))", TAG, _name, _area, _code, (_isActivated ? "1" : "0"), _action);
    }
}
