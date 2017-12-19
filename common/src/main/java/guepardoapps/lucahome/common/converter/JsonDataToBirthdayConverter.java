package guepardoapps.lucahome.common.converter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaBirthday;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class JsonDataToBirthdayConverter {
    private static final String TAG = JsonDataToBirthdayConverter.class.getSimpleName();
    private Logger _logger;

    private static String _searchParameter = "{\"Data\":";

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
        try {
            if (!value.contains("Error")) {
                SerializableList<LucaBirthday> list = new SerializableList<>();

                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("Birthday");

                    int id = child.getInt("ID");

                    String name = child.getString("Name");

                    boolean remindMe = child.getString("RemindMe").contains("1");
                    boolean sendMail = child.getString("SendMail").contains("1");

                    JSONObject birthdayJsonDate = child.getJSONObject("Date");

                    int day = birthdayJsonDate.getInt("Day");
                    int month = birthdayJsonDate.getInt("Month");
                    int year = birthdayJsonDate.getInt("Year");

                    Bitmap photo = BitmapFactory.decodeResource(context.getResources(), guepardoapps.lucahome.basic.R.mipmap.ic_face_white_48dp);
                    try {
                        photo = Tools.RetrieveContactPhoto(context, name, 250, 250, true);
                        _logger.Debug(String.format(Locale.getDefault(), "Retrieved photo is %s", photo));
                    } catch (Exception exception) {
                        _logger.Error(String.format(Locale.getDefault(), "Exception in RetrieveContactPhoto: %s", exception.getMessage()));
                    }

                    LucaBirthday newBirthday = new LucaBirthday(id, name, remindMe, sendMail, new SerializableDate(year, month, day), photo, true, ILucaClass.LucaServerDbAction.Null);
                    list.addValue(newBirthday);
                }

                return list;
            }
        } catch (JSONException jsonException) {
            _logger.Error(jsonException.getMessage());
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }
}