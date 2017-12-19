package guepardoapps.mediamirror.common.models;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class RaspberryModel implements Serializable {

    private static final long serialVersionUID = -7204415758428216530L;

    private static final String TAG = RaspberryModel.class.getSimpleName();

    private String _raspberryName;
    private String _raspberryTemperature;
    private String _raspberryTemperatureGraphUrl;

    public RaspberryModel(
            @NonNull String raspberryName,
            @NonNull String raspberryTemperature,
            @NonNull String raspberryTemperatureGraphUrl) {
        _raspberryName = raspberryName;
        _raspberryTemperature = raspberryTemperature;
        _raspberryTemperatureGraphUrl = raspberryTemperatureGraphUrl;
    }

    public String GetRaspberryName() {
        return _raspberryName;
    }

    public String GetRaspberryTemperature() {
        return _raspberryTemperature;
    }

    public String GetRaspberryTemperatureGraphUrl() {
        return _raspberryTemperatureGraphUrl;
    }

    @Override
    public String toString() {
        return TAG
                + ":{RaspberryName:" + _raspberryName
                + ";RaspberryTemperature:" + _raspberryTemperature
                + ";RaspberryTemperatureGraphUrl:" + _raspberryTemperatureGraphUrl + "}";
    }
}
