package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class YoutubeVideoDto implements Serializable {

    private static final String TAG = YoutubeVideoDto.class.getSimpleName();

    private String _youtubeId;
    private String _title;
    private String _description;
    private String _mediumImageUrl;

    public YoutubeVideoDto(
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
        return TAG
                + ":{youtubeId: " + _youtubeId
                + ", title: " + _title
                + ", description: " + _description
                + ", mediumImageUrl: " + _mediumImageUrl + "}";
    }
}
