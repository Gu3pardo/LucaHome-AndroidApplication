package guepardoapps.lucahome.common.classes;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Birthday implements ILucaClass {
    private static final String Tag = Birthday.class.getSimpleName();

    public enum BirthdayType {Previous, Today, Upcoming}

    private UUID _uuid;
    private String _name;
    private Calendar _birthday;
    private String _group;

    private boolean _remindMe;
    private boolean _sentMail;

    private transient Bitmap _photo;
    private int _notificationId;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public Birthday(
            @NonNull UUID uuid,
            @NonNull String name,
            @NonNull Calendar birthday,
            @NonNull String group,
            boolean remindMe,
            boolean sentMail,
            Bitmap photo,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _name = name;
        _birthday = birthday;
        _group = group;
        _remindMe = remindMe;
        _sentMail = sentMail;
        _photo = photo;
        _notificationId = _birthday.get(Calendar.DAY_OF_MONTH) + _birthday.get(Calendar.MONTH) * _birthday.get(Calendar.YEAR);
        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetRoomUuid not implemented for " + Tag);
    }

    public void SetName(@NonNull String name) {
        _name = name;
    }

    public String GetName() {
        return _name;
    }

    public void SetBirthday(@NonNull Calendar birthday) {
        _birthday = birthday;
    }

    public Calendar GetBirthday() {
        return _birthday;
    }

    public void SetGroup(@NonNull String group) {
        _group = group;
    }

    public String GetGroup() {
        return _group;
    }

    public void SetRemindMe(boolean remindMe) {
        _remindMe = remindMe;
    }

    public boolean GetRemindMe() {
        return _remindMe;
    }

    public void SetSentMail(boolean sentMail) {
        _sentMail = sentMail;
    }

    public boolean GetSentMail() {
        return _sentMail;
    }

    public void SetPhoto(@NonNull Bitmap photo) {
        _photo = photo;
    }

    public Bitmap GetPhoto() {
        return _photo;
    }

    public int GetNotificationId() {
        return _notificationId;
    }

    public String GetNotificationBody() {
        return "It is " + _name + "'s " + String.valueOf(GetAge()) + "th birthday!";
    }

    public int GetAge() {
        Calendar todayCalendar = Calendar.getInstance();
        int todayYear = todayCalendar.get(Calendar.YEAR);
        int todayMonth = todayCalendar.get(Calendar.MONTH);
        int todayDayOfMonth = todayCalendar.get(Calendar.DAY_OF_MONTH);

        int birthdayYear = _birthday.get(Calendar.YEAR);
        int birthdayMonth = _birthday.get(Calendar.MONTH);
        int birthdayDayOfMonth = _birthday.get(Calendar.DAY_OF_MONTH);

        if (todayMonth > birthdayMonth || (todayMonth == birthdayMonth && todayDayOfMonth >= birthdayDayOfMonth)) {
            return todayYear - birthdayYear;
        }

        return todayYear - birthdayYear - 1;
    }

    public boolean HasBirthday() {
        Calendar todayCalendar = Calendar.getInstance();
        int todayMonth = todayCalendar.get(Calendar.MONTH);
        int todayDayOfMonth = todayCalendar.get(Calendar.DAY_OF_MONTH);

        int birthdayMonth = _birthday.get(Calendar.MONTH);
        int birthdayDayOfMonth = _birthday.get(Calendar.DAY_OF_MONTH);

        return todayMonth == birthdayDayOfMonth && todayDayOfMonth == birthdayMonth;
    }

    public BirthdayType GetCurrentBirthdayType() {
        if (HasBirthday()) {
            return BirthdayType.Today;
        }

        Calendar todayCalendar = Calendar.getInstance();
        int todayMonth = todayCalendar.get(Calendar.MONTH);
        int todayDayOfMonth = todayCalendar.get(Calendar.DAY_OF_MONTH);

        int birthdayMonth = _birthday.get(Calendar.MONTH);
        int birthdayDayOfMonth = _birthday.get(Calendar.DAY_OF_MONTH);

        if (todayMonth > birthdayMonth || (todayMonth == birthdayMonth && todayDayOfMonth > birthdayDayOfMonth)) {
            return BirthdayType.Previous;
        }

        return BirthdayType.Upcoming;
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&name=%s&day=%d&month=%d&year=%d&group=%s&remindme=%s",
                LucaServerActionTypes.ADD_BIRTHDAY.toString(), _uuid, _name, _birthday.get(Calendar.DAY_OF_MONTH), _birthday.get(Calendar.MONTH), _birthday.get(Calendar.YEAR), _group, (_remindMe ? "1" : "0"));
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&name=%s&day=%d&month=%d&year=%d&group=%s&remindme=%s",
                LucaServerActionTypes.UPDATE_BIRTHDAY.toString(), _uuid, _name, _birthday.get(Calendar.DAY_OF_MONTH), _birthday.get(Calendar.MONTH), _birthday.get(Calendar.YEAR), _group, (_remindMe ? "1" : "0"));
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.DELETE_BIRTHDAY.toString(), _uuid);
    }

    @Override
    public void SetIsOnServer(boolean isOnServer) {
        _isOnServer = isOnServer;
    }

    @Override
    public boolean GetIsOnServer() {
        return _isOnServer;
    }

    @Override
    public void SetServerDbAction(@NonNull LucaServerDbAction serverDbAction) {
        _serverDbAction = serverDbAction;
    }

    @Override
    public LucaServerDbAction GetServerDbAction() {
        return _serverDbAction;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Name\":\"%s\",\"Birthday\":\"%s\",\"Age\":%d,\"Group\":\"%s\",\"HasBirthday\":\"%s\"}",
                Tag, _uuid, _name, _birthday, GetAge(), _group, (HasBirthday() ? "1" : "0"));
    }
}
