package guepardoapps.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public class WirelessSwitchDto implements Serializable {
    private static final long serialVersionUID = 5573384712706411180L;
    private static final String TAG = WirelessSwitchDto.class.getSimpleName();

    public enum Action {Add, Update}

    private int _id;

    private String _name;
    private String _area;
    private int _remoteId;
    private char _keyCode;
    private Action _action;

    public WirelessSwitchDto(
            int id,
            @NonNull String name,
            @NonNull String area,
            int remoteId,
            char keyCode,
            Action action) {
        _id = id;

        _name = name;
        _area = area;
        _remoteId = remoteId;
        _keyCode = keyCode;

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

    public int GetRemoteId() {
        return _remoteId;
    }

    public char GetKeyCode() {
        return _keyCode;
    }

    public Action GetAction() {
        return _action;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Name: %s );(Area: %s );(RemoteId: %d );(KeyCode: %s );(Action: %s ))", TAG, _name, _area, _remoteId, _keyCode, _action);
    }
}
