package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public class SavedAction implements Serializable {
    private static final long serialVersionUID = -4577368865681987282L;

    private int _id;

    private String _name;
    private String _action;
    private String _broadcast;

    private static final String TAG = SavedAction.class.getSimpleName();

    public SavedAction(
            int id,
            @NonNull String name,
            @NonNull String action,
            @NonNull String broadcast) {
        _id = id;

        _name = name;
        _action = action;
        _broadcast = broadcast;
    }

    public int GetId() {
        return _id;
    }

    public String GetName() {
        return _name;
    }

    public String GetAction() {
        return _action;
    }

    public String GetBroadcast() {
        return _broadcast;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "{%s{ID:%d}{Name:%s}{Action:%s}{Broadcast:%s}}",
                TAG, _id, _name, _action, _broadcast);
    }
}
