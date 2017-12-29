package guepardoapps.lucahome.common.interfaces.converter;

import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableList;

public interface IJsonDataConverter {
    SerializableList<?> GetList(@NonNull String[] stringArray);

    SerializableList<?> GetList(@NonNull String responseString);
}
