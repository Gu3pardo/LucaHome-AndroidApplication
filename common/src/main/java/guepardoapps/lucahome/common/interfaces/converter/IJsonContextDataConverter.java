package guepardoapps.lucahome.common.interfaces.converter;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableList;

public interface IJsonContextDataConverter {
    SerializableList<?> GetList(@NonNull String[] stringArray, @NonNull Context context);

    SerializableList<?> GetList(@NonNull String responseString, @NonNull Context context);
}
