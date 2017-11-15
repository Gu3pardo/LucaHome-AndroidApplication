package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.MapContent;
import guepardoapps.lucahome.common.classes.Schedule;
import guepardoapps.lucahome.common.classes.Temperature;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public final class JsonDataToMapContentConverter {
    private static final String TAG = JsonDataToMapContentConverter.class.getSimpleName();
    private static Logger _logger;

    private static String _searchParameter = "{mapcontent:";

    public JsonDataToMapContentConverter() {
        _logger = new Logger(TAG);
    }

    public SerializableList<MapContent> GetList(
            @NonNull String[] stringArray,
            @NonNull SerializableList<Temperature> temperatureList,
            @NonNull SerializableList<WirelessSocket> socketList,
            @NonNull SerializableList<Schedule> scheduleList) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0], temperatureList, socketList, scheduleList);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry, temperatureList, socketList, scheduleList);
        }
    }

    public SerializableList<MapContent> GetList(
            @NonNull String jsonString,
            @NonNull SerializableList<Temperature> temperatureList,
            @NonNull SerializableList<WirelessSocket> socketList,
            @NonNull SerializableList<Schedule> scheduleList) {
        return parseStringToList(jsonString, temperatureList, socketList, scheduleList);
    }

    public MapContent Get(
            @NonNull String value,
            @NonNull SerializableList<Temperature> temperatureList,
            @NonNull SerializableList<WirelessSocket> socketList,
            @NonNull SerializableList<Schedule> scheduleList) {
        _logger.Debug("MapContentDto Get");
        _logger.Debug(value);

        if (!value.contains("Error")) {
            if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
                if (value.contains(_searchParameter)) {
                    value = value.replace(_searchParameter, "").replace("};};", "");
                    _logger.Debug(value);
                    String[] data = value.split("\\};");
                    MapContent newValue = parseStringToValue(data, temperatureList, socketList, scheduleList);
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

    private SerializableList<MapContent> parseStringToList(
            @NonNull String value,
            @NonNull SerializableList<Temperature> temperatureList,
            @NonNull SerializableList<WirelessSocket> socketList,
            @NonNull SerializableList<Schedule> scheduleList) {
        _logger.Debug("SerializableList<MapContentDto> ParseStringToList");
        _logger.Debug(value);

        if (!value.contains("Error")) {
            if (StringHelper.GetStringCount(value, _searchParameter) > 0) {
                if (value.contains(_searchParameter)) {
                    SerializableList<MapContent> list = new SerializableList<>();

                    String[] entries = value.split("\\" + _searchParameter);
                    for (String entry : entries) {
                        _logger.Debug(entry);
                        entry = entry.replace(_searchParameter, "").replace("};};", "");
                        _logger.Debug(entry);

                        String[] data = entry.split("\\};");
                        MapContent newValue = parseStringToValue(data, temperatureList, socketList, scheduleList);
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

    private MapContent parseStringToValue(
            @NonNull String[] data,
            @NonNull SerializableList<Temperature> temperatureList,
            @NonNull SerializableList<WirelessSocket> socketList,
            @NonNull SerializableList<Schedule> scheduleList) {
        _logger.Debug("MapContentDto ParseStringToValue");

        if (data.length == 8) {
            _logger.Warning("Length is 8! trying to fix!");
            if (data[0].length() == 0) {
                _logger.Warning("Value at index 0 is null! Trying to fix...");
                String[] fixedData = new String[7];
                System.arraycopy(data, 1, fixedData, 0, 7);
                data = fixedData;
            }
        }

        if (data.length == 7) {
            if (data[0].contains("{id:")
                    && data[1].contains("{position:")
                    && data[2].contains("{type:")
                    && data[3].contains("{schedules:")
                    && data[4].contains("{sockets:")
                    && data[5].contains("{temperatureArea:")
                    && data[6].contains("{visibility:")) {

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
                MapContent.DrawingType drawingType = MapContent.DrawingType.values()[typeInteger];

                SerializableList<Schedule> mapScheduleList = new SerializableList<>();
                String scheduleString = data[3].replace("{schedules:", "").replace("};", "");
                String[] scheduleStringList = scheduleString.split("\\|");
                for (String entry : scheduleStringList) {
                    for (int scheduleIndex = 0; scheduleIndex < scheduleList.getSize(); scheduleIndex++) {
                        Schedule currentSchedule = scheduleList.getValue(scheduleIndex);
                        if (entry.contains(currentSchedule.GetName())) {
                            mapScheduleList.addValue(currentSchedule);
                            break;
                        }
                    }
                }

                WirelessSocket socket = null;
                boolean foundSocket = false;
                String socketString = data[4].replace("{sockets:", "").replace("};", "");
                String[] socketStringList = socketString.split("\\|");
                for (String entry : socketStringList) {
                    for (int socketIndex = 0; socketIndex < socketList.getSize(); socketIndex++) {
                        WirelessSocket currentSocket = socketList.getValue(socketIndex);
                        if (entry.contains(currentSocket.GetName())) {
                            socket = currentSocket;
                            foundSocket = true;
                            break;
                        }
                    }
                    if (foundSocket) {
                        break;
                    }
                }

                String temperatureArea = data[5].replace("{temperatureArea:", "").replace("};", "");

                Temperature temperature = null;
                if (drawingType == MapContent.DrawingType.Temperature) {
                    for (int temperatureIndex = 0; temperatureIndex < temperatureList.getSize(); temperatureIndex++) {
                        Temperature currentTemperature = temperatureList.getValue(temperatureIndex);
                        if (currentTemperature.GetArea().contains(temperatureArea)) {
                            temperature = currentTemperature;
                            break;
                        }
                    }
                }

                boolean visibility = data[6].contains("1");

                MapContent newValue = new MapContent(
                        id,
                        position,
                        drawingType,
                        temperatureArea,
                        socket,
                        scheduleList,
                        temperature,
                        visibility,
                        true,
                        ILucaClass.LucaServerDbAction.Null);
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