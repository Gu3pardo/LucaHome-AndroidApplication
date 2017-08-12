package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.LucaTimer;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.enums.SocketAction;
import guepardoapps.lucahome.common.enums.Weekday;

public final class JsonDataToTimerConverter {
    private static final String TAG = JsonDataToTimerConverter.class.getSimpleName();
    private Logger _logger;

    private static String _searchParameter = "{schedule:";

    public JsonDataToTimerConverter() {
        _logger = new Logger(TAG);
    }

    public SerializableList<LucaTimer> GetList(
            @NonNull String[] stringArray,
            @NonNull SerializableList<WirelessSocket> socketList) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            _logger.Information("StringsAreEqual");
            return parseStringToList(stringArray[0], socketList);
        } else {
            _logger.Information("Selecting entry");
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            _logger.Information("usedEntry: " + usedEntry);
            return parseStringToList(usedEntry, socketList);
        }
    }

    public SerializableList<LucaTimer> GetList(
            @NonNull String jsonString,
            @NonNull SerializableList<WirelessSocket> socketList) {
        return parseStringToList(jsonString, socketList);
    }

    private SerializableList<LucaTimer> parseStringToList(
            @NonNull String value,
            @NonNull SerializableList<WirelessSocket> socketList) {
        if (StringHelper.GetStringCount(value, _searchParameter) >= 1) {
            if (value.contains(_searchParameter)) {
                SerializableList<LucaTimer> list = new SerializableList<>();

                String[] entries = value.split("\\" + _searchParameter);
                _logger.Information(String.format("Found %s entries!", entries.length));
                for (int index = 0; index < entries.length; index++) {
                    String entry = entries[index];
                    if (entry == null) {
                        _logger.Error("Entry is null!");
                        continue;
                    }

                    entry = entry.replace(_searchParameter, "").replace("};};", "");
                    _logger.Information(String.format("Timer entry is %s", entry));

                    String[] data = entry.split("\\};");
                    _logger.Information(String.format("Split data %s has size is %s", data, data.length));
                    LucaTimer newValue = ParseStringToValue(index, data, socketList);
                    if (newValue != null) {
                        _logger.Information(String.format("Adding new value %s to list %s", newValue, list));
                        list.addValue(newValue);
                    } else {
                        _logger.Warning("newValue is null! Skipping!");
                    }
                }
                return list;
            } else {
                _logger.Error(String.format("Entry %s does not contain searchParameter %s!", value, _searchParameter));
            }
        } else {
            _logger.Error(String.format("Entry %s does contain %sx searchParameter!", value,
                    StringHelper.GetStringCount(value, _searchParameter)));
        }

        _logger.Error("ParseStringToList: " + value + " has an error!");

        return new SerializableList<>();
    }

    private LucaTimer ParseStringToValue(
            int id,
            @NonNull String[] data,
            @NonNull SerializableList<WirelessSocket> socketList) {
        if (data.length == 11) {
            if (data[0].contains("{Name:")
                    && data[1].contains("{Socket:")
                    && data[2].contains("{Gpio:")
                    && data[3].contains("{Weekday:")
                    && data[4].contains("{Hour:")
                    && data[5].contains("{Minute:")
                    && data[6].contains("{OnOff:")
                    && data[7].contains("{IsTimer:")
                    && data[8].contains("{PlaySound:")
                    && data[9].contains("{Raspberry:")
                    && data[10].contains("{State:")) {

                String name = data[0].replace("{Name:", "").replace("};", "");

                String socketName = data[1].replace("{Socket:", "").replace("};", "");
                WirelessSocket socket = null;
                for (int index = 0; index < socketList.getSize(); index++) {
                    if (socketList.getValue(index).GetName().contains(socketName)) {
                        socket = socketList.getValue(index);
                        break;
                    }
                }

                if (socket == null) {
                    _logger.Error("Socket is null!");
                    return null;
                }

                String weekdayString = data[3].replace("{Weekday:", "").replace("};", "");
                int weekdayInteger = Integer.parseInt(weekdayString);
                Weekday weekday = Weekday.GetById(weekdayInteger);

                String hourString = data[4].replace("{Hour:", "").replace("};", "");
                int Hour = Integer.parseInt(hourString);
                String minuteString = data[5].replace("{Minute:", "").replace("};", "");
                int Minute = Integer.parseInt(minuteString);
                SerializableTime time = new SerializableTime(Hour, Minute, 0, 0);

                String actionString = data[6].replace("{OnOff:", "").replace("};", "");
                boolean actionBoolean = actionString.contains("1");
                SocketAction socketAction = actionBoolean ? SocketAction.Activate : SocketAction.Deactivate;

                String isTimerString = data[7].replace("{IsTimer:", "").replace("};", "");
                boolean isTimer = isTimerString.contains("1");

                //String playSoundString = data[8].replace("{PlaySound:", "").replace("};", "");
                //boolean playSound = playSoundString.contains("1");
                //String playRaspberryString = data[9].replace("{Raspberry:", "").replace("};", "");

                String IsActiveString = data[10].replace("{State:", "").replace("};", "");
                boolean isActive = IsActiveString.contains("1");

                if (!isTimer) {
                    _logger.Warning("isSchedule!");
                    return null;
                } else {
                    LucaTimer newValue = new LucaTimer(
                            id,
                            name,
                            "",
                            socket,
                            weekday,
                            time,
                            socketAction,
                            isActive);
                    _logger.Debug(String.format(Locale.getDefault(), "New Schedule %s", newValue));

                    return newValue;
                }
            } else {
                _logger.Error("Invalid entries!!!");
                for (String dataEntry : data) {
                    _logger.Warning(dataEntry);
                }
            }
        }

        _logger.Error("Data has an error!");
        return null;
    }
}