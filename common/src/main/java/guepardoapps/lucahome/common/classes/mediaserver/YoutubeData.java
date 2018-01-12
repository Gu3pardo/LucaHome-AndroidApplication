package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.interfaces.classes.IMediaServerClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class YoutubeData implements IMediaServerClass, Serializable {
    private static final long serialVersionUID = 1384098950675481913L;

    private static final String TAG = YoutubeData.class.getSimpleName();
    private static final int COMMUNICATION_ENTRY_LENGTH = 5;

    private static final int INDEX_IS_YOUTUBE_PLAYING = 0;
    private static final int INDEX_CURRENT_YOUTUBE_ID = 1;
    private static final int INDEX_CURRENT_YOUTUBE_POSITION = 2;
    private static final int INDEX_CURRENT_YOUTUBE_DURATION = 3;
    private static final int INDEX_PLAYED_YOUTUBE_VIDEOS = 4;

    public static final String SPLIT_CHAR = "::";
    public static final String END_CHAR = ";";

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
        return String.format(Locale.getDefault(), "%s%s%s%s%d%s%d%s%s%s",
                _isYoutubePlaying, SPLIT_CHAR,
                _currentYoutubeId, SPLIT_CHAR,
                _currentYoutubeVideoPosition, SPLIT_CHAR,
                _currentYoutubeVideoDuration, SPLIT_CHAR,
                getPlayYoutubeVideosString(), END_CHAR);
    }

    @Override
    public void ParseCommunicationString(@NonNull String communicationString) throws Exception {
        if (communicationString.length() == 0) {
            throw new NullPointerException("CommunicationString may not be of length 0!");
        }
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "CommunicationString is %s", communicationString));
        communicationString = communicationString.replace(END_CHAR, "");

        String[] entries = communicationString.split(SPLIT_CHAR);
        if (entries.length != COMMUNICATION_ENTRY_LENGTH) {
            throw new IndexOutOfBoundsException(String.format(Locale.getDefault(), "Invalid length %d for entries in %s!", entries.length, TAG));
        }

        _isYoutubePlaying = entries[INDEX_IS_YOUTUBE_PLAYING].contains("1");
        _currentYoutubeId = entries[INDEX_CURRENT_YOUTUBE_ID];
        _currentYoutubeVideoPosition = Integer.parseInt(entries[INDEX_CURRENT_YOUTUBE_POSITION]);
        _currentYoutubeVideoDuration = Integer.parseInt(entries[INDEX_CURRENT_YOUTUBE_DURATION]);

        String playedYoutubeVideoDataString = entries[INDEX_PLAYED_YOUTUBE_VIDEOS];
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "playedYoutubeVideoDataString is %s", playedYoutubeVideoDataString));

        ArrayList<PlayedYoutubeVideoData> playedYoutubeVideos = new ArrayList<>();
        String[] playYoutubeDataEntries = playedYoutubeVideoDataString.split(PlayedYoutubeVideoData.END_CHAR);
        for (String playYoutubeDataEntry : playYoutubeDataEntries) {
            PlayedYoutubeVideoData playedYoutubeVideo = new PlayedYoutubeVideoData();
            playedYoutubeVideo.ParseCommunicationString(playYoutubeDataEntry);
            playedYoutubeVideos.add(playedYoutubeVideo);
        }
        _playedYoutubeVideos = playedYoutubeVideos;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "{%s:{IsYoutubePlaying:%s},{CurrentYoutubeId:%s},{CurrentYoutubeVideoPosition:%d},{CurrentYoutubeVideoDuration:%d},{PlayedYoutubeVideos:%s}}",
                TAG, _isYoutubePlaying, _currentYoutubeId, _currentYoutubeVideoPosition, _currentYoutubeVideoDuration, _playedYoutubeVideos);
    }
}
