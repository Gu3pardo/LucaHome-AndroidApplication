package guepardoapps.lucahome.common.converter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.Birthday;
import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.utils.BitmapHelper;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public class JsonDataToBirthdayConverter implements IJsonContextDataConverter {
    private static final String Tag = JsonDataToBirthdayConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToBirthdayConverter Singleton = new JsonDataToBirthdayConverter();

    public static JsonDataToBirthdayConverter getInstance() {
        return Singleton;
    }

    private JsonDataToBirthdayConverter() {
    }

    @Override
    public ArrayList<Birthday> GetList(@NonNull String[] jsonStringArray, @NonNull Context context) {
        if (StringHelper.StringsAreEqual(jsonStringArray)) {
            return parseStringToList(jsonStringArray[0], context);
        } else {
            String usedEntry = StringHelper.SelectString(jsonStringArray, SearchParameter);
            return parseStringToList(usedEntry, context);
        }
    }

    @Override
    public ArrayList<Birthday> GetList(@NonNull String responseString, @NonNull Context context) {
        return parseStringToList(responseString, context);
    }

    private ArrayList<Birthday> parseStringToList(@NonNull String jsonString, @NonNull Context context) {
        if (!jsonString.contains("Error")) {
            ArrayList<Birthday> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("Birthday");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));

                    String name = child.getString("Name");
                    String group = child.getString("Group");

                    boolean remindMe = child.getString("RemindMe").contains("1");
                    boolean sentMail = child.getString("SentMail").contains("1");

                    JSONObject birthdayJsonDate = child.getJSONObject("Date");

                    int day = birthdayJsonDate.getInt("Day");
                    int month = birthdayJsonDate.getInt("Month");
                    int year = birthdayJsonDate.getInt("Year");

                    Calendar birthday = Calendar.getInstance();
                    birthday.set(year, month, day);

                    Bitmap photo = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_face_white_48dp);
                    try {
                        photo = BitmapHelper.RetrieveContactPhoto(context, name, 250, 250, true);
                        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Retrieved photo is %s", photo));
                    } catch (Exception exception) {
                        Logger.getInstance().Error(Tag, String.format(Locale.getDefault(), "Exception in RetrieveContactPhoto: %s", exception.getMessage()));
                    }

                    Birthday newBirthday = new Birthday(uuid, name, birthday, group, remindMe, sentMail, photo, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newBirthday);
                }
            } catch (JSONException jsonException) {
                Logger.getInstance().Error(Tag, jsonException.getMessage());
            }

            return list;
        }

        Logger.getInstance().Error(Tag, jsonString + " has an error!");
        return new ArrayList<>();
    }
}