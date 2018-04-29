package guepardoapps.lucahome.common.controller;

import android.support.annotation.NonNull;

import java.util.List;

@SuppressWarnings({"unused"})
public interface ISharedPrefController {
    boolean Contains(@NonNull String key);

    boolean SaveStringValue(@NonNull String key, @NonNull String value);

    boolean SaveIntegerValue(@NonNull String key, int value);

    boolean SaveBooleanValue(@NonNull String key, boolean value);

    boolean SaveStringListToSharedPreferences(@NonNull String key, @NonNull List<String> value);

    boolean SaveIntegerListToSharedPreferences(@NonNull String key, @NonNull List<Integer> value);

    boolean SaveBooleanListToSharedPreferences(@NonNull String key, @NonNull List<Boolean> value);

    String LoadStringValueFromSharedPreferences(@NonNull String key);

    int LoadIntegerValueFromSharedPreferences(@NonNull String key);

    boolean LoadBooleanValueFromSharedPreferences(@NonNull String key);

    List<String> LoadStringListFromSharedPreferences(@NonNull String key);

    List<Integer> LoadIntegerListFromSharedPreferences(@NonNull String key);

    List<Boolean> LoadBooleanListFromSharedPreferences(@NonNull String key);

    boolean RemoveSharedPreferences();
}
