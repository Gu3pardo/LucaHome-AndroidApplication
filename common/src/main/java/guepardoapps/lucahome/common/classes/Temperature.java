package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.common.R;

@SuppressWarnings({"unused"})
public class Temperature implements Serializable {
    private static final long serialVersionUID = 8405839534384442492L;
    private static final String TAG = Temperature.class.getSimpleName();

    public enum TemperatureType {DUMMY, RASPBERRY, PUCK_JS, CITY}

    private int _id;

    private double _temperature;
    private String _area;
    private SerializableDate _date;
    private SerializableTime _time;
    private String _sensorPath;
    private TemperatureType _temperatureType;
    private String _graphPath;

    public Temperature(
            int id,
            double temperature,
            @NonNull String area,
            @NonNull SerializableDate date,
            @NonNull SerializableTime time,
            @NonNull String sensorPath,
            @NonNull TemperatureType temperatureType,
            @NonNull String graphPath) {
        _id = id;
        _temperature = temperature;
        _area = area;
        _date = date;
        _time = time;
        _sensorPath = sensorPath;
        _temperatureType = temperatureType;
        _graphPath = graphPath;
    }

    public int GetId() {
        return _id;
    }

    public double GetTemperature() {
        return _temperature;
    }

    public String GetTemperatureString() {
        return String.format(Locale.getDefault(), "%.2f °C", _temperature);
    }

    public String GetArea() {
        return _area;
    }

    public SerializableDate GetDate() {
        return _date;
    }

    public void SetDate(SerializableDate date) {
        _date = date;
    }

    public SerializableTime GetTime() {
        return _time;
    }

    public void SetTime(SerializableTime time) {
        _time = time;
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

    public int GetDrawable() {
        if (_temperature < 12) {
            return R.drawable.circle_blue;
        } else if (_temperature >= 12 && _temperature < 15) {
            return R.drawable.circle_red;
        } else if (_temperature >= 15 && _temperature < 18) {
            return R.drawable.circle_yellow;
        } else if (_temperature >= 18 && _temperature <= 25) {
            return R.drawable.circle_dark_green;
        } else if (_temperature > 25 && _temperature < 30) {
            return R.drawable.circle_yellow;
        } else if (_temperature >= 30) {
            return R.drawable.circle_red;
        } else {
            return R.drawable.drawing_temperature;
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Id: %d );(Temperature: %s );(Area: %s );(Date: %s );(Time: %s );(SensorPath: %s );(TemperatureType: %s );(GraphPath: %s ))", TAG, _id, GetTemperatureString(), _area, _date, _time, _sensorPath, _temperatureType, _graphPath);
    }
}
