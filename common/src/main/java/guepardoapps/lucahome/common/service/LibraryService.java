package guepardoapps.lucahome.common.service;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.MagazinDir;
import guepardoapps.lucahome.common.controller.LocalDriveController;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class LibraryService {
    public static class MagazinDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<MagazinDir> MagazinList;

        public MagazinDownloadFinishedContent(SerializableList<MagazinDir> magazinList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            MagazinList = magazinList;
        }
    }

    public static final String MagazinDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.magazin.download.finished";
    public static final String MagazinDownloadFinishedBundle = "MagazinDownloadFinishedBundle";

    private static final LibraryService SINGLETON = new LibraryService();
    private boolean _isInitialized;

    private static final String TAG = LibraryService.class.getSimpleName();
    private Logger _logger;

    private static final int TIMEOUT_MS = 3 * 60 * 60 * 1000;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_reloadListRunnable run");
            LoadMagazinList();
            _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);
        }
    };

    private BroadcastController _broadcastController;
    private LocalDriveController _localDriveController;

    private SerializableList<MagazinDir> _magazinList = new SerializableList<>();

    private LibraryService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static LibraryService getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _broadcastController = new BroadcastController(context);
        _localDriveController = LocalDriveController.getInstance();

        _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);

        _isInitialized = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _isInitialized = false;
    }

    public SerializableList<MagazinDir> GetMagazinList() {
        return _magazinList;
    }

    public MagazinDir GetByName(@NonNull String name) {
        for (int index = 0; index < _magazinList.getSize(); index++) {
            MagazinDir entry = _magazinList.getValue(index);

            if (entry.GetDirName().contains(name)) {
                return entry;
            }
        }

        return null;
    }

    public SerializableList<MagazinDir> FoundMagazins(@NonNull String searchKey) {
        SerializableList<MagazinDir> foundMagazins = new SerializableList<>();

        for (int index = 0; index < _magazinList.getSize(); index++) {
            MagazinDir entry = _magazinList.getValue(index);

            if (String.valueOf(entry.GetDirName()).contains(searchKey)
                    || entry.GetIcon().toString().contains(searchKey)
                    || entry.GetDirContent().toString().contains(searchKey)) {
                foundMagazins.addValue(entry);
            }
        }

        return foundMagazins;
    }

    public void LoadMagazinList() {
        _logger.Debug("LoadMagazinList");
        // TODO
    }

    private void sendFailedDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                MagazinDownloadFinishedBroadcast,
                MagazinDownloadFinishedBundle,
                new MagazinDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
    }
}
