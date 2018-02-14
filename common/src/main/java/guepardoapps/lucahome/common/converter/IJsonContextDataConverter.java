package guepardoapps.lucahome.common.converter;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public interface IJsonContextDataConverter {
    ArrayList<?> GetList(@NonNull String[] jsonStringArray, @NonNull Context context);

    ArrayList<?> GetList(@NonNull String jsonString, @NonNull Context context);
}
