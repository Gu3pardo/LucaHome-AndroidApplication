package guepardoapps.bixby.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.bixby.classes.actions.BixbyAction;
import guepardoapps.bixby.classes.requirements.BixbyRequirement;
import guepardoapps.lucahome.basic.classes.SerializableList;

@SuppressWarnings({"unused", "WeakerAccess"})
public class BixbyPair implements Serializable {
    private static final String TAG = BixbyPair.class.getSimpleName();

    public enum DatabaseAction {Null, Add, Update, Delete}

    private int _actionId;
    private BixbyAction _action;
    private SerializableList<BixbyRequirement> _requirements;

    private DatabaseAction _databaseAction = DatabaseAction.Add;

    public BixbyPair(int actionId, @NonNull BixbyAction action, @NonNull SerializableList<BixbyRequirement> requirements, @NonNull DatabaseAction databaseAction) {
        _actionId = actionId;
        _action = action;
        _requirements = requirements;
        _databaseAction = databaseAction;
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

    public void SetDatabaseAction(@NonNull DatabaseAction databaseAction) {
        _databaseAction = databaseAction;
    }

    public DatabaseAction GetDatabaseAction() {
        return _databaseAction;
    }

    @Override
    public String toString() {
        StringBuilder requirementString = new StringBuilder();
        for (int requirementIndex = 0; requirementIndex < _requirements.getSize(); requirementIndex++) {
            requirementString.append(_requirements.getValue(requirementIndex).toString());
        }
        return String.format(Locale.getDefault(), "{%s:{ActionId:%d,Action:%s,Requirements:%s,DatabaseAction:%s}}", TAG, _actionId, _action, requirementString.toString(), _databaseAction);
    }
}
