package guepardoapps.lucahome.common.controller.deprecated;

import android.content.Context;
import android.support.annotation.NonNull;

@SuppressWarnings({"unused"})
public interface ITTSController {
    void Initialize(@NonNull Context context, boolean enabled);

    void SetEnabled(boolean enabled);

    void Speak(@NonNull String text);

    void Dispose();
}
