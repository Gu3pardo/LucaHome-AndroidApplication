package guepardoapps.lucahome.common.services;

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.Movie;

public interface IMovieService extends ILucaService<Movie> {
    String MovieDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.movie.download.finished";
    String MovieUpdateFinishedBroadcast = "guepardoapps.lucahome.common.services.movie.update.finished";

    String MovieDownloadFinishedBundle = "MovieDownloadFinishedBundle";
    String MovieUpdateFinishedBundle = "MovieUpdateFinishedBundle";

    ArrayList<String> GetTitleList();

    ArrayList<String> GetGenreList();

    ArrayList<String> GetDescriptionList();
}
