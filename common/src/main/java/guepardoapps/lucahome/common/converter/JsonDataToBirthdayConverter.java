package guepardoapps.lucahome.common.converter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaBirthday;

public class JsonDataToBirthdayConverter {
    private static final String TAG = JsonDataToBirthdayConverter.class.getSimpleName();
    private Logger _logger;

    private static String _searchParameter = "{birthday:";

    public JsonDataToBirthdayConverter() {
        _logger = new Logger(TAG);
    }

    public SerializableList<LucaBirthday> GetList(@NonNull String[] stringArray, @NonNull Context context) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0], context);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry, context);
        }
    }

    public SerializableList<LucaBirthday> GetList(@NonNull String responseString, @NonNull Context context) {
        return parseStringToList(responseString, context);
    }

    private SerializableList<LucaBirthday> parseStringToList(@NonNull String value, @NonNull Context context) {
        if (!value.contains("Error")) {
            if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
                if (value.contains(_searchParameter)) {
                    SerializableList<LucaBirthday> list = new SerializableList<>();

                    String[] entries = value.split("\\" + _searchParameter);
                    for (String entry : entries) {
                        entry = entry.replace(_searchParameter, "").replace("};};", "");

                        String[] data = entry.split("\\};");
                        LucaBirthday newValue = parseStringToValue(data, context);
                        if (newValue != null) {
                            list.addValue(newValue);
                        }
                    }

                    return list;
                }
            }
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }

    private LucaBirthday parseStringToValue(@NonNull String[] data, @NonNull Context context) {
        if (data.length == 5) {
            if (data[0].contains("{id:")
                    && data[1].contains("{name:")
                    && data[2].contains("{day:")
                    && data[3].contains("{month:")
                    && data[4].contains("{year:")) {

                String idString = data[0].replace("{id:", "").replace("};", "");
                int id = Integer.parseInt(idString);

                String name = data[1].replace("{name:", "").replace("};", "");

                String dayString = data[2].replace("{day:", "").replace("};", "");
                int day = Integer.parseInt(dayString);
                String monthString = data[3].replace("{month:", "").replace("};", "");
                int month = Integer.parseInt(monthString);
                String yearString = data[4].replace("{year:", "").replace("};", "");
                int year = Integer.parseInt(yearString);

                SerializableDate birthday = new SerializableDate(year, month, day);

                Bitmap photo = BitmapFactory.decodeResource(context.getResources(), guepardoapps.lucahome.basic.R.mipmap.ic_face_white_48dp);
                try {
                    photo = Tools.RetrieveContactPhoto(context, name, 250, 250, true);
                    _logger.Debug(String.format(Locale.getDefault(), "Retrieved photo is %s", photo));
                } catch (Exception exception) {
                    _logger.Error(String.format(Locale.getDefault(), "Exception in RetrieveContactPhoto: %s", exception.getMessage()));
                }

                LucaBirthday newValue = new LucaBirthday(id, name, birthday, photo);
                _logger.Debug(String.format(Locale.getDefault(), "New BirthdayDto %s", newValue));

                return newValue;
            }
        }

        _logger.Error("Data has an error!");

        return null;
    }
}