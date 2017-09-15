package guepardoapps.mediamirrorv2.common.models;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class YoutubeDatabaseModel implements Serializable {
    private static final long serialVersionUID = 1167790558127364021L;

    private static final String TAG = YoutubeDatabaseModel.class.getSimpleName();

    private int _id;
    private String _youtubeId;
    private int _playCount;

    public YoutubeDatabaseModel(
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
        return TAG
                + ":{_id:" + _id
                + ";_youtubeId:" + _youtubeId
                + ";_playCount:" + String.valueOf(_playCount) + "}";
    }
}
