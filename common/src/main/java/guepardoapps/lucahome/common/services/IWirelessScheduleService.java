package guepardoapps.lucahome.common.services;

import guepardoapps.lucahome.common.classes.WirelessSchedule;

public interface IWirelessScheduleService extends ILucaService<WirelessSchedule> {
    String WirelessScheduleDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelessschedule.download.finished";
    String WirelessScheduleAddFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelessschedule.add.finished";
    String WirelessScheduleUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelessschedule.update.finished";
    String WirelessScheduleDeleteFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelessschedule.delete.finished";
    String WirelessScheduleSetFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelessschedule.set.finished";

    String WirelessScheduleDownloadFinishedBundle = "WirelessScheduleDownloadFinishedBundle";
    String WirelessScheduleAddFinishedBundle = "WirelessScheduleAddFinishedBundle";
    String WirelessScheduleUpdateFinishedBundle = "WirelessScheduleUpdateFinishedBundle";
    String WirelessScheduleDeleteFinishedBundle = "WirelessScheduleDeleteFinishedBundle";
    String WirelessScheduleSetFinishedBundle = "WirelessScheduleSetFinishedBundle";

    void SetWirelessScheduleState(WirelessSchedule entry, boolean newState) throws Exception;
}
