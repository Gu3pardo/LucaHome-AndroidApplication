package guepardoapps.library.lucahome.converter.json;

import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.library.lucahome.common.dto.MapContentDto;
import guepardoapps.library.lucahome.common.enums.DrawingType;
import guepardoapps.library.lucahome.common.enums.MediaServerSelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.StringHelper;

public final class JsonDataToMapContentConverter {

    private static final String TAG = JsonDataToMapContentConverter.class.getSimpleName();
    private static LucaHomeLogger _logger;

    private static String _searchParameter = "{mapcontent:";

    public static SerializableList<MapContentDto> GetList(String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return ParseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return ParseStringToList(usedEntry);
        }
    }

    public static MapContentDto Get(String value) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Debug("MapContentDto Get");
        _logger.Debug(value);

        if (!value.contains("Error")) {
            if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
                if (value.contains(_searchParameter)) {
                    value = value.replace(_searchParameter, "").replace("};};", "");
                    _logger.Debug(value);
                    String[] data = value.split("\\};");
                    MapContentDto newValue = ParseStringToValue(data);
                    if (newValue != null) {
                        return newValue;
                    }
                } else {
                    _logger.Error("MapContent does not contain " + _searchParameter);
                }
            } else {
                _logger.Error("String count of mapContent is not 1!");
            }
        } else {
            _logger.Error("mapContent contains error!");
        }

        _logger.Error(value + " has an error!");

        return null;
    }

    private static SerializableList<MapContentDto> ParseStringToList(String value) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Debug("SerializableList<MapContentDto> ParseStringToList");
        _logger.Debug(value);

        if (!value.contains("Error")) {
            if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
                if (value.contains(_searchParameter)) {
                    SerializableList<MapContentDto> list = new SerializableList<>();

                    String[] entries = value.split("\\" + _searchParameter);
                    for (String entry : entries) {
                        _logger.Debug(entry);
                        entry = entry.replace(_searchParameter, "").replace("};};", "");
                        _logger.Debug(entry);

                        String[] data = entry.split("\\};");
                        MapContentDto newValue = ParseStringToValue(data);
                        if (newValue != null) {
                            list.addValue(newValue);
                        }
                    }

                    return list;
                } else {
                    _logger.Error("MapContent does not contain " + _searchParameter);
                }
            } else {
                _logger.Error("String count of mapContent is not bigger then 1!");
            }
        } else {
            _logger.Error("mapContent contains error!");
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }

    private static MapContentDto ParseStringToValue(String[] data) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Debug("MapContentDto ParseStringToValue");

        if (data.length == 8) {
            _logger.Warn("Length is 8! trying to fix!");
            if (data[0].length() == 0) {
                _logger.Warn("Value at index 0 is null! Trying to fix...");
                String[] fixedData = new String[7];
                System.arraycopy(data, 1, fixedData, 0, 7);
                data = fixedData;
            }
        }

        if (data.length == 7) {
            if (data[0].contains("{id:") && data[1].contains("{position:") && data[2].contains("{type:")
                    && data[3].contains("{schedules:") && data[4].contains("{sockets:")
                    && data[5].contains("{temperatureArea:") && data[6].contains("{visibility:")) {
                String idString = data[0].replace("{id:", "").replace("};", "");
                int id = Integer.parseInt(idString);

                String positionString = data[1].replace("{position:", "").replace("};", "");
                String[] coordinates = positionString.split("\\|");
                int x = Integer.parseInt(coordinates[0]);
                int y = Integer.parseInt(coordinates[1]);

                int[] position = new int[2];
                position[0] = x;
                position[1] = y;

                String typeString = data[2].replace("{type:", "").replace("};", "");
                int typeInteger = Integer.parseInt(typeString);
                DrawingType drawingType = DrawingType.GetById(typeInteger);

                ArrayList<String> scheduleList = new ArrayList<>();
                String scheduleString = data[3].replace("{schedules:", "").replace("};", "");
                String[] schedulesArray = scheduleString.split("\\|");
                for (String entry : schedulesArray) {
                    if (entry.length() > 0) {
                        scheduleList.add(entry);
                    }
                }

                ArrayList<String> socketList = new ArrayList<>();
                String socketString = data[4].replace("{sockets:", "").replace("};", "");
                String[] socketsArray = socketString.split("\\|");
                for (String entry : socketsArray) {
                    if (entry.length() > 0) {
                        socketList.add(entry);
                    }
                }

                String temperatureString = data[5].replace("{temperatureArea:", "").replace("};", "");

                String mediaMirrorIp = "";
                for (String socket : socketList) {
                    _logger.Debug("Checking socket " + socket);
                    if (socket.contains("MediaMirror")) {
                        _logger.Debug("Socket contains MediaMirror!");
                        try {
                            mediaMirrorIp = MediaServerSelection.GetBySocket(socket).GetIp();
                            _logger.Debug(String.format("Ip for %s is %s", socket, mediaMirrorIp));
                        } catch (Exception ex) {
                            _logger.Error(ex.toString());
                        }
                        break;
                    }
                }

                boolean visibility = data[6].contains("1");

                MapContentDto newValue = new MapContentDto(
                        id,
                        position,
                        drawingType,
                        scheduleList,
                        socketList,
                        temperatureString,
                        mediaMirrorIp,
                        visibility);
                _logger.Debug(String.format(Locale.getDefault(), "New MapContentDto %s", newValue));

                return newValue;
            } else {
                _logger.Error("MapContent does contain valid parameter");
            }
        } else {
            _logger.Error("MapContent has wrong size (not 7!): " + String.valueOf(data.length));
        }

        _logger.Error("Data has an error!");

        return null;
    }
}