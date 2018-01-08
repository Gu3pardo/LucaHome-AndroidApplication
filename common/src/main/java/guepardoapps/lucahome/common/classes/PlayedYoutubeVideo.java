package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

@SuppressWarnings({"unused"})
public class PlayedYoutubeVideo implements Serializable {
    private static final long serialVersionUID = 2691811435346489566L;

    private static final String TAG = PlayedYoutubeVideo.class.getSimpleName();

    private int _id;
    private String _youtubeId;
    private int _playCount;

    public PlayedYoutubeVideo(
            int id,
            @NonNull String youtubeId,
            int playCount) {
        _id = id;
        _youtubeId = youtubeId;
        _playCount = playCount;
    }

    public int GetId() {
        return _id;
    }

    public String GetYoutubeId() {
        return _youtubeId;
    }

    public int GetPlayCount() {
        return _playCount;
    }

    public void IncreasePlayCount() {
        _playCount++;
    }

    public String GetCommunicationString() {
        return _id + "." + _youtubeId + "." + _playCount + ";";
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{%s:{Id:%d}{YoutubeId:%s}{PlayCount:%d}}",
                TAG, _id, _youtubeId, _playCount);
    }
}
