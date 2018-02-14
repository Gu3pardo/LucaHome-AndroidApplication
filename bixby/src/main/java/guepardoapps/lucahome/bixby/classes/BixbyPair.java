package guepardoapps.lucahome.bixby.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.lucahome.bixby.classes.actions.BixbyAction;
import guepardoapps.lucahome.bixby.classes.requirements.BixbyRequirement;

@SuppressWarnings({"WeakerAccess"})
public class BixbyPair implements Serializable {
    private static final String Tag = BixbyPair.class.getSimpleName();

    public enum DatabaseAction {Null, Add, Update, Delete}

    private int _actionId;
    private BixbyAction _action;
    private ArrayList<BixbyRequirement> _requirements;

    private DatabaseAction _databaseAction = DatabaseAction.Add;

    public BixbyPair(int actionId, @NonNull BixbyAction action, @NonNull ArrayList<BixbyRequirement> requirements, @NonNull DatabaseAction databaseAction) {
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

    public void SetRequirements(@NonNull ArrayList<BixbyRequirement> requirements) {
        _requirements = requirements;
    }

    public ArrayList<BixbyRequirement> GetRequirements() {
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
        for (int requirementIndex = 0; requirementIndex < _requirements.size(); requirementIndex++) {
            requirementString.append(_requirements.get(requirementIndex).toString());
        }
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"ActionId\":%d,\"Action\":\"%s\",\"Requirements\":\"%s\",\"DatabaseAction\":\"%s\"}",
                Tag, _actionId, _action, requirementString.toString(), _databaseAction);
    }
}
