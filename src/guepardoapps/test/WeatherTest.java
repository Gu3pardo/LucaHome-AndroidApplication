package guepardoapps.test;

import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.openweather.common.model.WeatherModel;

import guepardoapps.toolset.common.classes.SerializableTime;

public class WeatherTest {

	private static final String TAG = WeatherTest.class.getSimpleName();
	private LucaHomeLogger _logger;

	public WeatherTest() {
		_logger = new LucaHomeLogger(TAG);
	}

	public boolean Perform() {
		_logger.Debug("Perform");

		// String jsonString =
		// "{\"coord\":{\"lon\":11.58,\"lat\":48.14},\"weather\":[{\"id\":801,\"main\":\"Clouds\",\"description\":\"few
		// clouds\",\"icon\":\"02n\"}],\"base\":\"stations\",\"main\":{\"temp\":5.41,\"pressure\":1025,\"humidity\":64,\"temp_min\":4,\"temp_max\":7},\"visibility\":10000,\"wind\":{\"speed\":3.1,\"deg\":10},\"clouds\":{\"all\":20},\"dt\":1489166400,\"sys\":{\"type\":1,\"id\":4914,\"message\":0.0149,\"country\":\"DE\",\"sunrise\":1489124126,\"sunset\":1489165967},\"id\":6940463,\"name\":\"Altstadt\",\"cod\":200}";
		String jsonString = "{\"coord\":{\"lon\":11.58,\"lat\":48.14},\"weather\":[{\"id\":801,\"main\":\"Clouds\",\"description\":\"few clouds\",\"icon\":\"02n\"}],\"base\":\"stations\",\"main\":{\"temp\":5.41,\"pressure\":1025,\"humidity\":64,\"temp_min\":4,\"temp_max\":7},\"visibility\":10000,\"wind\":{\"speed\":3.1,\"deg\":10},\"clouds\":{\"all\":20},\"dt\":1489166400,\"sys\":{\"type\":1,\"id\":4914,\"message\":0.0149,\"country\":\"DE\",\"sunrise\":1489124126,\"sunset\":1489179967},\"id\":6940463,\"name\":\"Altstadt\",\"cod\":200}";
		JSONObject object = null;

		try {
			object = new JSONObject(jsonString);
		} catch (JSONException e) {
			_logger.Error(e.toString());
		}

		if (object == null) {
			_logger.Error("object is null!");
			return false;
		}

		WeatherModel currentWeather = new WeatherModel(object, new SerializableTime());
		_logger.Debug(currentWeather.toString());
		if (currentWeather.GetSunriseTime().isAfterNow() || currentWeather.GetSunsetTime().isBeforeNow()) {
			_logger.Debug("Show night!");
		} else {
			_logger.Debug("Switch background!");
		}

		return true;
	}
}
