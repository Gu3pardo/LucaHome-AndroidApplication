package guepardoapps.lucahome.common.service;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import guepardoapps.lucahome.common.controller.DownloadController;

public class DownloadStorageService {
    //private static final String TAG = DownloadStorageService.class.getSimpleName();

    private static final DownloadStorageService SINGLETON = new DownloadStorageService();

    private Map<DownloadController.DownloadType, byte[]> _downloadStorage = new HashMap<>();

    private DownloadStorageService() {
    }

    public static DownloadStorageService getInstance() {
        return SINGLETON;
    }

    public void PutDownloadResult(@NonNull DownloadController.DownloadType key, @NonNull byte[] downloadResult) {
        _downloadStorage.put(key, downloadResult);
    }

    public byte[] GetDownloadResult(@NonNull DownloadController.DownloadType key) {
        byte[] downloadResult = new byte[]{};
        if (_downloadStorage.containsKey(key)) {
            downloadResult = _downloadStorage.get(key);
            _downloadStorage.remove(key);
        }
        return downloadResult;
    }
}
