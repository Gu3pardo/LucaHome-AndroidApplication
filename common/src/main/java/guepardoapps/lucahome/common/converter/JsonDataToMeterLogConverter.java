package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.MeterLogItem;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToMeterLogConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToMeterLogConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToMeterLogConverter Singleton = new JsonDataToMeterLogConverter();

    public static JsonDataToMeterLogConverter getInstance() {
        return Singleton;
    }

    private JsonDataToMeterLogConverter() {
    }

    @Override
    public ArrayList<MeterLogItem> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<MeterLogItem> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private ArrayList<MeterLogItem> parseStringToList(@NonNull String jsonString) {
        if (!jsonString.contains("Error")) {
            ArrayList<MeterLogItem> list = new ArrayList<>();

            try {

                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("MeterLog");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));
                    UUID roomUuid = UUID.fromString(child.getString("RoomUuid"));

                    UUID typeUuid = UUID.fromString(child.getString("TypeUuid"));
                    String type = child.getString("Type");

                    JSONObject jsonDate = child.getJSONObject("Date");

                    int day = jsonDate.getInt("Day");
                    int month = jsonDate.getInt("Month");
                    int year = jsonDate.getInt("Year");

                    JSONObject jsonTime = child.getJSONObject("Time");

                    int hour = jsonTime.getInt("Hour");
                    int minute = jsonTime.getInt("Minute");

                    Calendar saveDateTime = Calendar.getInstance();
                    saveDateTime.set(year, month, day, hour, minute);

                    String meterId = child.getString("MeterId");

                    double value = child.getDouble("Value");
                    String imageName = child.getString("ImageName");

                    MeterLogItem newMeterLogItem = new MeterLogItem(uuid, roomUuid, typeUuid, type, saveDateTime, meterId, value, imageName, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newMeterLogItem);
                }
            } catch (JSONException jsonException) {
                Logger.getInstance().Error(Tag, jsonException.getMessage());
            }

            return list;
        }

        Logger.getInstance().Error(Tag, jsonString + " has an error!");
        return new ArrayList<>();
    }
}