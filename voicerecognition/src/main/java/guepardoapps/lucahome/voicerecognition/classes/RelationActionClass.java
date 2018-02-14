package guepardoapps.lucahome.voicerecognition.classes;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

public class RelationActionClass implements IRelationActionClass {
    private static String Tag = RelationActionClass.class.getSimpleName();

    private RelationAction _relationAction;
    private ArrayList<String> _actionParameter;

    public RelationActionClass(@NonNull RelationAction relationAction, @NonNull ArrayList<String> actionParameter) {
        _relationAction = relationAction;
        _actionParameter = actionParameter;
    }

    @Override
    public RelationAction GetRelationAction() {
        return _relationAction;
    }

    @Override
    public ArrayList<String> GetActionParameter() {
        return _actionParameter;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s:%s|%s", Tag, _relationAction, _actionParameter);
    }
}
