package guepardoapps.mediamirror.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.mediaserver.PlayedYoutubeVideoData;
import guepardoapps.mediamirror.database.DatabaseYoutubeIds;

@SuppressWarnings({"unused", "WeakerAccess"})
public class DatabaseController {
    private static final String TAG = DatabaseController.class.getSimpleName();

    private static final DatabaseController SINGLETON = new DatabaseController();

    private boolean _isInitialized;

    private DatabaseYoutubeIds _databaseYoutubeIds;

    private DatabaseController() {
    }

    public static DatabaseController getSingleton() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        if (_isInitialized) {
            Logger.getInstance().Warning(TAG, "Already initialized!");
            return;
        }

        _databaseYoutubeIds = new DatabaseYoutubeIds(context);
        _databaseYoutubeIds.Open();

        _isInitialized = true;
    }

    public ArrayList<PlayedYoutubeVideoData> GetYoutubeIds() {
        return _databaseYoutubeIds.GetYoutubeIds();
    }

    public void SaveYoutubeId(@NonNull PlayedYoutubeVideoData newEntry) {
        _databaseYoutubeIds.CreateEntry(newEntry);
    }

    public void UpdateYoutubeId(@NonNull PlayedYoutubeVideoData updateEntry) {
        _databaseYoutubeIds.Update(updateEntry);
    }

    public int GetHighestId() {
        return _databaseYoutubeIds.GetHighestId();
    }

    public void DeleteYoutubeId(@NonNull PlayedYoutubeVideoData deleteEntry) {
        _databaseYoutubeIds.Delete(deleteEntry);
    }

    public void RemoveDatabase() {
        _databaseYoutubeIds.Remove();
    }

    public void Dispose() {
        _databaseYoutubeIds.Close();
        _isInitialized = false;
    }
}
