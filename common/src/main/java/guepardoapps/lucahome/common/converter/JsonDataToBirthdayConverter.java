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
import guepardoapps.lucahome.common.interfaces.converter.IJsonContextDataConverter;

public class JsonDataToBirthdayConverter implements IJsonContextDataConverter {
    private static final String TAG = JsonDataToBirthdayConverter.class.getSimpleName();
    private static final String SEARCH_PARAMETER = "{\"Data\":";

    private static final JsonDataToBirthdayConverter SINGLETON = new JsonDataToBirthdayConverter();

    public static JsonDataToBirthdayConverter getInstance() {
        return SINGLETON;
    }

    private JsonDataToBirthdayConverter() {
    }

    public SerializableList<LucaBirthday> GetList(@NonNull String[] stringArray, @NonNull Context context) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0], context);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SEARCH_PARAMETER);
            return parseStringToList(usedEntry, context);
        }
    }

    public SerializableList<LucaBirthday> GetList(@NonNull String responseString, @NonNull Context context) {
        return parseStringToList(responseString, context);
    }

    private SerializableList<LucaBirthday> parseStringToList(@NonNull String value, @NonNull Context context) {
        if (!value.contains("Error")) {
            SerializableList<LucaBirthday> list = new SerializableList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("Birthday");

                    int id = child.getInt("Id");

                    String name = child.getString("Name");
                    String group = child.getString("Group");

                    boolean remindMe = child.getString("RemindMe").contains("1");
                    boolean sentMail = child.getString("SentMail").contains("1");

                    JSONObject birthdayJsonDate = child.getJSONObject("Date");

                    int day = birthdayJsonDate.getInt("Day");
                    int month = birthdayJsonDate.getInt("Month");
                    int year = birthdayJsonDate.getInt("Year");

                    Bitmap photo = BitmapFactory.decodeResource(context.getResources(), guepardoapps.lucahome.basic.R.mipmap.ic_face_white_48dp);
                    try {
                        photo = Tools.RetrieveContactPhoto(context, name, 250, 250, true);
                        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "Retrieved photo is %s", photo));
                    } catch (Exception exception) {
                        Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Exception in RetrieveContactPhoto: %s", exception.getMessage()));
                    }

                    LucaBirthday newBirthday = new LucaBirthday(id, name, new SerializableDate(year, month, day), group, remindMe, sentMail, photo, true, ILucaClass.LucaServerDbAction.Null);
                    list.addValue(newBirthday);
                }
            } catch (JSONException jsonException) {
                Logger.getInstance().Error(TAG, jsonException.getMessage());
            }

            return list;
        }

        Logger.getInstance().Error(TAG, value + " has an error!");
        return new SerializableList<>();
    }
}