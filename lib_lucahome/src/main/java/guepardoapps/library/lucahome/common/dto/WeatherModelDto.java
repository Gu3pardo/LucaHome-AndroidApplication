package guepardoapps.library.lucahome.common.dto;

import java.io.Serializable;

import guepardoapps.library.openweather.common.enums.WeatherCondition;

import guepardoapps.library.toolset.common.classes.SerializableTime;

public class WeatherModelDto implements Serializable {

	private static final long serialVersionUID = 1367674017737422882L;

	private static final String TAG = WeatherModelDto.class.getSimpleName();

	private double _temperature;

	private WeatherCondition _condition;

	private SerializableTime _sunriseTime;
	private SerializableTime _sunsetTime;

	private SerializableTime _lastUpdate;

	public WeatherModelDto(double temperature, WeatherCondition condition, SerializableTime sunriseTime,
			SerializableTime sunsetTime, SerializableTime lastUpdate) {
		_temperature = temperature;

		_condition = condition;

		if (sunriseTime != null) {
			_sunriseTime = sunriseTime;
		} else {
			_sunriseTime = new SerializableTime(6, 0, 0, 0);
		}

		if (sunsetTime != null) {
			_sunsetTime = sunsetTime;
		} else {
			_sunriseTime = new SerializableTime(20, 0, 0, 0);
		}

		if (lastUpdate != null) {
			_lastUpdate = lastUpdate;
		} else {
			_lastUpdate = new SerializableTime();
		}
	}

	public double GetTemperature() {
		return _temperature;
	}

	public WeatherCondition GetWeatherCondition() {
		return _condition;
	}

	public SerializableTime GetSunriseTime() {
		return _sunriseTime;
	}

	public SerializableTime GetSunsetTime() {
		return _sunsetTime;
	}

	public SerializableTime GetLastUpdate() {
		return _lastUpdate;
	}

	public String GetWatchFaceText() {
		return _condition.toString() + " " + String.valueOf(_temperature) + "°C " + _lastUpdate.HHMM();
	}

	@Override
	public String toString() {
		return "[" + TAG + ": Condition: " + _condition.toString() + "; Temperature: " + String.valueOf(_temperature)
				+ "°C; SunriseTime: " + _sunriseTime.toString() + "; SunsetTime: " + _sunsetTime.toString()
				+ "; LastUpdate: " + _lastUpdate.toString() + "]";
	}
}