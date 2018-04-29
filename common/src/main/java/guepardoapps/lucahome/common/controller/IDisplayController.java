package guepardoapps.lucahome.common.controller;

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
