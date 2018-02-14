package guepardoapps.lucahome.common.controller;

public interface IBluetoothController {
    boolean IsBluetoothEnabled();

    void SetNewBluetoothState(boolean isEnabled);
}
