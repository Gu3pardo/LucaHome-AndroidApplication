package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.interfaces.classes.IMediaServerClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class YoutubeData implements IMediaServerClass, Serializable {
    private static final long serialVersionUID = 1384098950675481913L;

    private static final String TAG = YoutubeData.class.getSimpleName();

    private boolean _isYoutubePlaying;
    private String _currentYoutubeId;
    private int _currentYoutubeVideoPosition;
    private int _currentYoutubeVideoDuration;
    private ArrayList<PlayedYoutubeVideoData> _playedYoutubeVideos;

    public YoutubeData(
            boolean isYoutubePlaying,
            @NonNull String currentYoutubeId,
            int currentYoutubeVideoPosition,
            int currentYoutubeVideoDuration,
            @NonNull ArrayList<PlayedYoutubeVideoData> playedYoutubeVideos) {
        _isYoutubePlaying = isYoutubePlaying;
        _currentYoutubeId = currentYoutubeId;
        _currentYoutubeVideoPosition = currentYoutubeVideoPosition;
        _currentYoutubeVideoDuration = currentYoutubeVideoDuration;
        _playedYoutubeVideos = playedYoutubeVideos;
    }

    public YoutubeData() {
        _isYoutubePlaying = false;
        _currentYoutubeId = "";
        _currentYoutubeVideoPosition = 0;
        _currentYoutubeVideoDuration = 0;
        _playedYoutubeVideos = new ArrayList<>();
    }

    public boolean IsYoutubePlaying() {
        return _isYoutubePlaying;
    }

    public String GetCurrentYoutubeId() {
        return _currentYoutubeId;
    }

    public int GetCurrentYoutubeVideoPosition() {
        return _currentYoutubeVideoPosition;
    }

    public int GetCurrentYoutubeVideoDuration() {
        return _currentYoutubeVideoDuration;
    }

    public ArrayList<PlayedYoutubeVideoData> GetPlayedYoutubeVideos() {
        return _playedYoutubeVideos;
    }

    private String getPlayYoutubeVideosString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (PlayedYoutubeVideoData playedYoutubeVideoData : _playedYoutubeVideos) {
            stringBuilder.append(playedYoutubeVideoData.GetCommunicationString());
        }
        return stringBuilder.toString();
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
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "CommunicationString is %s", communicationString));

        YoutubeData youtubeData = new Gson().fromJson(communicationString, YoutubeData.class);
        _isYoutubePlaying = youtubeData.IsYoutubePlaying();
        _currentYoutubeId = youtubeData.GetCurrentYoutubeId();
        _currentYoutubeVideoPosition = youtubeData.GetCurrentYoutubeVideoPosition();
        _currentYoutubeVideoDuration = youtubeData.GetCurrentYoutubeVideoDuration();
        _playedYoutubeVideos = youtubeData.GetPlayedYoutubeVideos();
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "{%s:{IsYoutubePlaying:%s},{CurrentYoutubeId:%s},{CurrentYoutubeVideoPosition:%d},{CurrentYoutubeVideoDuration:%d},{PlayedYoutubeVideos:%s}}",
                TAG, _isYoutubePlaying, _currentYoutubeId, _currentYoutubeVideoPosition, _currentYoutubeVideoDuration, _playedYoutubeVideos);
    }
}
