package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializablePair;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.Coin;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public final class JsonDataToCoinConverter {
    private static final String TAG = JsonDataToCoinConverter.class.getSimpleName();
    private Logger _logger;

    private static String _searchParameter = "{\"Data\":";

    public JsonDataToCoinConverter() {
        _logger = new Logger(TAG);
    }

    public SerializableList<Coin> GetList(@NonNull String[] stringArray, @NonNull SerializableList<SerializablePair<String, Double>> conversionList) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0], conversionList);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry, conversionList);
        }
    }

    public SerializableList<Coin> GetList(@NonNull String responseString, @NonNull SerializableList<SerializablePair<String, Double>> conversionList) {
        return parseStringToList(responseString, conversionList);
    }

    private SerializableList<Coin> parseStringToList(@NonNull String value, @NonNull SerializableList<SerializablePair<String, Double>> conversionList) {
        try {
            if (!value.contains("Error")) {
                SerializableList<Coin> list = new SerializableList<>();

                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("Coin");

                    int id = child.getInt("ID");

                    String user = child.getString("User");
                    String type = child.getString("Type");

                    int amount = child.getInt("Amount");

                    double currentConversion = getCurrentConversion(type, conversionList);

                    int icon = getIcon(type);

                    Coin newCoin = new Coin(id, user, type, amount, currentConversion, Coin.Trend.NULL, icon, true, ILucaClass.LucaServerDbAction.Null);
                    list.addValue(newCoin);
                }

                return list;
            }
        } catch (JSONException jsonException) {
            _logger.Error(jsonException.getMessage());
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }

    private double getCurrentConversion(@NonNull String type, @NonNull SerializableList<SerializablePair<String, Double>> conversionList) {
        for (int index = 0; index < conversionList.getSize(); index++) {
            SerializablePair<String, Double> entry = conversionList.getValue(index);
            if (entry.GetKey().contains(type)) {
                return entry.GetValue();
            }
        }
        return 0;
    }

    private int getIcon(@NonNull String type) {
        switch (type) {
            case "BCH":
                return R.drawable.bch;
            case "BTC":
                return R.drawable.btc;
            case "DASH":
                return R.drawable.dash;
            case "ETC":
                return R.drawable.etc;
            case "ETH":
                return R.drawable.eth;
            case "IOTA":
                return R.drawable.iota;
            case "LTC":
                return R.drawable.ltc;
            case "XMR":
                return R.drawable.xmr;
            case "XRP":
                return R.drawable.xrp;
            case "ZEC":
                return R.drawable.zec;
            default:
                return R.drawable.btc;
        }
    }
}