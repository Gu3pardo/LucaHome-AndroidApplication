package guepardoapps.lucahome.dto.sensor;

import java.io.Serializable;
import java.sql.Time;

public class HumidityDto implements Serializable {

	private static final long serialVersionUID = -6934156448279531637L;

	private double _humidity;
	private String _area;
	private Time _lastUpdate;

	public HumidityDto(double humidity, String area, Time lastUpdate) {
		_humidity = humidity;
		_area = area;
		_lastUpdate = lastUpdate;
	}

	public double GetHumidityValue() {
		return _humidity;
	}

	public void SetHumidity(double humidity) {
		_humidity = humidity;
	}

	public String GetHumidityString() {
		return String.valueOf(_humidity).substring(0, 6) + "%";
	}

	public String GetArea() {
		return _area;
	}

	public Time GetLastUpdate() {
		return _lastUpdate;
	}

	public void SetLastUpdate(Time lastUpdate) {
		_lastUpdate = lastUpdate;
	}

	public String GetWatchFaceText() {
		return _area + ": " + GetHumidityString() + " " + _lastUpdate.toString();
	}

	public String toString() {
		return "{Humidity: {Value: " + GetHumidityString() + "};{Area: " + _area + "};{LastUpdate: "
				+ _lastUpdate.toString() + "}}";
	}
}
