package guepardoapps.lucahome.common.services;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import guepardoapps.lucahome.common.controller.DownloadController;

public class DownloadStorageService implements IDownloadStorageService {
    private static final DownloadStorageService Singleton = new DownloadStorageService();

    private Map<DownloadController.DownloadType, byte[]> _downloadStorage = new HashMap<>();

    private DownloadStorageService() {
    }

    public static DownloadStorageService getInstance() {
        return Singleton;
    }

    @Override
    public void PutDownloadResult(@NonNull DownloadController.DownloadType key, @NonNull byte[] downloadResult) {
        _downloadStorage.put(key, downloadResult);
    }

    @Override
    public byte[] GetDownloadResult(@NonNull DownloadController.DownloadType key) {
        byte[] downloadResult = new byte[]{};
        if (_downloadStorage.containsKey(key)) {
            downloadResult = _downloadStorage.get(key);
            _downloadStorage.remove(key);
        }
        return downloadResult;
    }
}
