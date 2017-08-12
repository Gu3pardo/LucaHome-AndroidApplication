package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializablePair;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public class JsonDataToCoinConversionConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToCoinConversionConverter.class.getSimpleName();
    private Logger _logger;

    public JsonDataToCoinConversionConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<?> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, "");
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<SerializablePair<String, Double>> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private SerializableList<SerializablePair<String, Double>> parseStringToList(@NonNull String jsonValue) {
        SerializableList<SerializablePair<String, Double>> list = new SerializableList<>();

        String[] entries = jsonValue.split("\\},");
        for (int index = 0; index < entries.length; index++) {
            String entry = entries[index];
            String replacedEntry = entry.replace("},", "").replace("{", "").replace("}}", "");
            String[] data = replacedEntry.split(":");

            String key = data[0].replace("\"", "");
            String valueString = data[2];
            double value = Double.parseDouble(valueString);

            SerializablePair<String, Double> newValue = new SerializablePair<>(key, value);
            list.addValue(newValue);
        }

        return list;
    }
}
