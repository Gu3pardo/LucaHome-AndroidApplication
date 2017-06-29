package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;

import guepardoapps.library.lucahome.common.enums.TemperatureType;
import guepardoapps.library.openweather.common.classes.SerializableTime;

public class TemperatureDto implements Serializable {

    private static final long serialVersionUID = -8509818903730279328L;

    private static final String TAG = TemperatureDto.class.getSimpleName();

    private double _temperature;
    private String _area;
    private SerializableTime _lastUpdate;
    private String _sensorPath;
    private TemperatureType _temperatureType;
    private String _graphPath;

    public TemperatureDto(
            double temperature,
            @NonNull String area,
            SerializableTime lastUpdate,
            @NonNull String sensorPath,
            @NonNull TemperatureType temperatureType,
            @NonNull String graphPath) {
        _temperature = temperature;
        _area = area;

        if (lastUpdate != null) {
            _lastUpdate = lastUpdate;
        } else {
            Calendar now = Calendar.getInstance();
            _lastUpdate = new SerializableTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), 0, 0);
        }

        _sensorPath = sensorPath;
        _temperatureType = temperatureType;
        _graphPath = graphPath;
    }

    public double GetTemperatureValue() {
        return _temperature;
    }

    public String GetTemperatureString() {
        return String.valueOf(_temperature) + "°C";
    }

    public String GetArea() {
        return _area;
    }

    public SerializableTime GetLastUpdate() {
        return _lastUpdate;
    }

    public String GetSensorPath() {
        return _sensorPath;
    }

    public TemperatureType GetTemperatureType() {
        return _temperatureType;
    }

    public String GetGraphPath() {
        return _graphPath;
    }

    public String GetWatchFaceText() {
        return _area + ": " + GetTemperatureString() + " " + _lastUpdate.HHMM();
    }

    public String toString() {
        return "{"+ TAG
                + ": {Value: " + GetTemperatureString()
                + "};{Area: " + _area
                + "};{LastUpdate: " + _lastUpdate.toString()
                + "};{SensorPath: " + _sensorPath
                + "};{TemperatureType: " + _temperatureType.toString()
                + "};{GraphPath: " + _graphPath + "}}";
    }
}