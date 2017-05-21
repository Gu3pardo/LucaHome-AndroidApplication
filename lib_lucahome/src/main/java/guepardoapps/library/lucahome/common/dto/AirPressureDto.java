package guepardoapps.library.lucahome.common.dto;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.library.toolset.common.classes.SerializableTime;

public class AirPressureDto implements Serializable {

    private static final long serialVersionUID = -3771050814199928178L;

    private double _airPressure;
    private String _area;
    private SerializableTime _lastUpdate;

    public AirPressureDto(double airPressure, String area, SerializableTime lastUpdate) {
        _airPressure = airPressure;
        _area = area;
        _lastUpdate = lastUpdate;
    }

    public double GetAirPressureValue() {
        return _airPressure;
    }

    public void SetAirPressure(double airPressure) {
        _airPressure = airPressure;
    }

    public String GetAirPressureString() {
        return String.format(Locale.GERMAN, "%.2fmBar", _airPressure);
    }

    public String GetArea() {
        return _area;
    }

    public SerializableTime GetLastUpdate() {
        return _lastUpdate;
    }

    public void SetLastUpdate(SerializableTime lastUpdate) {
        _lastUpdate = lastUpdate;
    }

    public String GetWatchFaceText() {
        return _area + ": " + GetAirPressureString() + " " + _lastUpdate.toString();
    }

    public String toString() {
        return "{AirPressure: {Value: " + GetAirPressureString() + "};{Area: " + _area + "};{LastUpdate: "
                + _lastUpdate.toString() + "}}";
    }
}
