package guepardoapps.lucahome.bixby;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;

@SuppressWarnings({"unused", "WeakerAccess"})
public class BixbyPair implements Serializable {
    private static final String TAG = BixbyPair.class.getSimpleName();

    private int _actionId;
    private BixbyAction _action;
    private SerializableList<BixbyRequirement> _requirements;

    public BixbyPair(int actionId, @NonNull BixbyAction action, @NonNull SerializableList<BixbyRequirement> requirements) {
        _actionId = actionId;
        _action = action;
        _requirements = requirements;
    }

    public void SetActionId(int actionId) {
        _actionId = actionId;
    }

    public int GetActionId() {
        return _actionId;
    }

    public void SetAction(@NonNull BixbyAction action) {
        _action = action;
    }

    public BixbyAction GetAction() {
        return _action;
    }

    public void SetRequirements(@NonNull SerializableList<BixbyRequirement> requirements) {
        _requirements = requirements;
    }

    public SerializableList<BixbyRequirement> GetRequirements() {
        return _requirements;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s:{ActionId:%d,Action:%s,Requirements:%s}}", TAG, _actionId, _action, _requirements);
    }
}
