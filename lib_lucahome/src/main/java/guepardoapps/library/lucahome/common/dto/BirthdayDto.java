package guepardoapps.library.lucahome.common.dto;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.IDs;
import guepardoapps.library.lucahome.common.constants.ServerActions;
import guepardoapps.library.lucahome.common.enums.BirthdayDateType;

public class BirthdayDto implements Serializable {

    private static final long serialVersionUID = -5998688684442075214L;

    private static final String TAG = BirthdayDto.class.getSimpleName();

    private int _drawable;

    private String _name;
    private Calendar _birthday;

    private int _id;
    private int _notificationId;
    private boolean _notifyMe;

    private String _deleteBroadcastReceiverString;
    private String _hasBirthdayBroadcastReceiverString;

    public BirthdayDto(String name, Calendar birthday, int id) {
        _drawable = -1;

        _name = name;
        _birthday = birthday;

        _id = id;
        _notificationId = IDs.NOTIFICATION_BIRTHDAY + _id;
        _notifyMe = true;

        _deleteBroadcastReceiverString = Broadcasts.DELETE_BIRTHDAY + _name.toUpperCase(Locale.GERMAN).trim();
        _hasBirthdayBroadcastReceiverString = Broadcasts.HAS_BIRTHDAY + _name.toUpperCase(Locale.GERMAN).trim();
    }

    public BirthdayDto(int drawable, String name, Calendar birthday) {
        _drawable = drawable;

        _name = name;
        _birthday = birthday;

        _id = -1;
        _notificationId = IDs.NOTIFICATION_BIRTHDAY + _id;
        _notifyMe = true;

        _deleteBroadcastReceiverString = Broadcasts.DELETE_BIRTHDAY + _name.toUpperCase(Locale.GERMAN).trim();
        _hasBirthdayBroadcastReceiverString = Broadcasts.HAS_BIRTHDAY + _name.toUpperCase(Locale.GERMAN).trim();
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

    public String GetBirthdayWearString() {
        String day = String.valueOf(_birthday.get(Calendar.DAY_OF_MONTH));
        while (day.length() < 2) {
            day = "0" + day;
        }

        String month;
        switch (_birthday.get(Calendar.MONTH)) {
            case Calendar.JANUARY:
                month = "January";
                break;
            case Calendar.FEBRUARY:
                month = "February";
                break;
            case Calendar.MARCH:
                month = "March";
                break;
            case Calendar.APRIL:
                month = "April";
                break;
            case Calendar.MAY:
                month = "May";
                break;
            case Calendar.JUNE:
                month = "June";
                break;
            case Calendar.JULY:
                month = "July";
                break;
            case Calendar.AUGUST:
                month = "August";
                break;
            case Calendar.SEPTEMBER:
                month = "September";
                break;
            case Calendar.OCTOBER:
                month = "October";
                break;
            case Calendar.NOVEMBER:
                month = "November";
                break;
            case Calendar.DECEMBER:
                month = "December";
                break;
            default:
                month = "NULL";
                break;
        }

        return day + "." + month + " " + String.valueOf(_birthday.get(Calendar.YEAR));
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

    public boolean GetNotifyMe() {
        return _notifyMe;
    }

    public String GetDeleteBroadcast() {
        return _deleteBroadcastReceiverString;
    }

    public String GetHasBirthdayBroadcast() {
        return _hasBirthdayBroadcastReceiverString;
    }

    public String GetCommandAdd() {
        return ServerActions.ADD_BIRTHDAY + String.valueOf(_id) + "&name=" + _name + "&day="
                + String.valueOf(_birthday.get(Calendar.DAY_OF_MONTH)) + "&month="
                + String.valueOf(_birthday.get(Calendar.MONTH) + 1) + "&year="
                + String.valueOf(_birthday.get(Calendar.YEAR));
    }

    public String GetCommandUpdate() {
        return ServerActions.UPDATE_BIRTHDAY + String.valueOf(_id) + "&name=" + _name + "&day="
                + String.valueOf(_birthday.get(Calendar.DAY_OF_MONTH)) + "&month="
                + String.valueOf(_birthday.get(Calendar.MONTH) + 1) + "&year="
                + String.valueOf(_birthday.get(Calendar.YEAR));
    }

    public String GetCommandDelete() {
        return ServerActions.DELETE_BIRTHDAY + String.valueOf(_id);
    }

    public String toString() {
        return "{" + TAG + ": {Name: " + _name + "};{Birthday: " + _birthday.toString() + "};{NotifyMe: "
                + String.valueOf(_notifyMe) + "};{Id: " + String.valueOf(_id) + "};{NotificationId: "
                + String.valueOf(_notificationId) + "};{DeleteBroadcastReceiverString: "
                + _deleteBroadcastReceiverString + "};{HasBirthdayBroadcastReceiverString: "
                + _hasBirthdayBroadcastReceiverString + "}}";
    }
}
