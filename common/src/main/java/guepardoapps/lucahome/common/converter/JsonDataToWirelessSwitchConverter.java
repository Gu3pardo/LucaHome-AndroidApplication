package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToWirelessSwitchConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToWirelessSwitchConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToWirelessSwitchConverter Singleton = new JsonDataToWirelessSwitchConverter();

    public static JsonDataToWirelessSwitchConverter getInstance() {
        return Singleton;
    }

    private JsonDataToWirelessSwitchConverter() {
    }

    @Override
    public ArrayList<WirelessSwitch> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<WirelessSwitch> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private static ArrayList<WirelessSwitch> parseStringToList(String value) {
        if (!value.contains("Error")) {
            ArrayList<WirelessSwitch> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("WirelessSwitch");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));
                    UUID roomUuid = UUID.fromString(child.getString("RoomUuid"));

                    String name = child.getString("Name");

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

                    Calendar lastTriggerDateTime = Calendar.getInstance();
                    lastTriggerDateTime.set(year, month, day, hour, minute);

                    String lastTriggerUser = lastTriggerJsonData.getString("UserName");

                    WirelessSwitch newWirelessSwitch = new WirelessSwitch(uuid, roomUuid, name, remoteId, keyCode, action, lastTriggerDateTime, lastTriggerUser, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newWirelessSwitch);
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