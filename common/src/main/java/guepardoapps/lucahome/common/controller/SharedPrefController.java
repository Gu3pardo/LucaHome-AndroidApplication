package guepardoapps.lucahome.common.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class SharedPrefController implements ISharedPrefController {
    private static final String Tag = SharedPrefController.class.getSimpleName();

    private SharedPreferences _sharedPreferences;

    public SharedPrefController(@NonNull Context context) {
        _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public boolean Contains(@NonNull String key) {
        if (_sharedPreferences.contains(key)) {
            return true;
        }
        Logger.getInstance().Warning(Tag, "Key not available!");
        return false;
    }

    @Override
    public boolean SaveStringValue(@NonNull String key, @NonNull String value) {
        if (_sharedPreferences == null) {
            Logger.getInstance().Warning(Tag, "SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putString(key, value);

        return editor.commit();
    }

    @Override
    public boolean SaveIntegerValue(@NonNull String key, int value) {
        if (_sharedPreferences == null) {
            Logger.getInstance().Warning(Tag, "SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putInt(key, value);

        return editor.commit();
    }

    @Override
    public boolean SaveBooleanValue(@NonNull String key, boolean value) {
        if (_sharedPreferences == null) {
            Logger.getInstance().Warning(Tag, "SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putBoolean(key, value);

        return editor.commit();
    }

    @Override
    public boolean SaveStringListToSharedPreferences(@NonNull String key, @NonNull List<String> value) {
        if (_sharedPreferences == null) {
            Logger.getInstance().Warning(Tag, "SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putInt(key + "_size", value.size());
        for (int index = 0; index < value.size(); index++) {
            editor.putString(key + "_" + index, value.get(index));
        }

        return editor.commit();
    }

    @Override
    public boolean SaveIntegerListToSharedPreferences(@NonNull String key, @NonNull List<Integer> value) {
        if (_sharedPreferences == null) {
            Logger.getInstance().Warning(Tag, "SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putInt(key + "_size", value.size());
        for (int index = 0; index < value.size(); index++) {
            editor.putInt(key + "_" + index, value.get(index));
        }

        return editor.commit();
    }

    @Override
    public boolean SaveBooleanListToSharedPreferences(@NonNull String key, @NonNull List<Boolean> value) {
        if (_sharedPreferences == null) {
            Logger.getInstance().Warning(Tag, "SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putInt(key + "_size", value.size());
        for (int index = 0; index < value.size(); index++) {
            editor.putBoolean(key + "_" + index, value.get(index));
        }

        return editor.commit();
    }

    @Override
    public String LoadStringValueFromSharedPreferences(@NonNull String key) {
        if (_sharedPreferences == null) {
            Logger.getInstance().Warning(Tag, "SharedPreferences is null!");
            return "";
        }

        return _sharedPreferences.getString(key, "");
    }

    @Override
    public int LoadIntegerValueFromSharedPreferences(@NonNull String key) {
        if (_sharedPreferences == null) {
            Logger.getInstance().Warning(Tag, "SharedPreferences is null!");
            return 0;
        }

        return _sharedPreferences.getInt(key, 0);
    }

    @Override
    public boolean LoadBooleanValueFromSharedPreferences(@NonNull String key) {
        if (_sharedPreferences == null) {
            Logger.getInstance().Warning(Tag, "SharedPreferences is null!");
            return false;
        }

        return _sharedPreferences.getBoolean(key, false);
    }

    @Override
    public List<String> LoadStringListFromSharedPreferences(@NonNull String key) {
        if (_sharedPreferences == null) {
            Logger.getInstance().Warning(Tag, "SharedPreferences is null!");
            return new ArrayList<>();
        }

        int size = _sharedPreferences.getInt(key + "_size", 0);
        List<String> list = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            list.add(_sharedPreferences.getString(key + "_" + index, null));
        }

        return list;
    }

    @Override
    public List<Integer> LoadIntegerListFromSharedPreferences(@NonNull String key) {
        if (_sharedPreferences == null) {
            Logger.getInstance().Warning(Tag, "SharedPreferences is null!");
            return new ArrayList<>();
        }

        int size = _sharedPreferences.getInt(key + "_size", 0);
        List<Integer> list = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            list.add(_sharedPreferences.getInt(key + "_" + index, 0));
        }

        return list;
    }

    @Override
    public List<Boolean> LoadBooleanListFromSharedPreferences(@NonNull String key) {
        if (_sharedPreferences == null) {
            Logger.getInstance().Warning(Tag, "SharedPreferences is null!");
            return new ArrayList<>();
        }

        int size = _sharedPreferences.getInt(key + "_size", 0);
        List<Boolean> list = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            list.add(_sharedPreferences.getBoolean(key + "_" + index, false));
        }

        return list;
    }

    @Override
    public boolean RemoveSharedPreferences() {
        if (_sharedPreferences == null) {
            Logger.getInstance().Warning(Tag, "SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.clear();
        return editor.commit();
    }
}
