package guepardoapps.lucahome.bixby.classes.actions;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings("WeakerAccess")
public class NetworkAction implements IBixbyAction {
    private static final String Tag = NetworkAction.class.getSimpleName();

    private NetworkType _networkType;
    private StateType _stateType;

    public NetworkAction(@NonNull NetworkType networkType, @NonNull StateType stateType) {
        _networkType = networkType;
        _stateType = stateType;
    }

    public NetworkAction() {
        this(NetworkType.Null, StateType.Null);
    }

    public NetworkAction(@NonNull String databaseString) {
        String[] data = databaseString.split(":");
        if (data.length == 2) {
            try {
                _networkType = NetworkType.values()[Integer.parseInt(data[0])];
                _stateType = StateType.values()[Integer.parseInt(data[1])];

            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.toString());
                _networkType = NetworkType.Null;
                _stateType = StateType.Null;
            }
        } else {
            Logger.getInstance().Error(Tag, String.format(Locale.getDefault(), "Invalid data size %d", data.length));
            _networkType = NetworkType.Null;
            _stateType = StateType.Null;
        }
    }

    public NetworkType GetNetworkType() {
        return _networkType;
    }

    public StateType GetStateType() {
        return _stateType;
    }

    @Override
    public String GetDatabaseString() {
        return String.format(Locale.getDefault(), "%d:%d", _networkType.ordinal(), _stateType.ordinal());
    }

    @Override
    public String GetInformationString() {
        return String.format(Locale.getDefault(), "%s \nfor %s -> %s", Tag, _networkType, _stateType);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"NetworkType\":\"%s\",\"StateType\":\"%s\"}",
                Tag, _networkType, _stateType);
    }
}
