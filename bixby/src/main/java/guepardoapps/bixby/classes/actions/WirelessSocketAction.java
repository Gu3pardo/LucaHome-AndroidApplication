package guepardoapps.bixby.classes.actions;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.bixby.interfaces.IBixbyAction;

import guepardoapps.lucahome.basic.utils.Logger;

@SuppressWarnings("WeakerAccess")
public class WirelessSocketAction implements IBixbyAction, Serializable {
    private static final String TAG = WirelessSocketAction.class.getSimpleName();

    public enum StateType {OFF, ON, NULL}

    private StateType _stateType;
    private String _wirelessSocketName;

    public WirelessSocketAction(@NonNull StateType stateType, @NonNull String wirelessSocketName) {
        _stateType = stateType;
        _wirelessSocketName = wirelessSocketName;
    }

    public WirelessSocketAction() {
        this(StateType.NULL, "");
    }

    public WirelessSocketAction(@NonNull String databaseString) {
        String[] data = databaseString.split(":");
        if (data.length == 2) {
            try {
                _stateType = StateType.values()[Integer.parseInt(data[0])];
                _wirelessSocketName = data[1];

            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.toString());
                _stateType = StateType.NULL;
                _wirelessSocketName = "";
            }
        } else {
            Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid data size %d", data.length));
            _stateType = StateType.NULL;
            _wirelessSocketName = data[1];
        }
    }

    public StateType GetStateType() {
        return _stateType;
    }

    public String GetWirelessSocketName() {
        return _wirelessSocketName;
    }

    @Override
    public String GetDatabaseString() {
        return String.format(Locale.getDefault(), "%d:%s", _stateType.ordinal(), _wirelessSocketName);
    }

    @Override
    public String GetInformationString() {
        return String.format(Locale.getDefault(), "%s for %s -> %s", TAG, _wirelessSocketName, _stateType);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s:{StateType:%s,WirelessSocketName:%s}}", TAG, _stateType, _wirelessSocketName);
    }
}
