package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.MediaServer;
import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.mediaserver.BundledData;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public class JsonDataToMediaServerConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToMediaServerConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToMediaServerConverter Singleton = new JsonDataToMediaServerConverter();

    public static JsonDataToMediaServerConverter getInstance() {
        return Singleton;
    }

    private JsonDataToMediaServerConverter() {
    }

    @Override
    public ArrayList<MediaServer> GetList(@NonNull String[] jsonStringArray) {
        if (StringHelper.StringsAreEqual(jsonStringArray)) {
            return parseStringToList(jsonStringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(jsonStringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<MediaServer> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private ArrayList<MediaServer> parseStringToList(@NonNull String jsonString) {
        if (!jsonString.contains("Error")) {
            ArrayList<MediaServer> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("MediaServer");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));
                    UUID roomUuid = UUID.fromString(child.getString("RoomUuid"));

                    String ip = child.getString("Ip");
                    boolean isSleepingServer = child.getString("IsSleepingServer").contains("1");

                    UUID wirelessSocketUuid = UUID.fromString(child.getString("WirelessSocketUuid"));

                    MediaServer newMediaServer = new MediaServer(uuid, roomUuid, ip, isSleepingServer, wirelessSocketUuid, false, new BundledData(), true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newMediaServer);
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