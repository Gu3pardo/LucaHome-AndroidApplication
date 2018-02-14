package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Locale;

import guepardoapps.lucahome.common.classes.mediaserver.IMediaServerClass;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class YoutubeVideo implements IMediaServerClass {
    private static final String Tag = YoutubeVideo.class.getSimpleName();

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
    public String GetCommunicationString() {
        return new Gson().toJson(this);
    }

    @Override
    public void ParseCommunicationString(@NonNull String communicationString) throws Exception {
        if (communicationString.length() == 0) {
            throw new NullPointerException("CommunicationString may not be of length 0!");
        }
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "CommunicationString is %s", communicationString));

        YoutubeVideo tempYoutubeVideoData = new Gson().fromJson(communicationString, YoutubeVideo.class);
        _youtubeId = tempYoutubeVideoData.GetYoutubeId();
        _title = tempYoutubeVideoData.GetTitle();
        _description = tempYoutubeVideoData.GetDescription();
        _mediumImageUrl = tempYoutubeVideoData.GetMediumImageUrl();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"YoutubeId\":\"%s\",\"Title\":\"%s\",\"Description\":\"%s\",\"MediumImageUrl\":\"%s\"}",
                Tag, _youtubeId, _title, _description, _mediumImageUrl);
    }
}
