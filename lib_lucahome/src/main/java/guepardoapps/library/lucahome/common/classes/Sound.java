package guepardoapps.library.lucahome.common.classes;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.library.lucahome.common.constants.ServerActions;

public class Sound implements Serializable {

    private static final long serialVersionUID = -7308774152731079183L;

    @SuppressWarnings("unused")
    private static final String TAG = Sound.class.getSimpleName();

    private String _fileName;
    private boolean _isPlaying;

    public Sound(String fileName, boolean isPlaying) {
        _fileName = fileName;
        _isPlaying = isPlaying;
    }

    public String GetFileName() {
        return _fileName;
    }

    public boolean IsPlaying() {
        return _isPlaying;
    }

    public void SetIsPlaying(boolean isPlaying) {
        _isPlaying = isPlaying;
    }

    public String GetCommandStart() {
        return ServerActions.PLAY_SOUND + _fileName;
    }

    public String toString() {
        return String.format(Locale.GERMAN, "{Sound: {FileName: %s};{IsPlaying: %s}}", _fileName, _isPlaying);
    }
}
