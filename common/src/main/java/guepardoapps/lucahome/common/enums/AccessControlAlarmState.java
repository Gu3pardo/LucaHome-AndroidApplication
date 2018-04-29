package guepardoapps.lucahome.common.enums;

import android.support.annotation.NonNull;

import java.io.Serializable;

@SuppressWarnings({"unused"})
public enum AccessControlAlarmState implements Serializable {

    Null(0, "Null"),
    AccessControlActive(1, "AccessControlActive"),
    RequestCode(2, "RequestCode"),
    AccessSuccessful(3, "AccessSuccessful"),
    AccessFailed(4, "AccessFailed"),
    AlarmActive(5, "AlarmActive");

    private int _id;
    private String _action;

    AccessControlAlarmState(int id, @NonNull String action) {
        _id = id;
        _action = action;
    }

    public int GetId() {
        return _id;
    }

    @Override
    public String toString() {
        return _action;
    }

    public static AccessControlAlarmState GetById(int id) {
        for (AccessControlAlarmState e : values()) {
            if (e._id == id) {
                return e;
            }
        }

        return Null;
    }

    public static AccessControlAlarmState GetByString(@NonNull String action) {
        for (AccessControlAlarmState e : values()) {
            if (e._action.contains(action)) {
                return e;
            }
        }

        return Null;
    }
}
