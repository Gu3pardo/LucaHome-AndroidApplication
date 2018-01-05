package guepardoapps.lucahome.basic.controller;

import android.bluetooth.BluetoothAdapter;

import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;

public class BluetoothController {
    private static final String TAG = BluetoothController.class.getSimpleName();

    public BluetoothController() {
    }

    public boolean IsBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            boolean isEnabled = bluetoothAdapter.isEnabled();
            Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "IsBluetoothEnabled: %s", isEnabled));
            return isEnabled;
        }
        Logger.getInstance().Warning(TAG, "bluetoothAdapter is null!");
        return false;
    }

    public void SetNewBluetoothState(boolean isEnabled) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "SetNewBluetoothState: %s", isEnabled));
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (isEnabled) {
            if (IsBluetoothEnabled()) {
                Logger.getInstance().Information(TAG, "BT already enabled!");
                return;
            }
            bluetoothAdapter.enable();
        } else {
            if (!IsBluetoothEnabled()) {
                Logger.getInstance().Information(TAG, "BT already disabled!");
                return;
            }
            bluetoothAdapter.disable();
        }
    }
}