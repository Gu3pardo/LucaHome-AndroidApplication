package guepardoapps.lucahome.common.controller;

import android.view.Display;

public interface IDisplayController {
    Display GetDisplayDimension();

    void ScreenOn(int[] adFlags, int[] viewFlags);

    void ScreenOff(int[] removeFlags);

    boolean IsScreenOn();

    int GetCurrentBrightness();

    void SetBrightness(int brightness);

    void SetBrightness(double brightness);
}
