package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.MapContent;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.MapContentHelper;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToMapContentConverter {
    private static final String Tag = JsonDataToMapContentConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToMapContentConverter Singleton = new JsonDataToMapContentConverter();

    public static JsonDataToMapContentConverter getInstance() {
        return Singleton;
    }

    private JsonDataToMapContentConverter() {
    }

    public ArrayList<MapContent> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    public ArrayList<MapContent> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private ArrayList<MapContent> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            ArrayList<MapContent> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("MapContent");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));

                    UUID typeUuid = UUID.fromString(child.getString("TypeUuid"));
                    String typeString = child.getString("Type");
                    MapContent.DrawingType drawingType = MapContentHelper.GetDrawingType(typeString);

                    String name = child.getString("Name");
                    String shortName = child.getString("ShortName");

                    boolean visibility = child.getString("Visibility").contains("1");

                    JSONObject positionJson = child.getJSONObject("Position");
                    JSONObject pointJson = positionJson.getJSONObject("Point");

                    int positionX = pointJson.getInt("X");
                    int positionY = pointJson.getInt("Y");

                    int[] position = new int[]{positionX, positionY};

                    MapContent newMapContent = new MapContent(uuid, typeUuid, drawingType, position, name, shortName, visibility, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newMapContent);
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