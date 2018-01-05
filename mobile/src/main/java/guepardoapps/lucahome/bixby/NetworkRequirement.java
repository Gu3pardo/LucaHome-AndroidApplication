package guepardoapps.lucahome.bixby;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;

@SuppressWarnings({"WeakerAccess", "unused"})
public class NetworkRequirement {
    private static final String TAG = NetworkRequirement.class.getSimpleName();

    public enum NetworkType {NULL, MOBILE, WIFI}

    public enum StateType {OFF, ON, NULL}

    private NetworkType _networkType;
    private StateType _stateType;
    private String _wifiSsid;

    public NetworkRequirement(@NonNull NetworkType networkType, @NonNull StateType stateType, @NonNull String wifiSsid) {
        _networkType = networkType;
        _stateType = stateType;
        _wifiSsid = wifiSsid;
    }

    public NetworkRequirement(@NonNull StateType stateType, @NonNull String wifiSsid) {
        this(NetworkType.WIFI, stateType, wifiSsid);
    }

    public NetworkRequirement() {
        this(NetworkType.NULL, StateType.NULL, "");
    }

    public NetworkRequirement(@NonNull String databaseString) {
        String[] data = databaseString.split(":");
        if (data.length == 3) {
            try {
                _networkType = NetworkType.values()[Integer.parseInt(data[0])];
                _stateType = StateType.values()[Integer.parseInt(data[1])];
                _wifiSsid = data[2];

            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.toString());
                _networkType = NetworkType.NULL;
                _stateType = StateType.NULL;
                _wifiSsid = "";
            }
        } else {
            Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid data size %d", data.length));
            _networkType = NetworkType.NULL;
            _stateType = StateType.NULL;
            _wifiSsid = "";
        }
    }

    public String GetDatabaseString() {
        return String.format(Locale.getDefault(), "%d:%d:%s", _networkType.ordinal(), _stateType.ordinal(), _wifiSsid);
    }

    public NetworkType GetNetworkType() {
        return _networkType;
    }

    public StateType GetStateType() {
        return _stateType;
    }

    public String GetWifiSsid() {
        return _wifiSsid;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s:{NetworkType:%s,StateType:%s,WifiSsid:%s}}", TAG, _networkType, _stateType, _wifiSsid);
    }
}
