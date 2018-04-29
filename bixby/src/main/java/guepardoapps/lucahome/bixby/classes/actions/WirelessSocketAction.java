package guepardoapps.lucahome.bixby.classes.actions;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings("WeakerAccess")
public class WirelessSocketAction implements IBixbyAction {
    private static final String Tag = WirelessSocketAction.class.getSimpleName();

    private StateType _stateType;
    private String _wirelessSocketName;

    public WirelessSocketAction(@NonNull StateType stateType, @NonNull String wirelessSocketName) {
        _stateType = stateType;
        _wirelessSocketName = wirelessSocketName;
    }

    public WirelessSocketAction() {
        this(StateType.Null, "");
    }

    public WirelessSocketAction(@NonNull String databaseString) {
        String[] data = databaseString.split(":");
        if (data.length == 2) {
            try {
                _stateType = StateType.values()[Integer.parseInt(data[0])];
                _wirelessSocketName = data[1];

            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.toString());
                _stateType = StateType.Null;
                _wirelessSocketName = "";
            }
        } else {
            Logger.getInstance().Error(Tag, String.format(Locale.getDefault(), "Invalid data size %d", data.length));
            _stateType = StateType.Null;
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
        return String.format(Locale.getDefault(), "%s \nfor %s -> %s", Tag, _wirelessSocketName, _stateType);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"StateType\":\"%s\",\"WirelessSocketName\":\"%s\"}",
                Tag, _stateType, _wirelessSocketName);
    }
}
