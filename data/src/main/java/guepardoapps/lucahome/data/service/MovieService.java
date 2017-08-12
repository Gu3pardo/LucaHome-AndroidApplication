package guepardoapps.lucahome.data.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.Movie;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.converter.JsonDataToMovieConverter;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.data.controller.DownloadController;
import guepardoapps.lucahome.data.controller.SettingsController;
import guepardoapps.lucahome.data.service.broadcasts.content.ObjectChangeFinishedContent;

public class MovieService {
    public static class MovieDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<Movie> MovieList;

        public MovieDownloadFinishedContent(SerializableList<Movie> movieList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            MovieList = movieList;
        }
    }

    public static final String MovieIntent = "MovieIntent";

    public static final String MovieDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.movie.download.finished";
    public static final String MovieDownloadFinishedBundle = "MovieDownloadFinishedBundle";

    public static final String MovieUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.movie.update.finished";
    public static final String MovieUpdateFinishedBundle = "MovieUpdateFinishedBundle";

    private static final MovieService SINGLETON = new MovieService();
    private boolean _isInitialized;

    private static final String TAG = MovieService.class.getSimpleName();
    private Logger _logger;

    private static final int TIMEOUT_MS = 30 * 60 * 1000;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_reloadListRunnable run");
            LoadMovieList();
            _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);
        }
    };

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private JsonDataToMovieConverter _jsonDataToMovieConverter;

    private SerializableList<Movie> _movieList = new SerializableList<>();

    private BroadcastReceiver _movieDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_movieDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Movie) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<Movie> movieList = _jsonDataToMovieConverter.GetList(contentResponse);
            if (movieList == null) {
                _logger.Error("Converted movieList is null!");
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug("Successfully converted movieList! Now broadcasting!");
            _movieList = movieList;

            _broadcastController.SendSerializableBroadcast(
                    MovieDownloadFinishedBroadcast,
                    MovieDownloadFinishedBundle,
                    new MovieDownloadFinishedContent(_movieList, true, Tools.CompressStringToByteArray("")));
        }
    };

    private BroadcastReceiver _movieUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_movieUpdateFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MovieUpdate) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedUpdateBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedUpdateBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    MovieUpdateFinishedBroadcast,
                    MovieUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadMovieList();
        }
    };

    private MovieService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static MovieService getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _receiverController.RegisterReceiver(_movieDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_movieUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _jsonDataToMovieConverter = new JsonDataToMovieConverter();

        _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);

        _isInitialized = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public SerializableList<Movie> GetMovieList() {
        return _movieList;
    }

    public ArrayList<String> GetTitleList() {
        ArrayList<String> titleList = new ArrayList<>();

        for (int index = 0; index < _movieList.getSize(); index++) {
            titleList.add(_movieList.getValue(index).GetTitle());
        }

        return titleList;
    }

    public ArrayList<String> GetGenreList() {
        ArrayList<String> genreList = new ArrayList<>();

        for (int index = 0; index < _movieList.getSize(); index++) {
            genreList.add(_movieList.getValue(index).GetGenre());
        }

        return genreList;
    }

    public ArrayList<String> GetDescriptionList() {
        ArrayList<String> descriptionList = new ArrayList<>();

        for (int index = 0; index < _movieList.getSize(); index++) {
            descriptionList.add(_movieList.getValue(index).GetDescription());
        }

        return descriptionList;
    }

    public Movie GetById(int id) {
        for (int index = 0; index < _movieList.getSize(); index++) {
            Movie entry = _movieList.getValue(index);

            if (entry.GetId() == id) {
                return entry;
            }
        }

        return null;
    }

    public SerializableList<Movie> FoundMovies(@NonNull String searchKey) {
        SerializableList<Movie> foundMovies = new SerializableList<>();

        for (int index = 0; index < _movieList.getSize(); index++) {
            Movie entry = _movieList.getValue(index);

            if (String.valueOf(entry.GetId()).contains(searchKey)
                    || entry.GetTitle().contains(searchKey)
                    || entry.GetGenre().contains(searchKey)
                    || entry.GetDescription().contains(searchKey)
                    //|| String.valueOf(entry.GetWatched()).contains(searchKey)
                    || String.valueOf(entry.GetRating()).contains(searchKey)) {
                foundMovies.addValue(entry);
            }
        }

        return foundMovies;
    }

    public void LoadMovieList() {
        _logger.Debug("LoadMovieList");

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast("No user");
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_MOVIES_REDUCED.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Movie, true);
    }

    public void UpdateMovie(Movie entry) {
        _logger.Debug(String.format(Locale.getDefault(), "UpdateMovie: Updating entry %s", entry));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedUpdateBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MovieUpdate, true);
    }

    private void sendFailedDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                MovieDownloadFinishedBroadcast,
                MovieDownloadFinishedBundle,
                new MovieDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedUpdateBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                MovieUpdateFinishedBroadcast,
                MovieUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }
}
