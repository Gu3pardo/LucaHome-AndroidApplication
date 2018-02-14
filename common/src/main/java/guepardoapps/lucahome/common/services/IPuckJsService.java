package guepardoapps.lucahome.common.services;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.PuckJs;

public interface IPuckJsService extends ILucaService<PuckJs> {
    String PuckJsDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.puckjs.download.finished";
    String PuckJsAddFinishedBroadcast = "guepardoapps.lucahome.common.services.puckjs.add.finished";
    String PuckJsUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.puckjs.update.finished";
    String PuckJsDeleteFinishedBroadcast = "guepardoapps.lucahome.common.services.puckjs.delete.finished";

    String PuckJsDownloadFinishedBundle = "PuckJsDownloadFinishedBundle";
    String PuckJsAddFinishedBundle = "PuckJsAddFinishedBundle";
    String PuckJsUpdateFinishedBundle = "PuckJsUpdateFinishedBundle";
    String PuckJsDeleteFinishedBundle = "PuckJsDeleteFinishedBundle";

    ArrayList<String> GetNameList();

    ArrayList<UUID> GetRoomUuidList();

    ArrayList<String> GetMacList();
}
