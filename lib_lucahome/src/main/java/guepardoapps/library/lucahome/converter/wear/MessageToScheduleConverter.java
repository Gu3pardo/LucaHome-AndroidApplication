package guepardoapps.library.lucahome.converter.wear;

import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.dto.ScheduleDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class MessageToScheduleConverter {

    private static final String TAG = MessageToScheduleConverter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String SCHEDULES = "Schedules:";

    public MessageToScheduleConverter() {
        _logger = new LucaHomeLogger(TAG);
    }

    public SerializableList<ScheduleDto> ConvertMessageToScheduleList(@NonNull String message) {
        if (message.startsWith(SCHEDULES)) {
            _logger.Debug("message starts with " + SCHEDULES + "! replacing!");
            message = message.replace(SCHEDULES, "");
        }
        _logger.Debug("message: " + message);

        String[] items = message.split("\\&");
        if (items.length > 0) {
            SerializableList<ScheduleDto> list = new SerializableList<>();
            for (String entry : items) {
                String[] data = entry.split("\\:");

                _logger.Info("Schedule dataContent");
                for (String dataContent : data) {
                    _logger.Info(dataContent);
                }

                if (data.length == 5) {
                    if (data[0] != null && data[1] != null && data[2] != null && data[3] != null && data[4] != null) {
                        ScheduleDto item = new ScheduleDto(
                                -1,
                                R.drawable.scheduler,
                                data[0],
                                data[1] + ":" + data[2] + ":" + data[3],
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

        _logger.Warn("Found no schedule!");
        return new SerializableList<>();
    }
}
