package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.MoneyLogItem;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToMoneyLogConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToMoneyLogConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToMoneyLogConverter Singleton = new JsonDataToMoneyLogConverter();

    public static JsonDataToMoneyLogConverter getInstance() {
        return Singleton;
    }

    private JsonDataToMoneyLogConverter() {
    }

    @Override
    public ArrayList<MoneyLogItem> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<MoneyLogItem> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private ArrayList<MoneyLogItem> parseStringToList(@NonNull String jsonString) {
        if (!jsonString.contains("Error")) {
            ArrayList<MoneyLogItem> list = new ArrayList<>();

            try {

                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("MoneyLog");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));
                    UUID typeUuid = UUID.fromString(child.getString("TypeUuid"));

                    String bank = child.getString("Bank");
                    String plan = child.getString("Plan");
                    double amount = child.getDouble("Amount");
                    String unit = child.getString("Unit");

                    JSONObject jsonDate = child.getJSONObject("Date");

                    int day = jsonDate.getInt("Day");
                    int month = jsonDate.getInt("Month");
                    int year = jsonDate.getInt("Year");

                    Calendar saveDate = Calendar.getInstance();
                    saveDate.set(year, month, day);

                    String user = child.getString("User");

                    MoneyLogItem newMoneyLogItem = new MoneyLogItem(uuid, typeUuid, bank, plan, amount, unit, saveDate, user, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newMoneyLogItem);
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