package guepardoapps.lucahome.basic.controller;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import java.util.Locale;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.basic.utils.Logger;

public class DisplayController {
    private static final String TAG = DisplayController.class.getSimpleName();
    private Logger _logger;

    private static final double BRIGHTNESS_MAX_LEVEL = 1.0;
    private static final double BRIGHTNESS_MIN_LEVEL = 0.1;

    private static final int SCREEN_OFF_MIN_SEC = 5;
    private static final int RUNNABLE_TIMEOUT = 1000;
    private int _secondsSinceScreenOffCmd;

    private Context _context;
    private KeyController _keyController;

    private Handler _screenOffHandler = new Handler();
    private Runnable _screenOffCountdownRunnable = new Runnable() {
        @Override
        public void run() {
            _secondsSinceScreenOffCmd++;
            int timeToScreenOff = SCREEN_OFF_MIN_SEC - _secondsSinceScreenOffCmd;

            if (timeToScreenOff < 0) {
                _secondsSinceScreenOffCmd = -1;
                return;
            }

            Toasty.info(_context, String.format(Locale.GERMAN, "Displays disables in %d seconds!", timeToScreenOff), 950).show();
            _screenOffHandler.postDelayed(_screenOffCountdownRunnable, RUNNABLE_TIMEOUT);
        }
    };

    public DisplayController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
        _keyController = new KeyController();
    }

    public Display GetDisplayDimension() {
        WindowManager windowManager = (WindowManager) _context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            _logger.Error("windowManager is null!");
            return null;
        }

        Display display = windowManager.getDefaultDisplay();
        _logger.Debug(String.format(Locale.GERMAN, "Display: %s", display));
        return display;
    }

    @SuppressWarnings("deprecation")
    public void ScreenOn(int[] adFlags, int[] viewFlags) {
        _logger.Debug("ScreenOn");

        Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30 * 60 * 1000);
        _screenOffHandler.removeCallbacks(_screenOffCountdownRunnable);

        if (IsScreenOn()) {
            _logger.Debug("Screen is already on!");
            return;
        }

        int[] keys = new int[]{KeyEvent.KEYCODE_POWER};

        try {
            _keyController.SimulateKeyPress(keys, 0);
        } catch (Exception ex) {
            _logger.Error(ex.toString());
            return;
        }

        KeyguardManager keyguardManager = (KeyguardManager) _context.getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("MediaMirrorKeyguardLock");
        keyguardLock.disableKeyguard();

        PowerManager powerManager = (PowerManager) _context.getSystemService(Context.POWER_SERVICE);
        WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MediaMirrorWakeLock");
        wakeLock.acquire();
        wakeLock.release();

        if (adFlags != null) {
            if (adFlags.length > 0) {
                if (_context instanceof Activity) {
                    _logger.Debug("Trying to ad flags!");
                    Window window = ((Activity) _context).getWindow();

                    if (window == null) {
                        _logger.Error("Window is null!");
                        return;
                    }

                    for (int flag : adFlags) {
                        window.addFlags(flag);
                    }
                } else {
                    _logger.Warning("Context is not an activity!");
                }
            }
        }

        if (viewFlags != null) {
            if (viewFlags.length > 0) {
                if (_context instanceof Activity) {
                    _logger.Debug("Trying to set viewFlags!");
                    Window window = ((Activity) _context).getWindow();

                    if (window == null) {
                        _logger.Error("Window is null!");
                        return;
                    }

                    for (int flag : viewFlags) {
                        window.getDecorView().setSystemUiVisibility(flag);
                    }
                } else {
                    _logger.Warning("Context is not an activity!");
                }
            }
        }
    }

    public void ScreenOff(int[] removeFlags) {
        _logger.Debug("ScreenOff is only setting the sceen timeout down to minimum!");

        Settings.System.putInt(_context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, SCREEN_OFF_MIN_SEC * 1000);
        _screenOffHandler.post(_screenOffCountdownRunnable);

        if (!IsScreenOn()) {
            _logger.Debug("Screen is already off!");
            return;
        }

        if (removeFlags != null) {
            if (removeFlags.length > 0) {
                if (_context instanceof Activity) {
                    _logger.Debug("Trying to lock the screen!");
                    Window window = ((Activity) _context).getWindow();

                    if (window == null) {
                        _logger.Error("Window is null!");
                        return;
                    }

                    for (int flag : removeFlags) {
                        window.clearFlags(flag);
                    }
                } else {
                    _logger.Warning("Context is not an activity!");
                }
            }
        }
    }

    public boolean IsScreenOn() {
        DisplayManager displayManager = (DisplayManager) _context.getSystemService(Context.DISPLAY_SERVICE);
        if (displayManager == null) {
            _logger.Error("displayManager is null!");
            return false;
        }

        for (Display display : displayManager.getDisplays()) {
            if (display.getState() != Display.STATE_OFF) {
                return true;
            }
        }
        return false;
    }

    public int GetCurrentBrightness() {
        _logger.Debug("GetCurrentBrightness");

        ContentResolver contentResolver = _context.getContentResolver();
        int brightness = -1;

        try {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException e) {
            _logger.Error(e.toString());
        }

        return brightness;
    }

    public void SetBrightness(int brightness) {
        _logger.Debug("SetBrightness to " + String.valueOf(brightness / (float) 255));

        double newBrightness = (brightness / (double) 255);

        setBrightness(newBrightness);
    }

    public void SetBrightness(double brightness) {
        _logger.Debug("SetBrightness to " + String.valueOf(brightness));

        setBrightness(brightness);
    }

    private void setBrightness(double brightness) {
        if (brightness > BRIGHTNESS_MAX_LEVEL) {
            _logger.Error("Brightness to high! Set to maximum level!");
            brightness = BRIGHTNESS_MAX_LEVEL;
        }

        if (brightness < BRIGHTNESS_MIN_LEVEL) {
            _logger.Error("Brightness to low! Set to minimum level!");
            brightness = BRIGHTNESS_MIN_LEVEL;
        }

        ContentResolver contentResolver = _context.getContentResolver();
        Window window = ((Activity) _context).getWindow();

        try {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, (int) (brightness * 255));
            LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = (float) brightness;
            window.setAttributes(layoutParams);
        } catch (Exception e) {
            _logger.Error(e.toString());
            Toasty.error(_context, "Failed to set brightness!", Toast.LENGTH_SHORT).show();
        }
    }
}