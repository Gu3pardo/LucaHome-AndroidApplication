package guepardoapps.lucahome.common.services;

import guepardoapps.lucahome.common.classes.MapContent;

public interface IMapContentService extends ILucaService<MapContent> {
    String MapContentDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.mapcontent.download.finished";
    String MapContentDownloadFinishedBundle = "MapContentDownloadFinishedBundle";
}
