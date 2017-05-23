package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

public class YoutubeVideoDto {

    private static final String TAG = YoutubeVideoDto.class.getSimpleName();

    private String _youtubeId;
    private String _title;
    private String _description;

    public YoutubeVideoDto(
            @NonNull String youtubeId,
            @NonNull String title,
            @NonNull String description) {
        _youtubeId = youtubeId;
        _title = title;
        _description = description;
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

    @Override
    public String toString() {
        return TAG
                + ":{youtubeId: " + _youtubeId
                + ", title: " + _title
                + ", description: " + _description + "}";
    }
}
