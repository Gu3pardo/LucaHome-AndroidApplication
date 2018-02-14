package guepardoapps.lucahome.common.services;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.WirelessSwitch;

public interface IWirelessSwitchService extends ILucaService<WirelessSwitch> {
    String WirelessSwitchDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelessswitch.download.finished";
    String WirelessSwitchAddFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelessswitch.add.finished";
    String WirelessSwitchUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelessswitch.update.finished";
    String WirelessSwitchDeleteFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelessswitch.delete.finished";
    String WirelessSwitchToggleFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelessswitch.toggle.finished";

    String WirelessSwitchDownloadFinishedBundle = "WirelessSwitchDownloadFinishedBundle";
    String WirelessSwitchAddFinishedBundle = "WirelessSwitchAddFinishedBundle";
    String WirelessSwitchUpdateFinishedBundle = "WirelessSwitchUpdateFinishedBundle";
    String WirelessSwitchDeleteFinishedBundle = "WirelessSwitchDeleteFinishedBundle";
    String WirelessSwitchToggleFinishedBundle = "WirelessSwitchToggleFinishedBundle";

    int NotificationId = 10752177;

    WirelessSwitch GetByName(@NonNull String wirelessSwitchName);

    ArrayList<String> GetNameList();

    ArrayList<UUID> GetRoomUuidList();

    void ToggleWirelessSwitch(WirelessSwitch entry);

    void ToggleAllWirelessSwitches();
}
