package guepardoapps.lucahome.common.services;

import android.support.annotation.NonNull;

import guepardoapps.lucahome.common.controller.DownloadController;

public interface IDownloadStorageService {
    void PutDownloadResult(@NonNull DownloadController.DownloadType key, @NonNull byte[] downloadResult);

    byte[] GetDownloadResult(@NonNull DownloadController.DownloadType key);
}
