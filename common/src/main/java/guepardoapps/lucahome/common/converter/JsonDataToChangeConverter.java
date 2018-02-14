package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.Change;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToChangeConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToChangeConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToChangeConverter Singleton = new JsonDataToChangeConverter();

    public static JsonDataToChangeConverter getInstance() {
        return Singleton;
    }

    private JsonDataToChangeConverter() {
    }

    @Override
    public ArrayList<Change> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<Change> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private ArrayList<Change> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            ArrayList<Change> list = new ArrayList<>();

            try {

                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("Change");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));

                    String type = child.getString("Type");
                    String user = child.getString("UserName");

                    JSONObject jsonDate = child.getJSONObject("Date");

                    int day = jsonDate.getInt("Day");
                    int month = jsonDate.getInt("Month");
                    int year = jsonDate.getInt("Year");

                    JSONObject jsonTime = child.getJSONObject("Time");

                    int hour = jsonTime.getInt("Hour");
                    int minute = jsonTime.getInt("Minute");

                    Calendar dateTime = Calendar.getInstance();
                    dateTime.set(year, month, day, hour, minute);

                    Change newChange = new Change(uuid, type, dateTime, user);
                    list.add(newChange);
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