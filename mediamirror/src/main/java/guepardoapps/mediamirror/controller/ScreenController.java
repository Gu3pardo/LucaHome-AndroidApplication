package guepardoapps.mediamirror.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.controller.DisplayController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;

public class ScreenController extends DisplayController {
    private static final String TAG = ScreenController.class.getSimpleName();

    private static final int BRIGHTNESS_CHANGE_STEP = 25;

    public static final int INCREASE = 1;
    public static final int DECREASE = -1;

    private boolean _isInitialized;

    private ReceiverController _receiverController;

    private BroadcastReceiver _actionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra(Bundles.SCREEN_BRIGHTNESS, 0);
            if (action == INCREASE) {
                increaseBrightness();
            } else if (action == DECREASE) {
                decreaseBrightness();
            } else {
                Logger.getInstance().Warning(TAG, "Action not supported! " + String.valueOf(action));
            }
        }
    };

    private BroadcastReceiver _valueReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int value = intent.getIntExtra(Bundles.SCREEN_BRIGHTNESS, -1);
            if (value != -1) {
                SetBrightness(value);
            }
        }
    };

    public ScreenController(@NonNull Context context) {
        super(context);
        _receiverController = new ReceiverController(context);
    }

    public void onCreate() {
    }

    public void onPause() {
    }

    public void onResume() {
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_actionReceiver, new String[]{Broadcasts.ACTION_SCREEN_BRIGHTNESS});
            _receiverController.RegisterReceiver(_valueReceiver, new String[]{Broadcasts.VALUE_SCREEN_BRIGHTNESS});
            _isInitialized = true;
        } else {
            Logger.getInstance().Warning(TAG, "Is ALREADY initialized!");
        }
    }

    public void onDestroy() {
        if (!_isInitialized) {
            Logger.getInstance().Warning(TAG, "Not initialized!");
            return;
        }

        _receiverController.Dispose();
        _isInitialized = false;
    }

    private void increaseBrightness() {
        int currentBrightness = GetCurrentBrightness();
        if (currentBrightness == -1) {
            Logger.getInstance().Warning(TAG, "Failed to get current brightness!");
            return;
        }
        int newBrightness = currentBrightness + BRIGHTNESS_CHANGE_STEP;
        SetBrightness(newBrightness);
    }

    private void decreaseBrightness() {
        int currentBrightness = GetCurrentBrightness();
        if (currentBrightness == -1) {
            Logger.getInstance().Warning(TAG, "Failed to get current brightness!");
            return;
        }
        int newBrightness = currentBrightness - BRIGHTNESS_CHANGE_STEP;
        SetBrightness(newBrightness);
    }
}
