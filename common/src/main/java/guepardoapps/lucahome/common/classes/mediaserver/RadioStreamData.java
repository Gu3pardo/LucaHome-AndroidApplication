package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Locale;

import guepardoapps.lucahome.common.enums.RadioStreamType;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class RadioStreamData implements IMediaServerClass {
    private static final String Tag = RadioStreamData.class.getSimpleName();

    private RadioStreamType _radioStreamType;
    private boolean _radioStreamIsPlaying;

    public RadioStreamData(@NonNull RadioStreamType radioStreamType, boolean radioStreamIsPlaying) {
        _radioStreamType = radioStreamType;
        _radioStreamIsPlaying = radioStreamIsPlaying;
    }

    public RadioStreamData() {
        this(RadioStreamType.I_LOVE_RADIO, false);
    }

    public RadioStreamType GetRadioStreamType() {
        return _radioStreamType;
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
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "CommunicationString is %s", communicationString));

        RadioStreamData tmpRadioStreamData = new Gson().fromJson(communicationString, RadioStreamData.class);
        _radioStreamType = tmpRadioStreamData.GetRadioStreamType();
        _radioStreamIsPlaying = tmpRadioStreamData.GetRadioStreamIsPlaying();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"RadioStreamType\":\"%s\",\"RadioStreamIsPlaying\":\"%s\"}",
                Tag, _radioStreamType, _radioStreamIsPlaying);
    }
}
