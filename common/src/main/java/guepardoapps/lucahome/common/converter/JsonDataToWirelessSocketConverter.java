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
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToWirelessSocketConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToWirelessSocketConverter.class.getSimpleName();
    private static final String SEARCH_PARAMETER = "{\"Data\":";

    private static final JsonDataToWirelessSocketConverter SINGLETON = new JsonDataToWirelessSocketConverter();

    public static JsonDataToWirelessSocketConverter getInstance() {
        return SINGLETON;
    }

    private JsonDataToWirelessSocketConverter() {
    }

    @Override
    public SerializableList<WirelessSocket> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SEARCH_PARAMETER);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<WirelessSocket> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private static SerializableList<WirelessSocket> parseStringToList(String value) {
        if (!value.contains("Error")) {
            SerializableList<WirelessSocket> list = new SerializableList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("WirelessSocket");

                    int typeId = child.getInt("TypeId");

                    String name = child.getString("Name");
                    String area = child.getString("Area");
                    String code = child.getString("Code");

                    boolean isActivated = !child.getString("State").contains("0") && !child.getString("State").contains("-1");

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

                    WirelessSocket newWirelessSocket = new WirelessSocket(
                            typeId,
                            name, area, code, isActivated,
                            new SerializableDate(year, month, day), new SerializableTime(hour, minute, 0, 0), lastTriggerUser,
                            true, ILucaClass.LucaServerDbAction.Null);
                    list.addValue(newWirelessSocket);
                }
            } catch (JSONException jsonException) {
                Logger.getInstance().Error(TAG, jsonException.getMessage());
            }
            return list;
        }

        Logger.getInstance().Error(TAG, value + " has an error!");
        return new SerializableList<>();
    }
}