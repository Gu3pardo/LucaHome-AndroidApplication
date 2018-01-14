package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.enums.RadioStreams;
import guepardoapps.lucahome.common.interfaces.classes.IMediaServerClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class RadioStreamData implements IMediaServerClass, Serializable {
    private static final long serialVersionUID = 2691811435346000099L;

    private static final String TAG = RadioStreamData.class.getSimpleName();

    private RadioStreams _radioStream;
    private boolean _radioStreamIsPlaying;

    public RadioStreamData(
            @NonNull RadioStreams radioStream,
            boolean radioStreamIsPlaying) {
        _radioStream = radioStream;
        _radioStreamIsPlaying = radioStreamIsPlaying;
    }

    public RadioStreamData() {
        _radioStream = RadioStreams.BAYERN_3;
        _radioStreamIsPlaying = false;
    }

    public RadioStreams GetRadioStreamFeed() {
        return _radioStream;
    }

    public boolean GetRadioStreamIsPlaying() {
        return _radioStreamIsPlaying;
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

        RadioStreamData tempRadioStreamData = new Gson().fromJson(communicationString, RadioStreamData.class);
        _radioStream = tempRadioStreamData.GetRadioStreamFeed();
        _radioStreamIsPlaying = tempRadioStreamData.GetRadioStreamIsPlaying();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{%s:{RadioStream:%s},{RadioStreamIsPlaying:%s}}",
                TAG, _radioStream, _radioStreamIsPlaying);
    }
}
