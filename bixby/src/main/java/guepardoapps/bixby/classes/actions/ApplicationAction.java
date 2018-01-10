package guepardoapps.bixby.classes.actions;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.bixby.interfaces.IBixbyAction;

@SuppressWarnings("WeakerAccess")
public class ApplicationAction implements IBixbyAction, Serializable {
    private static final String TAG = ApplicationAction.class.getSimpleName();

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
        return String.format(Locale.getDefault(), "%s for %s", TAG, _packageName);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s:{PackageName:%s}}", TAG, _packageName);
    }
}
