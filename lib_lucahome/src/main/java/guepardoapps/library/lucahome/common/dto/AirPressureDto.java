package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.library.toolset.common.classes.SerializableTime;

public class AirPressureDto implements Serializable {

    private static final long serialVersionUID = -3771050814199928178L;

    private double _airPressure;
    private String _area;
    private SerializableTime _lastUpdate;

    public AirPressureDto(
            double airPressure,
            @NonNull String area,
            @NonNull SerializableTime lastUpdate) {
        _airPressure = airPressure;
        _area = area;
        _lastUpdate = lastUpdate;
    }

    public String GetAirPressureString() {
        return String.format(Locale.getDefault(), "%.2fmBar", _airPressure);
    }

    public String GetArea() {
        return _area;
    }

    public SerializableTime GetLastUpdate() {
        return _lastUpdate;
    }

    public String toString() {
        return "{AirPressure: {Value: " + GetAirPressureString()
                + "};{Area: " + _area
                + "};{LastUpdate: " + _lastUpdate.toString() + "}}";
    }
}
