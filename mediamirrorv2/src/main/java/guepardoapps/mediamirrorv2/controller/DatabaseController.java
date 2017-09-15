package guepardoapps.mediamirrorv2.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirrorv2.common.models.YoutubeDatabaseModel;
import guepardoapps.mediamirrorv2.database.DatabaseYoutubeIds;

public class DatabaseController {
    private static final DatabaseController SINGLETON = new DatabaseController();

    private static final String TAG = DatabaseController.class.getSimpleName();
    private Logger _logger;

    private boolean _isInitialized;

    private DatabaseYoutubeIds _databaseYoutubeIds;

    private DatabaseController() {
        _logger = new Logger(TAG);
        _logger.Debug(TAG + " created...");
    }

    public static DatabaseController getSingleton() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        _logger.Debug("Initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _databaseYoutubeIds = new DatabaseYoutubeIds(context);
        _databaseYoutubeIds.Open();

        _isInitialized = true;
    }

    public ArrayList<YoutubeDatabaseModel> GetYoutubeIds() {
        _logger.Debug("Loading youtube ids from database");
        return _databaseYoutubeIds.GetYoutubeIds();
    }

    public void SaveYoutubeId(@NonNull YoutubeDatabaseModel newEntry) {
        _logger.Debug("Saving new youtube id to database");
        _databaseYoutubeIds.CreateEntry(newEntry);
    }

    public void UpdateYoutubeId(@NonNull YoutubeDatabaseModel updateEntry) {
        _logger.Debug("Updating youtube id to database");
        _databaseYoutubeIds.Update(updateEntry);
    }

    public int GetHighestId() {
        _logger.Debug("Loading highest id from database");
        return _databaseYoutubeIds.GetHighestId();
    }

    public void DeleteYoutubeId(@NonNull YoutubeDatabaseModel deleteEntry) {
        _logger.Debug(String.format(Locale.GERMAN, "Deleting youtube id %s from database", deleteEntry));
        _databaseYoutubeIds.Delete(deleteEntry);
    }

    public void RemoveDatabase() {
        _logger.Debug("Removing database!");
        _databaseYoutubeIds.Remove();
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _databaseYoutubeIds.Close();
        _isInitialized = false;
    }
}
