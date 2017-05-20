package guepardoapps.library.lucahome.converter.wear;

import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class MessageToMenuConverter {

    private static final String TAG = MessageToMenuConverter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String MENU = "Menu:";

    public MessageToMenuConverter() {
        _logger = new LucaHomeLogger(TAG);
    }

    public SerializableList<MenuDto> ConvertMessageToList(String message) {
        if (message.startsWith(MENU)) {
            _logger.Debug("message starts with " + MENU + "! replacing!");
            message = message.replace(MENU, "");
        }
        _logger.Debug("message: " + message);

        String[] items = message.split("\\&");
        if (items.length > 0) {
            SerializableList<MenuDto> list = new SerializableList<>();
            for (String entry : items) {
                String[] data = entry.split("\\:");

                _logger.Info("Menu dataContent");
                for (String dataContent : data) {
                    _logger.Info(dataContent);
                }

                if (data.length == 6) {
                    if (data[0] != null && data[1] != null && data[2] != null && data[3] != null && data[4] != null
                            && data[5] != null) {
                        MenuDto item = new MenuDto(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]),
                                Integer.parseInt(data[3]), data[4], data[5]);
                        list.addValue(item);
                    } else {
                        _logger.Warn("data[0] or data[1] or data[2] or data[3] or data[4] or data[5] is null!");
                    }
                } else {
                    _logger.Warn("Wrong size of menuData: " + String.valueOf(data.length));
                }
            }
            return list;
        }

        _logger.Warn("Found no menu!");
        return new SerializableList<>();
    }
}
