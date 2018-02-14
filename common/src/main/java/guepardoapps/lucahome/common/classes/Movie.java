package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"WeakerAccess"})
public class Movie implements ILucaClass {
    private static final String Tag = Movie.class.getSimpleName();

    public static final int MaxRating = 5;

    private UUID _uuid;
    private String _title;
    private String _genre;
    private String _description;
    private int _rating;
    private int _watchCount;

    public Movie(
            @NonNull UUID uuid,
            @NonNull String title,
            @NonNull String genre,
            @NonNull String description,
            int rating,
            int watchCount) {
        _uuid = uuid;
        _title = title;
        _genre = genre;
        _description = description;
        _rating = rating;
        _watchCount = watchCount;
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetRoomUuid not implemented for " + Tag);
    }

    public void SetTitle(@NonNull String title) {
        _title = title;
    }

    public String GetTitle() {
        return _title;
    }

    public void SetGenre(@NonNull String genre) {
        _genre = genre;
    }

    public String GetGenre() {
        return _genre;
    }

    public void SetDescription(@NonNull String description) {
        _description = description;
    }

    public String GetDescription() {
        return _description;
    }

    public void SetRating(int rating) {
        _rating = rating;
    }

    public int GetRating() {
        return _rating;
    }

    public int GetWatchCount() {
        return _watchCount;
    }

    public String GetRatingString() {
        return String.format(Locale.getDefault(), "%d/%d", _rating, MaxRating);
    }

    @Override
    public String GetCommandAdd() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetCommandAdd not implemented for " + Tag);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&genre=%s&description=%s&rating=%d&watchcount=%d",
                LucaServerActionTypes.UPDATE_MOVIE.toString(), _title, _genre, _description, _rating, _watchCount);
    }

    @Override
    public String GetCommandDelete() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetCommandDelete not implemented for " + Tag);
    }

    @Override
    public void SetIsOnServer(boolean isOnServer) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetIsOnServer not implemented for " + Tag);
    }

    @Override
    public boolean GetIsOnServer() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetIsOnServer not implemented for " + Tag);
    }

    @Override
    public void SetServerDbAction(@NonNull LucaServerDbAction lucaServerDbAction) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetServerDbAction not implemented for " + Tag);
    }

    @Override
    public LucaServerDbAction GetServerDbAction() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetServerDbAction not implemented for " + Tag);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Title\":\"%s\",\"Genre\":\"%s\",\"Description\":\"%s\",\"Rating\":%d,\"WatchCount\":%d}",
                Tag, _uuid, _title, _genre, _description, _rating, _watchCount);
    }
}
