package guepardoapps.lucahome.common.controller.mediaserver;

public interface IWirelessSocketBatteryController {
    int BatteryLimitUpperPercent = 90;
    int BatteryLimitLowerPercent = 15;

    void Dispose();
}
