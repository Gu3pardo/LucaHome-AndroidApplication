package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.Coin;
import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToCoinConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToCoinConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToCoinConverter Singleton = new JsonDataToCoinConverter();

    public static JsonDataToCoinConverter getInstance() {
        return Singleton;
    }

    private JsonDataToCoinConverter() {
    }

    @Override
    public ArrayList<Coin> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<Coin> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private ArrayList<Coin> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            ArrayList<Coin> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("Coin");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));

                    String user = child.getString("User");
                    String type = child.getString("Type");

                    int amount = child.getInt("Amount");

                    double currentConversion = 1;

                    Coin newCoin = new Coin(uuid, user, type, amount, currentConversion, Coin.Trend.Null, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newCoin);
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