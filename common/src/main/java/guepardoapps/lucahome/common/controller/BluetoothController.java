package guepardoapps.lucahome.common.controller;

import android.bluetooth.BluetoothAdapter;

import java.util.Locale;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class BluetoothController implements IBluetoothController {
    private static final String Tag = BluetoothController.class.getSimpleName();

    public BluetoothController() {
    }

    @Override
    public boolean IsBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            boolean isEnabled = bluetoothAdapter.isEnabled();
            Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "IsBluetoothEnabled: %s", isEnabled));
            return isEnabled;
        }
        Logger.getInstance().Warning(Tag, "bluetoothAdapter is null!");
        return false;
    }

    @Override
    public void SetNewBluetoothState(boolean isEnabled) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "SetNewBluetoothState: %s", isEnabled));
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (isEnabled) {
            if (IsBluetoothEnabled()) {
                Logger.getInstance().Information(Tag, "BT already enabled!");
                return;
            }
            bluetoothAdapter.enable();
        } else {
            if (!IsBluetoothEnabled()) {
                Logger.getInstance().Information(Tag, "BT already disabled!");
                return;
            }
            bluetoothAdapter.disable();
        }
    }
}