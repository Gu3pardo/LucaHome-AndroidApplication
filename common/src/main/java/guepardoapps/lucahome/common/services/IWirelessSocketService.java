package guepardoapps.lucahome.common.services;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.WirelessSocket;

public interface IWirelessSocketService extends ILucaService<WirelessSocket> {
    String WirelessSocketDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelesssocket.download.finished";
    String WirelessSocketAddFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelesssocket.add.finished";
    String WirelessSocketUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelesssocket.update.finished";
    String WirelessSocketDeleteFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelesssocket.delete.finished";
    String WirelessSocketSetFinishedBroadcast = "guepardoapps.lucahome.common.services.wirelesssocket.set.finished";

    String WirelessSocketDownloadFinishedBundle = "WirelessSocketDownloadFinishedBundle";
    String WirelessSocketAddFinishedBundle = "WirelessSocketAddFinishedBundle";
    String WirelessSocketUpdateFinishedBundle = "WirelessSocketUpdateFinishedBundle";
    String WirelessSocketDeleteFinishedBundle = "WirelessSocketDeleteFinishedBundle";
    String WirelessSocketSetFinishedBundle = "WirelessSocketSetFinishedBundle";

    int NotificationId = 211990;

    WirelessSocket GetByName(@NonNull String wirelessSocketName);

    ArrayList<String> GetNameList();

    ArrayList<UUID> GetRoomUuidList();

    ArrayList<String> GetCodeList();

    void SetWirelessSocketState(WirelessSocket entry, boolean newState) throws Exception;

    void DeactivateAllWirelessSockets();
}
