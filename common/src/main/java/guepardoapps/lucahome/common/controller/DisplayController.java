package guepardoapps.lucahome.common.controller;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import java.util.Locale;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class DisplayController implements IDisplayController {
    private static final String Tag = DisplayController.class.getSimpleName();

    private static final double BrightnessLevelMin = 0.1;
    private static final double BrightnessLevelMax = 1.0;

    private static final int MinScreenOffSec = 5;
    private static final int RunnableTimeout = 1000;
    private int _secondsSinceScreenOffCmd;

    private Context _context;

    private Handler _screenOffHandler = new Handler();
    private Runnable _screenOffCountdownRunnable = new Runnable() {
        @Override
        public void run() {
            _secondsSinceScreenOffCmd++;
            int timeToScreenOff = MinScreenOffSec - _secondsSinceScreenOffCmd;

            if (timeToScreenOff < 0) {
                _secondsSinceScreenOffCmd = -1;
                return;
            }

            Toasty.info(_context, String.format(Locale.GERMAN, "Displays disables in %d seconds!", timeToScreenOff), 950).show();
            _screenOffHandler.postDelayed(_screenOffCountdownRunnable, RunnableTimeout);
        }
    };

    public DisplayController(@NonNull Context context) {
        _context = context;
    }

    @Override
    public Display GetDisplayDimension() {
        WindowManager windowManager = (WindowManager) _context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            Logger.getInstance().Error(Tag, "windowManager is null!");
            return null;
        }
        return windowManager.getDefaultDisplay();
    }

    @Override
    public void ScreenOff(int[] removeFlags) {
        Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, MinScreenOffSec * 1000);
        _screenOffHandler.post(_screenOffCountdownRunnable);

        if (!IsScreenOn()) {
            return;
        }

        if (removeFlags != null) {
            if (removeFlags.length > 0) {
                if (_context instanceof Activity) {
                    Window window = ((Activity) _context).getWindow();

                    if (window == null) {
                        Logger.getInstance().Error(Tag, "Window is null!");
                        return;
                    }

                    for (int flag : removeFlags) {
                        window.clearFlags(flag);
                    }
                } else {
                    Logger.getInstance().Warning(Tag, "Context is not an activity!");
                }
            }
        }
    }

    @Override
    public boolean IsScreenOn() {
        DisplayManager displayManager = (DisplayManager) _context.getSystemService(Context.DISPLAY_SERVICE);
        if (displayManager == null) {
            Logger.getInstance().Error(Tag, "displayManager is null!");
            return false;
        }

        for (Display display : displayManager.getDisplays()) {
            if (display.getState() != Display.STATE_OFF) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int GetCurrentBrightness() {
        ContentResolver contentResolver = _context.getContentResolver();
        int brightness = -1;

        try {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException e) {
            Logger.getInstance().Error(Tag, e.toString());
        }

        return brightness;
    }

    @Override
    public void SetBrightness(int brightness) {
        double newBrightness = (brightness / (double) 255);
        setBrightness(newBrightness);
    }

    @Override
    public void SetBrightness(double brightness) {
        setBrightness(brightness);
    }

    private void setBrightness(double brightness) {
        if (brightness > BrightnessLevelMax) {
            Logger.getInstance().Error(Tag, "Brightness to high! Set to maximum level!");
            brightness = BrightnessLevelMax;
        }

        if (brightness < BrightnessLevelMin) {
            Logger.getInstance().Error(Tag, "Brightness to low! Set to minimum level!");
            brightness = BrightnessLevelMin;
        }

        ContentResolver contentResolver = _context.getContentResolver();
        Window window = ((Activity) _context).getWindow();

        try {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, (int) (brightness * 255));
            LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = (float) brightness;
            window.setAttributes(layoutParams);
        } catch (Exception e) {
            Logger.getInstance().Error(Tag, e.toString());
            Toasty.error(_context, "Failed to set brightness!", Toast.LENGTH_SHORT).show();
        }
    }
}
