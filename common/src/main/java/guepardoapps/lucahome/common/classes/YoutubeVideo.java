package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public class YoutubeVideo implements Serializable {
    private static final String TAG = YoutubeVideo.class.getSimpleName();

    private String _youtubeId;
    private String _title;
    private String _description;
    private String _mediumImageUrl;

    public YoutubeVideo(
            @NonNull String youtubeId,
            @NonNull String title,
            @NonNull String description,
            @NonNull String mediumImageUrl) {
        _youtubeId = youtubeId;
        _title = title;
        _description = description;
        _mediumImageUrl = mediumImageUrl;
    }

    public String GetYoutubeId() {
        return _youtubeId;
    }

    public String GetTitle() {
        return _title;
    }

    public String GetDescription() {
        return _description;
    }

    public String GetMediumImageUrl() {
        return _mediumImageUrl;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s:{YoutubeId: %s}{Title: %s}{Description: %s}{MediumImageUrl: %s}", TAG, _youtubeId, _title, _description, _mediumImageUrl);
    }
}
