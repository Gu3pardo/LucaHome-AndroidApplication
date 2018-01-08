package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

@SuppressWarnings({"unused"})
public class LucaUser implements Serializable {
    private static final long serialVersionUID = 109475847363954820L;
    private static final String TAG = LucaUser.class.getSimpleName();

    private String _name;
    private String _passphrase;

    public LucaUser(
            @NonNull String name,
            @NonNull String passphrase) {
        _name = name;
        _passphrase = passphrase;
    }

    public String GetName() {
        return _name;
    }

    public String GetPassphrase() {
        return _passphrase;
    }

    public void SetPassphrase(@NonNull String passphrase) {
        _passphrase = passphrase;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s : (Name : %s );(Passphrase : -/- ))", TAG, _name);
    }
}
