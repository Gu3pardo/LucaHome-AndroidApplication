package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import guepardoapps.lucahome.common.classes.Coin;
import guepardoapps.lucahome.common.utils.Logger;

public class JsonDataToCoinDataConverter {
    private static final String Tag = JsonDataToCoinDataConverter.class.getSimpleName();

    private static final JsonDataToCoinDataConverter Singleton = new JsonDataToCoinDataConverter();

    public static JsonDataToCoinDataConverter getInstance() {
        return Singleton;
    }

    private JsonDataToCoinDataConverter() {
    }

    public static Coin UpdateData(@NonNull Coin coin, @NonNull String responseString, @NonNull String currency) {
        if (responseString.length() <= 2) {
            return coin;
        }

        try {
            JSONObject jsonObject = new JSONObject(responseString);

            JSONArray dataArray = jsonObject.getJSONArray("Data");
            int arraySize = dataArray.length();

            JSONObject firstEntry = dataArray.getJSONObject(0);
            double openValue = firstEntry.getDouble("open");

            JSONObject lastEntry = dataArray.getJSONObject(arraySize - 1);
            double closeValue = lastEntry.getDouble("close");

            double difference = closeValue - openValue;

            if (currency.contains("EUR")) {
                coin.SetCurrentTrend(difference > 0 ? Coin.Trend.Rise : Coin.Trend.Fall);
                coin.SetCurrentConversion(closeValue);
            }
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
        }

        return coin;
    }
}
