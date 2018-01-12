package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.interfaces.classes.IMediaServerClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PlayedYoutubeVideoData implements IMediaServerClass, Serializable {
    private static final long serialVersionUID = 2691811435346489566L;

    private static final String TAG = PlayedYoutubeVideoData.class.getSimpleName();
    private static final int COMMUNICATION_ENTRY_LENGTH = 3;

    private static final int INDEX_ID = 0;
    private static final int INDEX_YOUTUBE_ID = 1;
    private static final int INDEX_PLAY_COUNT = 2;

    public static final String SPLIT_CHAR = "---";
    public static final String END_CHAR = "===";

    private int _id;
    private String _youtubeId;
    private int _playCount;

    public PlayedYoutubeVideoData(
            int id,
            @NonNull String youtubeId,
            int playCount) {
        _id = id;
        _youtubeId = youtubeId;
        _playCount = playCount;
    }

    public PlayedYoutubeVideoData() {
        _id = 0;
        _youtubeId = "";
        _playCount = 0;
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

    @Override
    public String GetCommunicationString() {
        return String.format(Locale.getDefault(), "%s%s%s%s%d%s",
                _id, SPLIT_CHAR,
                _youtubeId, SPLIT_CHAR,
                _playCount, END_CHAR);
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

        _id = Integer.parseInt(entries[INDEX_ID]);
        _youtubeId = entries[INDEX_YOUTUBE_ID];
        _playCount = Integer.parseInt(entries[INDEX_PLAY_COUNT]);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{%s:{Id:%d},{YoutubeId:%s},{PlayCount:%d}}",
                TAG, _id, _youtubeId, _playCount);
    }
}
