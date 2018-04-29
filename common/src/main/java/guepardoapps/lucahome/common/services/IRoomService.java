package guepardoapps.lucahome.common.services;

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.Room;

@SuppressWarnings({"unused"})
public interface IRoomService extends ILucaService<Room> {
    String RoomDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.room.download.finished";
    String RoomAddFinishedBroadcast = "guepardoapps.lucahome.common.services.room.add.finished";
    String RoomUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.room.update.finished";
    String RoomDeleteFinishedBroadcast = "guepardoapps.lucahome.common.services.room.delete.finished";

    String RoomDownloadFinishedBundle = "RoomDownloadFinishedBundle";
    String RoomAddFinishedBundle = "RoomAddFinishedBundle";
    String RoomUpdateFinishedBundle = "RoomUpdateFinishedBundle";
    String RoomDeleteFinishedBundle = "RoomDeleteFinishedBundle";

    ArrayList<String> GetNameList();
}
