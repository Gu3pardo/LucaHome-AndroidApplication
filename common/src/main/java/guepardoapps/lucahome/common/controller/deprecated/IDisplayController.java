package guepardoapps.lucahome.common.controller.deprecated;

import android.view.Display;

@SuppressWarnings({"unused"})
public interface IDisplayController {
    Display GetDisplayDimension();

    boolean IsScreenOn();

    void ScreenOff(int[] removeFlags);

    int GetCurrentBrightness();

    void SetBrightness(int brightness);

    void SetBrightness(double brightness);
}
