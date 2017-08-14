package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.enums.LucaServerAction;

public class LucaBirthday implements Serializable {
    private static final long serialVersionUID = 8796770534492842492L;
    private static final String TAG = LucaBirthday.class.getSimpleName();

    public enum BirthdayType {PREVIOUS, TODAY, UPCOMING}

    private int _id;
    private String _name;
    private SerializableDate _date;
    private int _notificationId;

    public LucaBirthday(
            int id,
            @NonNull String name,
            @NonNull SerializableDate date) {
        _id = id;
        _name = name;
        _date = date;
        _notificationId = _id * _date.DayOfMonth() + _date.Month() * _date.Year();
    }

    public int GetId() {
        return _id;
    }

    public String GetName() {
        return _name;
    }

    public void SetName(@NonNull String name) {
        _name = name;
    }

    public SerializableDate GetDate() {
        return _date;
    }

    public void SetDate(@NonNull SerializableDate date) {
        _date = date;
    }

    public int GetNotificationId() {
        return _notificationId;
    }

    public String GetNotificationBody() {
        return "It is " + _name + "'s " + String.valueOf(GetAge()) + "th birthday!";
    }

    public int GetIcon() {
        return R.drawable.birthday;
    }

    public int GetAge() {
        int age;
        SerializableDate today = new SerializableDate();

        if (today.Month() > _date.Month()) {
            age = today.Year() - _date.Year();
        } else if (today.Month() == _date.Month()) {
            if (today.DayOfMonth() >= _date.DayOfMonth()) {
                age = today.Year() - _date.Year();
            } else {
                age = today.Year() - _date.Year() - 1;
            }
        } else {
            age = today.Year() - _date.Year() - 1;
        }

        return age;
    }

    public boolean HasBirthday() {
        SerializableDate today = new SerializableDate();
        if (today.DayOfMonth() == _date.DayOfMonth() && today.Month() == _date.Month()) {
            return true;
        }
        return false;
    }

    public BirthdayType CurrentBirthdayType() {
        if (HasBirthday()) {
            return BirthdayType.TODAY;
        } else {
            SerializableDate today = new SerializableDate();
            if (today.Month() > _date.Month() || (today.Month() == _date.Month() && today.DayOfMonth() > _date.DayOfMonth())) {
                return BirthdayType.PREVIOUS;
            } else {
                return BirthdayType.UPCOMING;
            }
        }
    }

    public String CommandAdd() {
        return String.format(Locale.getDefault(), "%s%d&name=%s&day=%d&month=%d&year=%d", LucaServerAction.ADD_BIRTHDAY.toString(), _id, _name, _date.DayOfMonth(), _date.Month(), _date.Year());
    }

    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%d&name=%s&day=%d&month=%d&year=%d", LucaServerAction.UPDATE_BIRTHDAY.toString(), _id, _name, _date.DayOfMonth(), _date.Month(), _date.Year());
    }

    public String CommandDelete() {
        return String.format(Locale.getDefault(), "%s%d", LucaServerAction.DELETE_BIRTHDAY.toString(), _id);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Name: %s );(Birthday: %s );(Age: %d );(HasBirthday: %s ))", TAG, _name, _date, GetAge(), HasBirthday());
    }
}
