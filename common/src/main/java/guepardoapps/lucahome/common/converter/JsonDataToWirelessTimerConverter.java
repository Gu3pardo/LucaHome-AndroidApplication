package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.WirelessTimer;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToWirelessTimerConverter {
    private static final String Tag = JsonDataToWirelessTimerConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToWirelessTimerConverter Singleton = new JsonDataToWirelessTimerConverter();

    public static JsonDataToWirelessTimerConverter getInstance() {
        return Singleton;
    }

    private JsonDataToWirelessTimerConverter() {
    }

    public ArrayList<WirelessTimer> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    public ArrayList<WirelessTimer> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private ArrayList<WirelessTimer> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            ArrayList<WirelessTimer> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("WirelessSchedule");

                    boolean isTimer = child.getString("IsTimer").contains("1");
                    if (!isTimer) {
                        continue;
                    }

                    UUID uuid = UUID.fromString(child.getString("Uuid"));
                    String name = child.getString("Name");

                    UUID wirelessSocketUuid = UUID.fromString(child.getString("WirelessSocketUuid"));
                    boolean wirelessSocketAction = child.getString("WirelessSocketAction").contains("1");

                    UUID wirelessSwitchUuid = UUID.fromString(child.getString("WirelessSwitchUuid"));

                    int weekdayInteger = child.getInt("Weekday");
                    int hour = child.getInt("Hour");
                    int minute = child.getInt("Minute");

                    Calendar dateTime = Calendar.getInstance();
                    dateTime.set(Calendar.DAY_OF_WEEK, weekdayInteger);
                    dateTime.set(Calendar.HOUR, hour);
                    dateTime.set(Calendar.MINUTE, minute);

                    boolean isActive = child.getString("IsActive").contains("1");

                    WirelessTimer newWirelessTimer = new WirelessTimer(uuid, name, dateTime, isActive, wirelessSocketUuid, wirelessSocketAction, wirelessSwitchUuid, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newWirelessTimer);
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