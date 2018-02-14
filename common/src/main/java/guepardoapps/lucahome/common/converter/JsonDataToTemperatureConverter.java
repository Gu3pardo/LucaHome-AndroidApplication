package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.Temperature;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToTemperatureConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToTemperatureConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Temperature\":";

    private static final JsonDataToTemperatureConverter Singleton = new JsonDataToTemperatureConverter();

    public static JsonDataToTemperatureConverter getInstance() {
        return Singleton;
    }

    private JsonDataToTemperatureConverter() {
    }

    @Override
    public ArrayList<Temperature> GetList(@NonNull String[] jsonStringArray) {
        if (StringHelper.StringsAreEqual(jsonStringArray)) {
            return parseStringToList(jsonStringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(jsonStringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<Temperature> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private ArrayList<Temperature> parseStringToList(@NonNull String jsonString) {
        if (!jsonString.contains("Error")) {
            ArrayList<Temperature> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONObject child = jsonObject.getJSONObject("Temperature");

                UUID uuid = UUID.fromString(child.getString("Uuid"));
                UUID roomUuid = UUID.fromString(child.getString("RoomUuid"));

                double temperatureValue = child.getDouble("Value");

                String sensorPath = child.getString("SensorPath");
                String graphPath = child.getString("GraphPath");

                Temperature temperature = new Temperature(uuid, roomUuid, temperatureValue, Calendar.getInstance(), sensorPath, Temperature.TemperatureType.Raspberry, graphPath);
                list.add(temperature);

            } catch (JSONException jsonException) {
                Logger.getInstance().Error(Tag, jsonException.getMessage());
            }
            return list;
        }

        Logger.getInstance().Error(Tag, jsonString + " has an error!");
        return new ArrayList<>();
    }
}