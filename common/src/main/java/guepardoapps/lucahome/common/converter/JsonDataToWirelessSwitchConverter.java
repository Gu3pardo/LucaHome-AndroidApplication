package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToWirelessSwitchConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToWirelessSwitchConverter.class.getSimpleName();
    private static Logger _logger;

    private static String _searchParameter = "{\"Data\":";

    public JsonDataToWirelessSwitchConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<WirelessSwitch> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<WirelessSwitch> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private static SerializableList<WirelessSwitch> parseStringToList(String value) {
        try {
            if (!value.contains("Error")) {
                SerializableList<WirelessSwitch> list = new SerializableList<>();

                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("WirelessSwitch");

                    String name = child.getString("Name");
                    String area = child.getString("Area");

                    int remoteId = child.getInt("RemoteId");
                    char keyCode = child.getString("KeyCode").charAt(0);

                    boolean action = child.getString("Action").contains("1");

                    JSONObject lastTriggerJsonData = child.getJSONObject("LastTrigger");

                    int year = lastTriggerJsonData.getInt("Year");
                    int month = lastTriggerJsonData.getInt("Month");
                    int day = lastTriggerJsonData.getInt("Day");
                    int hour = lastTriggerJsonData.getInt("Hour");
                    int minute = lastTriggerJsonData.getInt("Minute");

                    if (year == -1 || month == -1 || day == -1 || hour == -1 || minute == -1) {
                        year = 1970;
                        month = 1;
                        day = 1;
                        hour = 0;
                        minute = 0;
                    }

                    String lastTriggerUser = lastTriggerJsonData.getString("UserName");

                    WirelessSwitch newWirelessSwitch = new WirelessSwitch(
                            dataIndex,
                            name, area, remoteId, keyCode, false, action,
                            new SerializableDate(year, month, day), new SerializableTime(hour, minute, 0, 0), lastTriggerUser,
                            true, ILucaClass.LucaServerDbAction.Null);
                    list.addValue(newWirelessSwitch);
                }

                return list;
            }
        } catch (JSONException jsonException) {
            _logger.Error(jsonException.getMessage());
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }
}