package guepardoapps.lucahome.common.controller;

import android.content.Context;
import android.support.annotation.NonNull;

public interface ITTSController {
    void Initialize(@NonNull Context context, boolean enabled);

    void SetEnabled(boolean enabled);

    void Speak(@NonNull String text);

    void Dispose();
}
