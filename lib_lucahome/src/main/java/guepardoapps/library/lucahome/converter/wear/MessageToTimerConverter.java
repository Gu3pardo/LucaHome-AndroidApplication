package guepardoapps.library.lucahome.converter.wear;

import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.dto.TimerDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class MessageToTimerConverter {

    private static final String TAG = MessageToTimerConverter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String TIMER = "Timer:";

    public MessageToTimerConverter() {
        _logger = new LucaHomeLogger(TAG);
    }

    public SerializableList<TimerDto> ConvertMessageToTimerList(@NonNull String message) {
        if (message.startsWith(TIMER)) {
            _logger.Debug("message starts with " + TIMER + "! replacing!");
            message = message.replace(TIMER, "");
        }
        _logger.Debug("message: " + message);

        String[] items = message.split("\\&");
        if (items.length > 0) {
            SerializableList<TimerDto> list = new SerializableList<>();
            for (String entry : items) {
                String[] data = entry.split("\\:");

                _logger.Info("Timer dataContent");
                for (String dataContent : data) {
                    _logger.Info(dataContent);
                }

                if (data.length == 5) {
                    if (data[0] != null && data[1] != null && data[2] != null && data[3] != null && data[4] != null) {
                        TimerDto item = new TimerDto(
                                -1,
                                R.drawable.timer,
                                data[0], data[1] + ":" + data[2] + ":" + data[3],
                                data[4].contains("1"));
                        list.addValue(item);
                    } else {
                        _logger.Warn("data[0] or data[1] or data[2] or data[3] or data[4] is null!");
                    }
                } else {
                    _logger.Warn("Wrong size of scheduleData: " + String.valueOf(data.length));
                }
            }
            return list;
        }

        _logger.Warn("Found no timer!");
        return new SerializableList<>();
    }
}
