package guepardoapps.bixby.classes.requirements;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.bixby.interfaces.IBixbyRequirement;

import guepardoapps.lucahome.basic.utils.Logger;

@SuppressWarnings("WeakerAccess")
public class WirelessSocketRequirement implements IBixbyRequirement, Serializable {
    private static final String TAG = WirelessSocketRequirement.class.getSimpleName();

    public enum StateType {OFF, ON, NULL}

    private StateType _stateType;
    private String _wirelessSocketName;

    public WirelessSocketRequirement(@NonNull StateType stateType, @NonNull String wirelessSocketName) {
        _stateType = stateType;
        _wirelessSocketName = wirelessSocketName;
    }

    public WirelessSocketRequirement() {
        this(StateType.NULL, "");
    }

    public WirelessSocketRequirement(@NonNull String databaseString) {
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
        return String.format(Locale.getDefault(), "%s: %s should be %s", TAG, _wirelessSocketName, _stateType);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s:{StateType:%s,WirelessSocketName:%s}}", TAG, _stateType, _wirelessSocketName);
    }
}
