package guepardoapps.lucahome.common.enums;

import android.support.annotation.NonNull;

import java.io.Serializable;

@SuppressWarnings({"unused"})
public enum SocketAction implements Serializable {

    NULL(-1, "", false, "0"),
    Deactivate(0, "Deactivate", false, "0"),
    Activate(1, "Activate", true, "1");

    private int _id;
    private String _action;
    private boolean _bool;
    private String _flag;

    SocketAction(int id, @NonNull String action, boolean bool, @NonNull String flag) {
        _id = id;
        _action = action;
        _bool = bool;
        _flag = flag;
    }

    @Override
    public String toString() {
        return _action;
    }

    public int GetId() {
        return _id;
    }

    public String GetAction() {
        return _action;
    }

    public boolean GetBoolean() {
        return _bool;
    }

    public String GetFlag() {
        return _flag;
    }

    public static SocketAction GetById(int id) {
        for (SocketAction e : values()) {
            if (e._id == id) {
                return e;
            }
        }
        return NULL;
    }

    public static SocketAction GetByAction(String action) {
        for (SocketAction e : values()) {
            if (e._action.contains(action)) {
                return e;
            }
        }
        return NULL;
    }

    public static SocketAction GetByFlag(String flag) {
        for (SocketAction e : values()) {
            if (e._flag.contains(flag)) {
                return e;
            }
        }
        return NULL;
    }
}
