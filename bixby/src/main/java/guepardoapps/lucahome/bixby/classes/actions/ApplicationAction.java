package guepardoapps.lucahome.bixby.classes.actions;

import android.support.annotation.NonNull;

import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public class ApplicationAction implements IBixbyAction {
    private static final String Tag = ApplicationAction.class.getSimpleName();

    private String _packageName;

    public ApplicationAction(@NonNull String packageName) {
        _packageName = packageName;
    }

    public ApplicationAction() {
        this("");
    }

    public String GetPackageName() {
        return _packageName;
    }

    @Override
    public String GetDatabaseString() {
        return _packageName;
    }

    @Override
    public String GetInformationString() {
        return String.format(Locale.getDefault(), "%s \nfor %s", Tag, _packageName);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"PackageName\":\"%s\"}",
                Tag, _packageName);
    }
}
