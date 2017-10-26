package guepardoapps.lucahome.basic.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import guepardoapps.lucahome.basic.utils.Logger;

public class SharedPrefController {

    private static final String TAG = SharedPrefController.class.getSimpleName();
    private Logger _logger;

    private SharedPreferences _sharedPreferences;

    public SharedPrefController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _logger.Debug("Created new " + TAG + "...");

        _sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean Contains(@NonNull String key) {
        if (_sharedPreferences.contains(key)) {
            return true;
        }
        _logger.Warning("Key not available!");
        return false;
    }

    public boolean SaveStringValue(
            @NonNull String key,
            @NonNull String value) {
        if (_sharedPreferences == null) {
            _logger.Warning("SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putString(key, value);

        return editor.commit();
    }

    public boolean SaveIntegerValue(
            @NonNull String key,
            int value) {
        if (_sharedPreferences == null) {
            _logger.Warning("SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putInt(key, value);

        return editor.commit();
    }

    public boolean SaveBooleanValue(
            @NonNull String key,
            boolean value) {
        if (_sharedPreferences == null) {
            _logger.Warning("SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putBoolean(key, value);

        return editor.commit();
    }

    public boolean SaveStringListToSharedPreferences(
            @NonNull List<String> list,
            @NonNull String arrayName) {
        if (_sharedPreferences == null) {
            _logger.Warning("SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putInt(arrayName + "_size", list.size());
        for (int index = 0; index < list.size(); index++) {
            editor.putString(arrayName + "_" + index, list.get(index));
        }

        return editor.commit();
    }

    public boolean SaveIntegerListToSharedPreferences(
            @NonNull List<Integer> list,
            @NonNull String arrayName) {
        if (_sharedPreferences == null) {
            _logger.Warning("SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putInt(arrayName + "_size", list.size());
        for (int index = 0; index < list.size(); index++) {
            editor.putInt(arrayName + "_" + index, list.get(index));
        }

        return editor.commit();
    }

    public boolean SaveBooleanListToSharedPreferences(
            @NonNull List<Boolean> list,
            @NonNull String arrayName) {
        if (_sharedPreferences == null) {
            _logger.Warning("SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putInt(arrayName + "_size", list.size());
        for (int index = 0; index < list.size(); index++) {
            editor.putBoolean(arrayName + "_" + index, list.get(index));
        }

        return editor.commit();
    }

    public String LoadStringValueFromSharedPreferences(@NonNull String value) {
        if (_sharedPreferences == null) {
            _logger.Warning("SharedPreferences is null!");
            return "";
        }

        return _sharedPreferences.getString(value, "");
    }

    public int LoadIntegerValueFromSharedPreferences(@NonNull String value) {
        if (_sharedPreferences == null) {
            _logger.Warning("SharedPreferences is null!");
            return 0;
        }

        return _sharedPreferences.getInt(value, 0);
    }

    public Boolean LoadBooleanValueFromSharedPreferences(@NonNull String value) {
        if (_sharedPreferences == null) {
            _logger.Warning("SharedPreferences is null!");
            return false;
        }

        return _sharedPreferences.getBoolean(value, false);
    }

    public List<String> LoadStringListFromSharedPreferences(@NonNull String listName) {
        if (_sharedPreferences == null) {
            _logger.Warning("SharedPreferences is null!");
            return new ArrayList<>();
        }

        int size = _sharedPreferences.getInt(listName + "_size", 0);
        List<String> list = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            list.add(_sharedPreferences.getString(listName + "_" + index, null));
        }

        return list;
    }

    public List<Integer> LoadIntegerListFromSharedPreferences(@NonNull String listName) {
        if (_sharedPreferences == null) {
            _logger.Warning("SharedPreferences is null!");
            return new ArrayList<>();
        }

        int size = _sharedPreferences.getInt(listName + "_size", 0);
        List<Integer> list = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            list.add(_sharedPreferences.getInt(listName + "_" + index, 0));
        }

        return list;
    }

    public List<Boolean> LoadBooleanListFromSharedPreferences(@NonNull String listName) {
        if (_sharedPreferences == null) {
            _logger.Warning("SharedPreferences is null!");
            return new ArrayList<>();
        }

        int size = _sharedPreferences.getInt(listName + "_size", 0);
        List<Boolean> list = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            list.add(_sharedPreferences.getBoolean(listName + "_" + index, false));
        }

        return list;
    }

    public boolean RemoveSharedPreferences() {
        if (_sharedPreferences == null) {
            _logger.Warning("SharedPreferences is null!");
            return false;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.clear();
        return editor.commit();
    }
}
