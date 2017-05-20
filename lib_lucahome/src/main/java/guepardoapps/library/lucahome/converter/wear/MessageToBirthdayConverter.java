package guepardoapps.library.lucahome.converter.wear;

import java.util.Calendar;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.dto.BirthdayDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class MessageToBirthdayConverter {

    private static final String TAG = MessageToBirthdayConverter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String BIRTHDAYS = "Birthdays:";

    public MessageToBirthdayConverter() {
        _logger = new LucaHomeLogger(TAG);
    }

    public SerializableList<BirthdayDto> ConvertMessageToBirthdayList(String message) {
        if (message.startsWith(BIRTHDAYS)) {
            _logger.Debug("message starts with " + BIRTHDAYS + "! replacing!");
            message = message.replace(BIRTHDAYS, "");
        }
        _logger.Debug("message: " + message);

        String[] items = message.split("\\&");
        if (items.length > 0) {
            SerializableList<BirthdayDto> list = new SerializableList<>();
            for (String entry : items) {
                String[] data = entry.split("\\:");

                _logger.Info("Birthday dataContent");
                for (String dataContent : data) {
                    _logger.Info(dataContent);
                }

                if (data.length == 4) {
                    if (data[0] != null && data[1] != null && data[2] != null && data[3] != null) {
                        Calendar birthday = Calendar.getInstance();
                        birthday.set(Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                        BirthdayDto item = new BirthdayDto(R.drawable.birthday, data[0], birthday);
                        list.addValue(item);
                    } else {
                        _logger.Warn("data[0] or data[1] or data[2] or data[3] is null!");
                    }
                } else {
                    _logger.Warn("Wrong size of birthdayData: " + String.valueOf(data.length));
                }
            }
            return list;
        }

        _logger.Warn("Found no birthday!");
        return new SerializableList<>();
    }
}
