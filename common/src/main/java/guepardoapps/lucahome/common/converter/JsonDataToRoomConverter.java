package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.Room;
import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public class JsonDataToRoomConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToRoomConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToRoomConverter Singleton = new JsonDataToRoomConverter();

    public static JsonDataToRoomConverter getInstance() {
        return Singleton;
    }

    private JsonDataToRoomConverter() {
    }

    @Override
    public ArrayList<Room> GetList(@NonNull String[] jsonStringArray) {
        if (StringHelper.StringsAreEqual(jsonStringArray)) {
            return parseStringToList(jsonStringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(jsonStringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<Room> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private ArrayList<Room> parseStringToList(@NonNull String jsonString) {
        if (!jsonString.contains("Error")) {
            ArrayList<Room> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("Room");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));

                    String name = child.getString("Name");
                    int roomTypeInteger = child.getInt("RoomType");

                    Room.RoomType roomType = Room.RoomType.values()[roomTypeInteger];

                    Room newRoom = new Room(uuid, name, roomType, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newRoom);
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