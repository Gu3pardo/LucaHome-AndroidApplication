package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public interface IJsonDataConverter {
    ArrayList<?> GetList(@NonNull String[] stringArray);

    ArrayList<?> GetList(@NonNull String responseString);
}
