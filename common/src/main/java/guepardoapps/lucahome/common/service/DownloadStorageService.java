package guepardoapps.lucahome.common.service;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.controller.DownloadController;

public class DownloadStorageService {
    private static final String TAG = DownloadStorageService.class.getSimpleName();

    private static final DownloadStorageService SINGLETON = new DownloadStorageService();

    private Map<DownloadController.DownloadType, byte[]> _downloadStorage = new HashMap<>();

    private DownloadStorageService() {
    }

    public static DownloadStorageService getInstance() {
        return SINGLETON;
    }

    public void PutDownloadResult(@NonNull DownloadController.DownloadType key, @NonNull byte[] downloadResult) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "PutDownloadResult: Key: %s and downloadResult: %s", key, Tools.DecompressByteArrayToString(downloadResult)));
        _downloadStorage.put(key, downloadResult);
    }

    public byte[] GetDownloadResult(@NonNull DownloadController.DownloadType key) {
        byte[] downloadResult = new byte[]{};
        if (_downloadStorage.containsKey(key)) {
            downloadResult = _downloadStorage.get(key);
            _downloadStorage.remove(key);
        }
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "GetDownloadResult: Key: %s and downloadResult: %s", key, Tools.DecompressByteArrayToString(downloadResult)));
        return downloadResult;
    }
}
