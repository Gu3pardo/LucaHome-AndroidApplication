package guepardoapps.lucahome.dto;

import java.io.Serializable;
import java.util.ArrayList;

import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.enums.RaspberrySelection;

public class SoundDto implements Serializable {

	private static final long serialVersionUID = 8261461743775735447L;
	
	private static final String TAG = SoundDto.class.getName();
	private LucaHomeLogger _logger;

	private String _fileName;
	private boolean _isPlaying;
	private int _volume;
	private RaspberrySelection _raspberrySelection;
	private ArrayList<String> _fileNames;

	public SoundDto(String fileName, boolean isPlaying, int volume, RaspberrySelection raspberrySelection,
			ArrayList<String> fileNames) {
		_logger = new LucaHomeLogger(TAG);

		_fileName = fileName;
		_isPlaying = isPlaying;
		_volume = volume;
		_raspberrySelection = raspberrySelection;
		_fileNames = fileNames;

		_logger.Debug("New SoundDto: " + toString());
	}

	public String GetFileName() {
		return _fileName;
	}

	public boolean GetIsPlaying() {
		return _isPlaying;
	}

	public int GetVolume() {
		return _volume;
	}

	public RaspberrySelection GetRaspberrySelection() {
		return _raspberrySelection;
	}

	public ArrayList<String> GetFileNames() {
		return _fileNames;
	}

	public String GetCommandStart() {
		return Constants.ACTION_PLAY_SOUND + _fileName;
	}

	public String toString() {
		return "{SoundDto: {FileName: " + _fileName + "};{IsPlaying: " + String.valueOf(_isPlaying) + "};{Volume: "
				+ String.valueOf(_volume) + "};{RaspberrySelection: " + _raspberrySelection.toString()
				+ "};{FileNames: " + _fileNames.toString() + "}}";
	}
}
