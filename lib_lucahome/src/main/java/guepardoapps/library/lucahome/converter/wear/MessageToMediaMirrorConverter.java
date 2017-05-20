package guepardoapps.library.lucahome.converter.wear;

import java.util.ArrayList;

import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.dto.MediaMirrorViewDto;
import guepardoapps.library.lucahome.common.enums.MediaMirrorSelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

public class MessageToMediaMirrorConverter {

    private static final String TAG = MessageToMediaMirrorConverter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String MEDIAMIRROR = "MediaMirror:";

    public MessageToMediaMirrorConverter() {
        _logger = new LucaHomeLogger(TAG);
    }

    public ArrayList<MediaMirrorViewDto> ConvertMessageToList(@NonNull String message) {
        if (message.startsWith(MEDIAMIRROR)) {
            _logger.Debug("message starts with " + MEDIAMIRROR + "! replacing!");
            message = message.replace(MEDIAMIRROR, "");
        }
        _logger.Debug("message: " + message);

        String[] items = message.split("\\&");
        if (items.length > 0) {
            ArrayList<MediaMirrorViewDto> list = new ArrayList<>();
            for (String entry : items) {
                String[] data = entry.split("\\:");

                if (data.length == 5) {
                    if (data[0] != null && data[1] != null && data[2] != null && data[3] != null && data[4] != null) {
                        MediaMirrorSelection selection = MediaMirrorSelection.GetByIp(data[0]);
                        if (selection == MediaMirrorSelection.NULL) {
                            continue;
                        }

                        String batteryLevelString = data[1];
                        int batteryLevel = -1;
                        try {
                            batteryLevel = Integer.parseInt(batteryLevelString);
                        } catch (Exception ex) {
                            _logger.Error(ex.toString());
                        }

                        String volumeString = data[2];
                        int volume = -1;
                        try {
                            volume = Integer.parseInt(volumeString);
                        } catch (Exception ex) {
                            _logger.Error(ex.toString());
                        }

                        String youtubeId = data[3];

                        String serverVersion = data[4];

                        MediaMirrorViewDto item = new MediaMirrorViewDto(selection, batteryLevel, volume, youtubeId,
                                serverVersion);

                        list.add(item);
                    } else {
                        _logger.Warn("data[0] or data[1] or data[2] or data[3] or data[4] is null!");
                    }
                } else {
                    _logger.Warn("Wrong size of mediaMirrorData: " + String.valueOf(data.length));
                }
            }
            return list;
        }

        _logger.Warn("Found no mediaMirror!");
        return new ArrayList<>();
    }
}
