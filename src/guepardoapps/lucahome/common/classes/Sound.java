package guepardoapps.lucahome.common.classes;

import java.io.Serializable;

import guepardoapps.lucahome.common.constants.ServerActions;

public class Sound implements Serializable {

	private static final long serialVersionUID = -7308774152731079183L;

	private String _fileName;
	private boolean _isPlaying;

	public Sound(String fileName, boolean isPlaying) {
		_fileName = fileName;
		_isPlaying = isPlaying;
	}

	public String GetFileName() {
		return _fileName;
	}

	public boolean GetIsPlaying() {
		return _isPlaying;
	}

	public void SetIsPlaying(boolean isPlaying) {
		_isPlaying = isPlaying;
	}

	public String GetCommandStart() {
		return ServerActions.PLAY_SOUND + _fileName;
	}

	public String toString() {
		return "{Sound: {FileName: " + _fileName + "};{IsPlaying: " + String.valueOf(_isPlaying) + "}}";
	}
}
