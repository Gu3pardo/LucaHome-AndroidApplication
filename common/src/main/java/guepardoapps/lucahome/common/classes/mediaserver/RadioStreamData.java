package guepardoapps.lucahome.common.classes.mediaserver;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.enums.RadioStreams;
import guepardoapps.lucahome.common.interfaces.classes.IMediaServerClass;

@SuppressWarnings({"unused", "WeakerAccess"})
public class RadioStreamData implements IMediaServerClass, Serializable {
    private static final long serialVersionUID = 2691811435346000099L;

    private static final String TAG = RadioStreamData.class.getSimpleName();
    private static final int COMMUNICATION_ENTRY_LENGTH = 2;

    private static final int INDEX_RADIO_STREAM_ID = 0;
    private static final int INDEX_RADIO_STREAM_IS_PLAYING = 1;

    public static final String SPLIT_CHAR = "::";
    public static final String END_CHAR = ";";

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
        return String.format(Locale.getDefault(), "%d%s%s%s",
                _radioStream.GetId(), SPLIT_CHAR,
                _radioStreamIsPlaying, END_CHAR);
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

        _radioStream = RadioStreams.GetById(Integer.parseInt(entries[INDEX_RADIO_STREAM_ID]));
        _radioStreamIsPlaying = entries[INDEX_RADIO_STREAM_IS_PLAYING].contains("1");
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{%s:{RadioStream:%s},{RadioStreamIsPlaying:%s}}",
                TAG, _radioStream, _radioStreamIsPlaying);
    }
}
