package guepardoapps.lucahome.dto;

import java.io.Serializable;
import java.sql.Time;

public class WeatherModelDto implements Serializable {

	private static final long serialVersionUID = 8871312329734975799L;

	@SuppressWarnings("unused")
	private static final String TAG = WeatherModelDto.class.getName();

	private String _description;
	private double _temperature;
	private Time _lastUpdate;

	public WeatherModelDto(String description, double temperature, Time lastUpdate) {
		_description = description;
		_temperature = temperature;
		_lastUpdate = lastUpdate;
	}

	public String GetDescription() {
		return _description;
	}

	public double GetTemperature() {
		return _temperature;
	}

	public Time GetLastUpdate() {
		return _lastUpdate;
	}

	public String GetWatchFaceText() {
		return _description + " " + String.valueOf(_temperature) + " " + _lastUpdate.toString();
	}

	@Override
	public String toString() {
		return "[WeatherModel: Description: " + _description + "; Temperature: " + String.valueOf(_description)
				+ "; LastUpdate: " + _lastUpdate.toString() + "]";
	}
}