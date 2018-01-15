package guepardoapps.bixby.classes.actions;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.bixby.interfaces.IBixbyAction;

import guepardoapps.lucahome.basic.utils.Logger;

@SuppressWarnings("WeakerAccess")
public class NetworkAction implements IBixbyAction, Serializable {
    private static final String TAG = NetworkAction.class.getSimpleName();

    public enum NetworkType {NULL, MOBILE, WIFI}

    public enum StateType {OFF, ON, NULL}

    private NetworkType _networkType;
    private StateType _stateType;

    public NetworkAction(@NonNull NetworkType networkType, @NonNull StateType stateType) {
        _networkType = networkType;
        _stateType = stateType;
    }

    public NetworkAction() {
        this(NetworkType.NULL, StateType.NULL);
    }

    public NetworkAction(@NonNull String databaseString) {
        String[] data = databaseString.split(":");
        if (data.length == 2) {
            try {
                _networkType = NetworkType.values()[Integer.parseInt(data[0])];
                _stateType = StateType.values()[Integer.parseInt(data[1])];

            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.toString());
                _networkType = NetworkType.NULL;
                _stateType = StateType.NULL;
            }
        } else {
            Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid data size %d", data.length));
            _networkType = NetworkType.NULL;
            _stateType = StateType.NULL;
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
        return String.format(Locale.getDefault(), "%s \nfor %s -> %s", TAG, _networkType, _stateType);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s:{NetworkType:%s,StateType:%s}}", TAG, _networkType, _stateType);
    }
}
