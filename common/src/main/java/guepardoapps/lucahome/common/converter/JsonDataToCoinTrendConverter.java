package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.lucahome.common.classes.Coin;

public class JsonDataToCoinTrendConverter {
    private static final String TAG = JsonDataToCoinTrendConverter.class.getSimpleName();

    public static Coin UpdateTrend(@NonNull Coin coin, @NonNull String responseString, @NonNull String currency) throws JSONException {
        if (responseString.length() <= 2) {
            return coin;
        }

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
        }

        return coin;
    }
}
