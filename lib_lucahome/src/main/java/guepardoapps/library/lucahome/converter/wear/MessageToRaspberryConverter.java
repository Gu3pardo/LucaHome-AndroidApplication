package guepardoapps.library.lucahome.converter.wear;

import java.util.Calendar;

import guepardoapps.library.lucahome.common.dto.TemperatureDto;
import guepardoapps.library.lucahome.common.enums.TemperatureType;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.openweather.common.classes.SerializableTime;

public class MessageToRaspberryConverter {

    private static final String TAG = MessageToRaspberryConverter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String RASPBERRY_TEMPERATURE = "RaspberryTemperature:";

    public MessageToRaspberryConverter() {
        _logger = new LucaHomeLogger(TAG);
    }

    public TemperatureDto ConvertMessageToRaspberryModel(String message) {
        if (message.startsWith(RASPBERRY_TEMPERATURE)) {
            _logger.Debug("message starts with " + RASPBERRY_TEMPERATURE + "! replacing!");
            message = message.replace(RASPBERRY_TEMPERATURE, "");
        }
        _logger.Debug("message: " + message);

        String[] entries = message.split("\\&");
        if (entries.length == 3) {
            String temperatureString = entries[0];
            temperatureString = temperatureString.replace("TEMPERATURE", "");
            temperatureString = temperatureString.replace("�C", "");
            temperatureString = temperatureString.replace(":", "");
            temperatureString = temperatureString.replace(" ", "");
            _logger.Debug("temperatureString is: " + temperatureString);

            double temperature = -1;
            try {
                temperature = Double.parseDouble(temperatureString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            String area = entries[1];
            area = area.replace("AREA:", "");
            _logger.Debug("area is: " + area);

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            SerializableTime time = new SerializableTime(hour, minute, second, 0);

            TemperatureDto raspberryTemperature = new TemperatureDto(
                    temperature,
                    area,
                    time,
                    "",
                    TemperatureType.NULL,
                    "");
            _logger.Debug("Received raspberryTemperature: " + raspberryTemperature.toString());
            return raspberryTemperature;
        }

        _logger.Warn("Wrong size of entries: " + String.valueOf(entries.length));
        return null;
    }
}
