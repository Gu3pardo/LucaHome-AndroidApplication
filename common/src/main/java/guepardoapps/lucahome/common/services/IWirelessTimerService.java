package guepardoapps.lucahome.common.services;

import guepardoapps.lucahome.common.classes.WirelessTimer;

public interface IWirelessTimerService extends ILucaService<WirelessTimer> {
    String WirelessTimerDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelesstimer.download.finished";
    String WirelessTimerAddFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelesstimer.add.finished";
    String WirelessTimerUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelesstimer.update.finished";
    String WirelessTimerDeleteFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelesstimer.delete.finished";

    String WirelessTimerDownloadFinishedBundle = "WirelessTimerDownloadFinishedBundle";
    String WirelessTimerAddFinishedBundle = "WirelessTimerAddFinishedBundle";
    String WirelessTimerUpdateFinishedBundle = "WirelessTimerUpdateFinishedBundle";
    String WirelessTimerDeleteFinishedBundle = "WirelessTimerDeleteFinishedBundle";
}
