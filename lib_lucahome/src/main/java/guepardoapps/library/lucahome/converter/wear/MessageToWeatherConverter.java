package guepardoapps.library.lucahome.converter.wear;

import java.sql.Time;

import guepardoapps.library.lucahome.common.dto.WeatherModelDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.openweather.common.enums.WeatherCondition;

import guepardoapps.library.toolset.common.classes.SerializableTime;

public class MessageToWeatherConverter {

    private static final String TAG = MessageToWeatherConverter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String CURRENT_WEATHER = "CurrentWeather:";

    public MessageToWeatherConverter() {
        _logger = new LucaHomeLogger(TAG);
    }

    @SuppressWarnings("deprecation")
    public WeatherModelDto ConvertMessageToWeatherModel(String message) {
        if (message.startsWith(CURRENT_WEATHER)) {
            _logger.Debug("message starts with " + CURRENT_WEATHER + "! replacing!");
            message = message.replace(CURRENT_WEATHER, "");
        }
        _logger.Debug("message: " + message);

        String[] entries = message.split("\\&");
        if (entries.length == 5) {
            String descriptionString = entries[0];
            descriptionString = descriptionString.replace("DESCRIPTION:", "");
            _logger.Debug("original description is: " + descriptionString);
            WeatherCondition condition = WeatherCondition.GetByString(descriptionString);
            _logger.Debug("converted condition is: " + condition.toString());

            String temperatureString = entries[1];
            temperatureString = temperatureString.replace("TEMPERATURE", "");
            temperatureString = temperatureString.replace("0xC2C", "");
            temperatureString = temperatureString.replace(":", "");
            temperatureString = temperatureString.replace(" ", "");
            _logger.Debug("temperatureString is: " + temperatureString);

            double temperature = -1;
            try {
                temperature = Double.parseDouble(temperatureString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            String lastUpdateString = entries[2];
            lastUpdateString = lastUpdateString.replace("LASTUPDATE:", "");
            _logger.Debug("lastUpdate is: " + lastUpdateString);
            int lastUpdateInteger = -1;
            try {
                lastUpdateInteger = Integer.parseInt(lastUpdateString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }
            Time lastUpdateOld = new Time(lastUpdateInteger);
            SerializableTime lastUpdate = new SerializableTime(lastUpdateOld.getHours() - 1, lastUpdateOld.getMinutes(),
                    lastUpdateOld.getSeconds(), 0);
            _logger.Debug("lastUpdate is " + String.valueOf(lastUpdate.toMilliSecond()));

            String sunriseString = entries[3];
            sunriseString = sunriseString.replace("SUNRISE:", "");
            _logger.Debug("sunriseString is: " + sunriseString);
            int sunriseInteger = -1;
            try {
                sunriseInteger = Integer.parseInt(sunriseString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }
            Time sunriseTimeOld = new Time(sunriseInteger);
            SerializableTime sunrise = new SerializableTime(sunriseTimeOld.getHours() - 1, sunriseTimeOld.getMinutes(),
                    sunriseTimeOld.getSeconds(), 0);
            _logger.Debug("sunrise is " + String.valueOf(sunrise.toMilliSecond()));

            String sunsetString = entries[4];
            sunsetString = sunsetString.replace("SUNSET:", "");
            _logger.Debug("sunsetString is: " + sunsetString);
            int sunsetInteger = -1;
            try {
                sunsetInteger = Integer.parseInt(sunsetString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }
            Time sunsetTimeOld = new Time(sunsetInteger);
            SerializableTime sunset = new SerializableTime(sunsetTimeOld.getHours() - 1, sunsetTimeOld.getMinutes(),
                    sunsetTimeOld.getSeconds(), 0);
            _logger.Debug("sunset is " + String.valueOf(sunset.toMilliSecond()));

            WeatherModelDto currentWeather = new WeatherModelDto(temperature, condition, sunrise, sunset, lastUpdate);
            _logger.Debug("Received current weather: " + currentWeather.toString());
            return currentWeather;
        }

        _logger.Warn("Wrong size of entries: " + String.valueOf(entries.length));
        return null;
    }
}
