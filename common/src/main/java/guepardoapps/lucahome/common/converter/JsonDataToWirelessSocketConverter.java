package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToWirelessSocketConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToWirelessSocketConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToWirelessSocketConverter Singleton = new JsonDataToWirelessSocketConverter();

    public static JsonDataToWirelessSocketConverter getInstance() {
        return Singleton;
    }

    private JsonDataToWirelessSocketConverter() {
    }

    @Override
    public ArrayList<WirelessSocket> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<WirelessSocket> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private static ArrayList<WirelessSocket> parseStringToList(String value) {
        if (!value.contains("Error")) {
            ArrayList<WirelessSocket> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("WirelessSocket");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));
                    UUID roomUuid = UUID.fromString(child.getString("RoomUuid"));

                    String name = child.getString("Name");
                    String code = child.getString("Code");

                    boolean state = !child.getString("State").contains("0") && !child.getString("State").contains("-1");

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

                    Calendar lastTriggerDateTime = Calendar.getInstance();
                    lastTriggerDateTime.set(year, month, day, hour, minute);

                    String lastTriggerUser = lastTriggerJsonData.getString("UserName");

                    WirelessSocket newWirelessSocket = new WirelessSocket(uuid, roomUuid, name, code, state, lastTriggerDateTime, lastTriggerUser, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newWirelessSocket);
                }
            } catch (JSONException jsonException) {
                Logger.getInstance().Error(Tag, jsonException.getMessage());
            }
            return list;
        }

        Logger.getInstance().Error(Tag, value + " has an error!");
        return new ArrayList<>();
    }
}