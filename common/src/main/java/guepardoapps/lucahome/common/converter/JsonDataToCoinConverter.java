package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializablePair;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.Coin;

public final class JsonDataToCoinConverter {
    private static final String TAG = JsonDataToCoinConverter.class.getSimpleName();
    private Logger _logger;

    private static String _searchParameter = "{coin:";

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
        if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
            if (value.contains(_searchParameter)) {
                SerializableList<Coin> list = new SerializableList<>();

                String[] entries = value.split("\\" + _searchParameter);
                for (String entry : entries) {
                    entry = entry.replace(_searchParameter, "").replace("};};", "");

                    String[] data = entry.split("\\};");
                    Coin newValue = parseStringToValue(data, conversionList);
                    if (newValue != null) {
                        list.addValue(newValue);
                    }
                }
                return list;
            }
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }

    private Coin parseStringToValue(@NonNull String[] data, @NonNull SerializableList<SerializablePair<String, Double>> conversionList) {
        if (data.length == 4) {
            if (data[0].contains("{Id:")
                    && data[1].contains("{User:")
                    && data[2].contains("{Type:")
                    && data[3].contains("{Amount:")) {

                String idString = data[0].replace("{Id:", "").replace("};", "");
                int id = Integer.parseInt(idString);

                String user = data[1].replace("{User:", "").replace("};", "");

                String type = data[2].replace("{Type:", "").replace("};", "");

                String amountString = data[3].replace("{Amount:", "").replace("};", "");
                double amount = Double.parseDouble(amountString);

                double currentConversion = 0;
                for (int index = 0; index < conversionList.getSize(); index++) {
                    SerializablePair<String, Double> entry = conversionList.getValue(index);
                    if (entry.GetKey().contains(type)) {
                        currentConversion = entry.GetValue();
                        break;
                    }
                }

                int icon;
                switch (type) {
                    case "BCH":
                        icon = R.drawable.bch;
                        break;
                    case "BTC":
                        icon = R.drawable.btc;
                        break;
                    case "DASH":
                        icon = R.drawable.dash;
                        break;
                    case "ETC":
                        icon = R.drawable.etc;
                        break;
                    case "ETH":
                        icon = R.drawable.eth;
                        break;
                    case "LTC":
                        icon = R.drawable.ltc;
                        break;
                    case "XMR":
                        icon = R.drawable.xmr;
                        break;
                    case "ZEC":
                        icon = R.drawable.zec;
                        break;
                    default:
                        icon = R.drawable.btc;
                }

                return new Coin(id, user, type, amount, currentConversion, icon);
            }
        }

        _logger.Error("Data has an error!");

        return null;
    }
}