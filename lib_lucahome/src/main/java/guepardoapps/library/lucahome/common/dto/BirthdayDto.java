package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;

import guepardoapps.library.lucahome.common.constants.IDs;
import guepardoapps.library.lucahome.common.enums.BirthdayDateType;
import guepardoapps.library.lucahome.common.enums.LucaServerAction;

public class BirthdayDto implements Serializable {

    private static final long serialVersionUID = -5998688684442075214L;

    private static final String TAG = BirthdayDto.class.getSimpleName();

    private int _id;

    private String _name;
    private Calendar _birthday;

    private int _drawable;
    private int _notificationId;

    public BirthdayDto(
            int id,
            @NonNull String name,
            @NonNull Calendar birthday,
            int drawable) {
        _id = id;
        _notificationId = IDs.NOTIFICATION_BIRTHDAY + _id;

        _name = name;
        _birthday = birthday;

        _drawable = drawable;
    }

    public BirthdayDto(
            @NonNull String name,
            @NonNull Calendar birthday,
            int id) {
        this(id, name, birthday, -1);
    }

    public BirthdayDto(
            int drawable,
            @NonNull String name,
            @NonNull Calendar birthday) {
        this(-1, name, birthday, drawable);
    }

    public int GetDrawable() {
        return _drawable;
    }

    public String GetName() {
        return _name;
    }

    public Calendar GetBirthday() {
        return _birthday;
    }

    public boolean HasBirthday() {
        Calendar now = Calendar.getInstance();
        return (now.get(Calendar.DAY_OF_MONTH) == _birthday.get(Calendar.DAY_OF_MONTH)
                && now.get(Calendar.MONTH) == _birthday.get(Calendar.MONTH));
    }

    public int GetAge() {
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.MONTH) > _birthday.get(Calendar.MONTH)) {
            return now.get(Calendar.YEAR) - _birthday.get(Calendar.YEAR);
        } else if (now.get(Calendar.MONTH) == _birthday.get(Calendar.MONTH)) {
            if (now.get(Calendar.DAY_OF_MONTH) >= _birthday.get(Calendar.DAY_OF_MONTH)) {
                return now.get(Calendar.YEAR) - _birthday.get(Calendar.YEAR);
            } else {
                return now.get(Calendar.YEAR) - _birthday.get(Calendar.YEAR) - 1;
            }
        } else {
            return now.get(Calendar.YEAR) - _birthday.get(Calendar.YEAR) - 1;
        }
    }

    public BirthdayDateType GetDateType() {
        if (HasBirthday()) {
            return BirthdayDateType.TODAY;
        } else {
            Calendar today = Calendar.getInstance();
            if ((today.get(Calendar.MONTH) > _birthday.get(Calendar.MONTH))
                    || (today.get(Calendar.MONTH) == _birthday.get(Calendar.MONTH)
                    && today.get(Calendar.DAY_OF_MONTH) >= _birthday.get(Calendar.DAY_OF_MONTH))) {
                return BirthdayDateType.PREVIOUS;
            } else {
                return BirthdayDateType.UPCOMING;
            }
        }
    }

    public String GetBirthdayString() {
        String birthdayString = "";
        birthdayString += String.valueOf(_birthday.get(Calendar.DAY_OF_MONTH)) + "."
                + String.valueOf(_birthday.get(Calendar.MONTH) + 1) + "."
                + String.valueOf(_birthday.get(Calendar.YEAR));
        return birthdayString;
    }

    public int GetId() {
        return _id;
    }

    public int GetNotificationId() {
        return _notificationId;
    }

    public String GetNotificationBody(int age) {
        return "It is " + _name + "'s " + String.valueOf(age) + "th birthday!";
    }

    public String GetCommandAdd() {
        return LucaServerAction.ADD_BIRTHDAY.toString() + String.valueOf(_id)
                + "&name=" + _name
                + "&day=" + String.valueOf(_birthday.get(Calendar.DAY_OF_MONTH))
                + "&month=" + String.valueOf(_birthday.get(Calendar.MONTH) + 1)
                + "&year=" + String.valueOf(_birthday.get(Calendar.YEAR));
    }

    public String GetCommandUpdate() {
        return LucaServerAction.UPDATE_BIRTHDAY.toString() + String.valueOf(_id)
                + "&name=" + _name
                + "&day=" + String.valueOf(_birthday.get(Calendar.DAY_OF_MONTH))
                + "&month=" + String.valueOf(_birthday.get(Calendar.MONTH) + 1)
                + "&year=" + String.valueOf(_birthday.get(Calendar.YEAR));
    }

    public String GetCommandDelete() {
        return LucaServerAction.DELETE_BIRTHDAY.toString() + String.valueOf(_id);
    }

    public String toString() {
        return "{" + TAG + ": {Name: " + _name
                + "};{Birthday: " + _birthday.toString()
                + "};{Id: " + String.valueOf(_id)
                + "};{NotificationId: " + String.valueOf(_notificationId) + "}}";
    }
}
