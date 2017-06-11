package guepardoapps.library.lucahome.converter.json;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.library.lucahome.common.dto.ScheduleDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.classes.SerializableTime;
import guepardoapps.library.toolset.common.enums.Weekday;
import guepardoapps.library.toolset.common.StringHelper;

public final class JsonDataToScheduleConverter {

    private static final String TAG = JsonDataToScheduleConverter.class.getSimpleName();
    private static LucaHomeLogger _logger;

    private static String _searchParameter = "{schedule:";

    public static SerializableList<ScheduleDto> GetList(
            @NonNull String[] stringArray,
            @NonNull SerializableList<WirelessSocketDto> socketList) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }

        if (StringHelper.StringsAreEqual(stringArray)) {
            _logger.Info("StringsAreEqual");
            return ParseStringToList(stringArray[0], socketList);
        } else {
            _logger.Info("Selecting entry");
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            _logger.Info("usedEntry: " + usedEntry);
            return ParseStringToList(usedEntry, socketList);
        }
    }

    private static SerializableList<ScheduleDto> ParseStringToList(
            @NonNull String value,
            @NonNull SerializableList<WirelessSocketDto> socketList) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }

        if (StringHelper.GetStringCount(value, _searchParameter) >= 1) {
            if (value.contains(_searchParameter)) {
                SerializableList<ScheduleDto> list = new SerializableList<>();

                String[] entries = value.split("\\" + _searchParameter);
                _logger.Info(String.format("Found %s entries!", entries.length));
                for (int index = 0; index < entries.length; index++) {
                    String entry = entries[index];
                    if (entry == null) {
                        _logger.Error("Entry is null!");
                        continue;
                    }

                    entry = entry.replace(_searchParameter, "").replace("};};", "");
                    _logger.Info(String.format("Schedule entry is %s", entry));

                    String[] data = entry.split("\\};");
                    _logger.Info(String.format("Split data %s has size is %s", data, data.length));
                    ScheduleDto newValue = ParseStringToValue(index, data, socketList);
                    if (newValue != null) {
                        _logger.Info(String.format("Adding new value %s to list %s", newValue, list));
                        list.addValue(newValue);
                    } else {
                        _logger.Warn("newValue is null! Skipping!");
                    }
                }
                return list;
            } else {
                _logger.Error(String.format("Entry %s does not contain searchparameter %s!", value, _searchParameter));
            }
        } else {
            _logger.Error(String.format("Entry %s does contain %sx searchparameter!", value,
                    StringHelper.GetStringCount(value, _searchParameter)));
        }

        _logger.Error("ParseStringToList: " + value + " has an error!");

        return new SerializableList<>();
    }

    private static ScheduleDto ParseStringToValue(
            int id,
            @NonNull String[] data,
            @NonNull SerializableList<WirelessSocketDto> socketList) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }

        if (data.length == 11) {
            if (data[0].contains("{Name:") && data[1].contains("{Socket:") && data[2].contains("{Gpio:")
                    && data[3].contains("{Weekday:") && data[4].contains("{Hour:") && data[5].contains("{Minute:")
                    && data[6].contains("{OnOff:") && data[7].contains("{IsTimer:") && data[8].contains("{PlaySound:")
                    && data[9].contains("{Raspberry:") && data[10].contains("{State:")) {

                String name = data[0].replace("{Name:", "").replace("};", "");

                String socketName = data[1].replace("{Socket:", "").replace("};", "");
                WirelessSocketDto socket = new WirelessSocketDto();
                for (int index = 0; index < socketList.getSize(); index++) {
                    if (socketList.getValue(index).GetName().contains(socketName)) {
                        socket = socketList.getValue(index);
                        break;
                    }
                }

                String WeekdayString = data[3].replace("{Weekday:", "").replace("};", "");
                int weekdayInteger = Integer.parseInt(WeekdayString);
                Weekday weekday = Weekday.GetById(weekdayInteger);

                String HourString = data[4].replace("{Hour:", "").replace("};", "");
                int Hour = Integer.parseInt(HourString);
                String MinuteString = data[5].replace("{Minute:", "").replace("};", "");
                int Minute = Integer.parseInt(MinuteString);
                SerializableTime time = new SerializableTime(Hour, Minute, 0, 0);

                String ActionString = data[6].replace("{OnOff:", "").replace("};", "");
                boolean action = ActionString.contains("1");

                String IsTimerString = data[7].replace("{IsTimer:", "").replace("};", "");
                boolean isTimer = IsTimerString.contains("1");

                String PlaySoundString = data[8].replace("{PlaySound:", "").replace("};", "");
                boolean playSound = PlaySoundString.contains("1");

                //String PlayRaspberryString = data[9].replace("{Raspberry:", "").replace("};", "");

                String IsActiveString = data[10].replace("{State:", "").replace("};", "");
                boolean isActive = IsActiveString.contains("1");

                if (!isTimer) {
                    ScheduleDto newValue = new ScheduleDto(
                            id,
                            name,
                            socket,
                            weekday,
                            time,
                            action,
                            playSound,
                            isActive);
                    _logger.Debug(String.format(Locale.getDefault(), "New ScheduleDto %s", newValue));

                    return newValue;
                } else {
                    _logger.Warn("isTimer!");
                    return null;
                }
            } else {
                _logger.Error("Invalid entries!!!");
                for (String dataEntry : data) {
                    _logger.Warn(dataEntry);
                }
            }
        }

        _logger.Error(String.format(Locale.getDefault(), "Data %s has an error! Size is %s", data, data.length));

        return null;
    }
}