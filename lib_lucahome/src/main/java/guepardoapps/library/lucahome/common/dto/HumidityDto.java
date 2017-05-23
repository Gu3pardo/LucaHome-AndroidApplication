package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.library.toolset.common.classes.SerializableTime;

public class HumidityDto implements Serializable {

	private static final long serialVersionUID = -6934156448279531637L;

	private double _humidity;
	private String _area;
	private SerializableTime _lastUpdate;

	public HumidityDto(
			double humidity,
			@NonNull String area,
			@NonNull SerializableTime lastUpdate) {
		_humidity = humidity;
		_area = area;
		_lastUpdate = lastUpdate;
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

	public String toString() {
		return "{Humidity: {Value: " + GetHumidityString()
				+ "};{Area: " + _area
				+ "};{LastUpdate: " + _lastUpdate.toString() + "}}";
	}
}
