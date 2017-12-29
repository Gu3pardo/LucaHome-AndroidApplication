package guepardoapps.mediamirror.common.models;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class IpAddressModel implements Serializable {
    private static final long serialVersionUID = 2738438227273452818L;

    private static final String TAG = IpAddressModel.class.getSimpleName();

    private boolean _isVisible;
    private String _ipAddress;

    public IpAddressModel(
            boolean isVisible,
            @NonNull String ipAddress) {
        _isVisible = isVisible;
        _ipAddress = ipAddress;
    }

    public boolean GetIsVisible() {
        return _isVisible;
    }

    public String GetIpAddress() {
        return _ipAddress;
    }

    @Override
    public String toString() {
        return TAG
                + ":{IsVisible:" + String.valueOf(_isVisible)
                + ";IpAddress:" + _ipAddress + "}";
    }
}
