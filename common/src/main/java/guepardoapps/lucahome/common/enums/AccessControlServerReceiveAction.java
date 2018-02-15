package guepardoapps.lucahome.common.enums;

import android.support.annotation.NonNull;

import java.io.Serializable;

public enum AccessControlServerReceiveAction implements Serializable {

    Null(0, "Null"),
    RequestCode(1, "RequestCode"),
    LoginFailed(2, "LoginFailed"),
    LoginSuccess(3, "LoginSuccess"),
    AlarmActive(4, "AlarmActive"),
    ActivateAccessControl(5, "ActivateAccessControl");

    private int _id;
    private String _action;

    AccessControlServerReceiveAction(int id, @NonNull String action) {
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

    public static AccessControlServerReceiveAction GetById(int id) {
        for (AccessControlServerReceiveAction e : values()) {
            if (e._id == id) {
                return e;
            }
        }

        return Null;
    }

    public static AccessControlServerReceiveAction GetByString(@NonNull String action) {
        for (AccessControlServerReceiveAction e : values()) {
            if (e._action.contains(action)) {
                return e;
            }
        }

        return Null;
    }
}
