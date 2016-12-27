package guepardoapps.lucahome.common.classes;

import java.io.Serializable;

import guepardoapps.lucahome.common.Constants;

public class Sound implements Serializable {

	private static final long serialVersionUID = 2488436194258046774L;

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
		return Constants.ACTION_PLAY_SOUND + _fileName;
	}

	public String toString() {
		return "{Sound: {FileName: " + _fileName + "};{IsPlaying: " + String.valueOf(_isPlaying) + "}}";
	}
}
