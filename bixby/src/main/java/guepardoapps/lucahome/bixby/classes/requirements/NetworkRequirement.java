package guepardoapps.lucahome.bixby.classes.requirements;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class NetworkRequirement implements IBixbyRequirement, Serializable {
    private static final String Tag = NetworkRequirement.class.getSimpleName();

    private NetworkType _networkType;
    private StateType _stateType;
    private String _wifiSsid;

    public NetworkRequirement(@NonNull NetworkType networkType, @NonNull StateType stateType, @NonNull String wifiSsid) {
        _networkType = networkType;
        _stateType = stateType;
        _wifiSsid = wifiSsid;
    }

    public NetworkRequirement(@NonNull StateType stateType, @NonNull String wifiSsid) {
        this(NetworkType.Wifi, stateType, wifiSsid);
    }

    public NetworkRequirement() {
        this(NetworkType.Null, StateType.Null, "");
    }

    public NetworkRequirement(@NonNull String databaseString) {
        String[] data = databaseString.split(":");
        if (data.length == 3) {
            try {
                _networkType = NetworkType.values()[Integer.parseInt(data[0])];
                _stateType = StateType.values()[Integer.parseInt(data[1])];
                _wifiSsid = data[2];

            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.toString());
                _networkType = NetworkType.Null;
                _stateType = StateType.Null;
                _wifiSsid = "";
            }
        } else {
            Logger.getInstance().Error(Tag, String.format(Locale.getDefault(), "Invalid data size %d", data.length));
            _networkType = NetworkType.Null;
            _stateType = StateType.Null;
            _wifiSsid = "";
        }
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
    public String GetDatabaseString() {
        return String.format(Locale.getDefault(), "%d:%d:%s", _networkType.ordinal(), _stateType.ordinal(), _wifiSsid);
    }

    @Override
    public String GetInformationString() {
        return String.format(Locale.getDefault(), "%s: %s should be %s and %s", Tag, _networkType, _wifiSsid, _stateType);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"NetworkType\":\"%s\",\"StateType\":\"%s\",\"WifiSsid\":\"%s\"}",
                Tag, _networkType, _stateType, _wifiSsid);
    }
}
