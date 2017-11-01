package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializablePair;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public class JsonDataToCoinConversionConverter implements IJsonDataConverter {
    private static final int KEY_INDEX = 0;
    private static final int VALUE_INDEX = 2;

    private static final String STRING_SPLIT_REGEX = "\\},";
    private static final String DATA_SPLIT_REGEX = ":";

    private static final String[] REPLACE_VALUES = {"},", "{", "}}"};
    private static final String REPLACEMENT = "";

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

    private static SerializableList<SerializablePair<String, Double>> parseStringToList(@NonNull String jsonValue) {
        SerializableList<SerializablePair<String, Double>> list = new SerializableList<>();

        if (jsonValue.length() == 0) {
            return list;
        }

        String[] entries = jsonValue.split(STRING_SPLIT_REGEX);

        for (String entry : entries) {
            String replacedEntry = entry;

            for (String replaceValue : REPLACE_VALUES) {
                replacedEntry = replacedEntry.replace(replaceValue, REPLACEMENT);
            }

            String[] data = replacedEntry.split(DATA_SPLIT_REGEX);

            if (data.length != 3) {
                break;
            }

            String key = data[KEY_INDEX].replace("\"", REPLACEMENT);

            String valueString = data[VALUE_INDEX];
            double value = Double.parseDouble(valueString);

            SerializablePair<String, Double> newValue = new SerializablePair<>(key, value);
            list.addValue(newValue);
        }

        return list;
    }
}
