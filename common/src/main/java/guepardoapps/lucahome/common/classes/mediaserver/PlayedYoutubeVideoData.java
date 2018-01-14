package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.interfaces.classes.IMediaServerClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PlayedYoutubeVideoData implements IMediaServerClass, Serializable {
    private static final long serialVersionUID = 2691811435346489566L;

    private static final String TAG = PlayedYoutubeVideoData.class.getSimpleName();

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
        return new Gson().toJson(this);
    }

    @Override
    public void ParseCommunicationString(@NonNull String communicationString) throws Exception {
        if (communicationString.length() == 0) {
            throw new NullPointerException("CommunicationString may not be of length 0!");
        }
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "CommunicationString is %s", communicationString));

        PlayedYoutubeVideoData tempPlayedYoutubeVideoData = new Gson().fromJson(communicationString, PlayedYoutubeVideoData.class);
        _id = tempPlayedYoutubeVideoData.GetId();
        _youtubeId = tempPlayedYoutubeVideoData.GetYoutubeId();
        _playCount = tempPlayedYoutubeVideoData.GetPlayCount();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{%s:{Id:%d},{YoutubeId:%s},{PlayCount:%d}}",
                TAG, _id, _youtubeId, _playCount);
    }
}
