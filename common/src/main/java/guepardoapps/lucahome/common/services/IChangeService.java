package guepardoapps.lucahome.common.services;

import guepardoapps.lucahome.common.classes.Change;

public interface IChangeService extends ILucaService<Change> {
    String ChangeDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.change.download.finished";

    String ChangeDownloadFinishedBundle = "BirthdayDownloadFinishedBundle";
}
