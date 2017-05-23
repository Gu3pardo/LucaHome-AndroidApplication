package guepardoapps.library.lucahome.converter.wear;

import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class MessageToSocketConverter {

    private static final String TAG = MessageToSocketConverter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String SOCKETS = "Sockets:";

    public MessageToSocketConverter() {
        _logger = new LucaHomeLogger(TAG);
    }

    public SerializableList<WirelessSocketDto> ConvertMessageToSocketList(@NonNull String message) {
        if (message.startsWith(SOCKETS)) {
            _logger.Debug("message starts with " + SOCKETS + "! replacing!");
            message = message.replace(SOCKETS, "");
        }
        _logger.Debug("message: " + message);

        String[] items = message.split("\\&");
        if (items.length > 0) {
            SerializableList<WirelessSocketDto> list = new SerializableList<>();
            for (int index = 0; index < items.length; index++) {
                String entry = items[index];
                String[] data = entry.split("\\:");

                _logger.Info("Socket dataContent");
                for (String dataContent : data) {
                    _logger.Info(dataContent);
                }

                if (data.length == 2) {
                    if (data[0] != null && data[1] != null) {
                        WirelessSocketDto item = new WirelessSocketDto(
                                index,
                                R.drawable.socket,
                                data[0],
                                (data[1].contains("1")));
                        list.addValue(item);
                    } else {
                        _logger.Warn("data[0] or data[1] is null!");
                    }
                } else {
                    _logger.Warn("Wrong size of socketData: " + String.valueOf(data.length));
                }
            }
            return list;
        }

        _logger.Warn("Found no socket!");
        return new SerializableList<>();
    }
}
