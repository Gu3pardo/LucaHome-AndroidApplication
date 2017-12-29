package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.LucaTimer;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.enums.SocketAction;
import guepardoapps.lucahome.common.enums.Weekday;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public final class JsonDataToTimerConverter {
    private static final String TAG = JsonDataToTimerConverter.class.getSimpleName();
    private static final String SEARCH_PARAMETER = "{\"Data\":";

    private static final JsonDataToTimerConverter SINGLETON = new JsonDataToTimerConverter();

    public static JsonDataToTimerConverter getInstance() {
        return SINGLETON;
    }

    private JsonDataToTimerConverter() {
    }

    public SerializableList<LucaTimer> GetList(
            @NonNull String[] stringArray,
            @NonNull SerializableList<WirelessSocket> wirelessSocketList,
            @NonNull SerializableList<WirelessSwitch> wirelessSwitchList) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0], wirelessSocketList, wirelessSwitchList);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SEARCH_PARAMETER);
            return parseStringToList(usedEntry, wirelessSocketList, wirelessSwitchList);
        }
    }

    public SerializableList<LucaTimer> GetList(
            @NonNull String jsonString,
            @NonNull SerializableList<WirelessSocket> wirelessSocketList,
            @NonNull SerializableList<WirelessSwitch> wirelessSwitchList) {
        return parseStringToList(jsonString, wirelessSocketList, wirelessSwitchList);
    }

    private SerializableList<LucaTimer> parseStringToList(
            @NonNull String value,
            @NonNull SerializableList<WirelessSocket> wirelessSocketList,
            @NonNull SerializableList<WirelessSwitch> wirelessSwitchList) {
        if (!value.contains("Error")) {
            SerializableList<LucaTimer> list = new SerializableList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("Schedule");

                    boolean isTimer = child.getString("IsTimer").contains("1");
                    if (!isTimer) {
                        continue;
                    }

                    int id = child.getInt("Id");

                    String name = child.getString("Name");

                    String wirelessSocketName = child.getString("Socket");
                    WirelessSocket wirelessSocket = getWirelessSocket(wirelessSocketName, wirelessSocketList);

                    // TODO Gpios currently not supported in LucaHome Android application
                    // String gpioName = child.getString("Gpio");

                    String wirelessSwitchName = child.getString("Switch");
                    WirelessSwitch wirelessSwitch = getWirelessSwitch(wirelessSwitchName, wirelessSwitchList);

                    int weekdayInteger = child.getInt("Weekday");
                    Weekday weekday = Weekday.GetById(weekdayInteger);

                    int hour = child.getInt("Hour");
                    int minute = child.getInt("Minute");
                    SerializableTime time = new SerializableTime(hour, minute, 0, 0);

                    SocketAction action = child.getString("Action").contains("1") ? SocketAction.Activate : SocketAction.Deactivate;
                    boolean isActive = child.getString("IsActive").contains("1");

                    LucaTimer newTimer = new LucaTimer(id, name, wirelessSocket, wirelessSwitch, weekday, time, action, isActive, true, ILucaClass.LucaServerDbAction.Null);
                    list.addValue(newTimer);
                }
            } catch (JSONException jsonException) {
                Logger.getInstance().Error(TAG, jsonException.getMessage());
            }
            return list;
        }

        Logger.getInstance().Error(TAG, value + " has an error!");
        return new SerializableList<>();
    }

    private WirelessSocket getWirelessSocket(@NonNull String wirelessSocketName, @NonNull SerializableList<WirelessSocket> wirelessSocketList) {
        for (int index = 0; index < wirelessSocketList.getSize(); index++) {
            if (wirelessSocketList.getValue(index).GetName().contains(wirelessSocketName)) {
                return wirelessSocketList.getValue(index);
            }
        }
        return null;
    }

    private WirelessSwitch getWirelessSwitch(String wirelessSwitchName, SerializableList<WirelessSwitch> wirelessSwitchList) {
        for (int index = 0; index < wirelessSwitchList.getSize(); index++) {
            if (wirelessSwitchList.getValue(index).GetName().contains(wirelessSwitchName)) {
                return wirelessSwitchList.getValue(index);
            }
        }
        return null;
    }
}