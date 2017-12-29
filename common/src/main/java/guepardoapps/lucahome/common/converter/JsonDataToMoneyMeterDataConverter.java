package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.MoneyMeterData;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToMoneyMeterDataConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToMoneyMeterDataConverter.class.getSimpleName();
    private static final String SEARCH_PARAMETER = "{\"Data\":";

    private static final JsonDataToMoneyMeterDataConverter SINGLETON = new JsonDataToMoneyMeterDataConverter();

    public static JsonDataToMoneyMeterDataConverter getInstance() {
        return SINGLETON;
    }

    private JsonDataToMoneyMeterDataConverter() {
    }

    @Override
    public SerializableList<MoneyMeterData> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SEARCH_PARAMETER);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<MoneyMeterData> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private SerializableList<MoneyMeterData> parseStringToList(@NonNull String jsonString) {
        if (!jsonString.contains("Error")) {
            SerializableList<MoneyMeterData> list = new SerializableList<>();

            try {

                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("MoneyMeterData");

                    int id = child.getInt("Id");
                    int typeId = child.getInt("TypeId");

                    String bank = child.getString("Bank");
                    String plan = child.getString("Plan");
                    double amount = child.getDouble("Amount");
                    String unit = child.getString("Unit");

                    JSONObject jsonDate = child.getJSONObject("Date");

                    int day = jsonDate.getInt("Day");
                    int month = jsonDate.getInt("Month");
                    int year = jsonDate.getInt("Year");

                    String user = child.getString("User");

                    MoneyMeterData newMoneyMeterData = new MoneyMeterData(id, typeId, bank, plan, amount, unit, new SerializableDate(year, month, day), user);
                    list.addValue(newMoneyMeterData);
                }
            } catch (JSONException jsonException) {
                Logger.getInstance().Error(TAG, jsonException.getMessage());
            }

            return list;
        }

        Logger.getInstance().Error(TAG, jsonString + " has an error!");
        return new SerializableList<>();
    }
}