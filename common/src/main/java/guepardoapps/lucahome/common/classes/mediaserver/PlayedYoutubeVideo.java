package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class PlayedYoutubeVideo implements IMediaServerClass {
    private static final String Tag = PlayedYoutubeVideo.class.getSimpleName();

    private UUID _uuid;
    private String _youtubeId;
    private String _title;
    private int _playCount;

    public PlayedYoutubeVideo(@NonNull UUID uuid, @NonNull String youtubeId, @NonNull String title, int playCount) {
        _uuid = uuid;
        _youtubeId = youtubeId;
        _title = title;
        _playCount = playCount;
    }

    public UUID GetUuid() {
        return _uuid;
    }

    public String GetYoutubeId() {
        return _youtubeId;
    }

    public String GetTitle() {
        return _title;
    }

    public int GetPlayCount() {
        return _playCount;
    }

    public void IncreasePlayCount() {
        _playCount++;
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

        PlayedYoutubeVideo tmpPlayedYoutubeVideo = new Gson().fromJson(communicationString, PlayedYoutubeVideo.class);
        _uuid = tmpPlayedYoutubeVideo.GetUuid();
        _youtubeId = tmpPlayedYoutubeVideo.GetYoutubeId();
        _title = tmpPlayedYoutubeVideo.GetTitle();
        _playCount = tmpPlayedYoutubeVideo.GetPlayCount();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"YoutubeId\":\"%s\",\"Title\":\"%s\",\"PlayCount\":%d}",
                Tag, _uuid, _youtubeId, _title, _playCount);
    }
}
