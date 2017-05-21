package guepardoapps.library.lucahome.common.dto;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.library.toolset.common.classes.SerializableTime;

public class HumidityDto implements Serializable {

	private static final long serialVersionUID = -6934156448279531637L;

	private double _humidity;
	private String _area;
	private SerializableTime _lastUpdate;

	public HumidityDto(double humidity, String area, SerializableTime lastUpdate) {
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
		return String.format(Locale.GERMAN, "%.2f%%", _humidity);
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
		return _area + ": " + GetHumidityString() + " " + _lastUpdate.toString();
	}

	public String toString() {
		return "{Humidity: {Value: " + GetHumidityString() + "};{Area: " + _area + "};{LastUpdate: "
				+ _lastUpdate.toString() + "}}";
	}
}
