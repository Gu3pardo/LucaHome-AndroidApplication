package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Locale;

import guepardoapps.lucahome.common.classes.RadioStream;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class RadioStreamData implements IMediaServerClass {
    private static final String Tag = RadioStreamData.class.getSimpleName();

    private RadioStream _radioStream;
    private boolean _radioStreamIsPlaying;

    public RadioStreamData(@NonNull RadioStream radioStream, boolean radioStreamIsPlaying) {
        _radioStream = radioStream;
        _radioStreamIsPlaying = radioStreamIsPlaying;
    }

    public RadioStreamData() {
        this(new RadioStream(), false);
    }

    public RadioStream GetRadioStream() {
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
    public void ParseCommunicationString(@NonNull String communicationString) throws NullPointerException {
        if (communicationString.length() == 0) {
            throw new NullPointerException("CommunicationString may not be of length 0!");
        }
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "CommunicationString is %s", communicationString));

        RadioStreamData tmpRadioStreamData = new Gson().fromJson(communicationString, RadioStreamData.class);
        _radioStream = tmpRadioStreamData.GetRadioStream();
        _radioStreamIsPlaying = tmpRadioStreamData.GetRadioStreamIsPlaying();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"RadioStream\":\"%s\",\"RadioStreamIsPlaying\":\"%s\"}",
                Tag, _radioStream, _radioStreamIsPlaying);
    }
}
