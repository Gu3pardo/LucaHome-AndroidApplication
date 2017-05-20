package guepardoapps.library.lucahome.common.dto;

import java.io.Serializable;
import java.util.ArrayList;

import guepardoapps.library.lucahome.common.enums.MediaMirrorSelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

public class MediaMirrorViewDto implements Serializable {

	private static final long serialVersionUID = -6336573950675481913L;

	private static final String TAG = MediaMirrorViewDto.class.getSimpleName();
	private LucaHomeLogger _logger;

	private MediaMirrorSelection _mediaMirrorSelection;

	private int _batteryLevel;

	private String _socketName;
	private boolean _socketState;

	private int _volume;

	private String _youtubeId;
	private boolean _youtubeIsPlaying;
	private int _youtubeVideoCurrentPlayTime;
	private int _youtubeVideoDuration;
	private ArrayList<PlayedYoutubeVideoDto> _playedYoutubeIds;

	private boolean _sleepTimerEnabled;
	private int _countDownSec;

	private String _serverVersion;

	private int _screenBrightness;

	public MediaMirrorViewDto(MediaMirrorSelection mediaMirrorSelection, int batteryLevel, String socketName,
			boolean socketState, int volume, String youtubeId, boolean youtubeIsPlaying,
			int youtubeVideoCurrentPlayTime, int youtubeVideoDuration,
			ArrayList<PlayedYoutubeVideoDto> playedYoutubeIds, boolean sleepTimerEnabled, int countDownSec,
			String serverVersion, int screenBrightness) {
		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("Creating...");

		_mediaMirrorSelection = mediaMirrorSelection;

		_batteryLevel = batteryLevel;

		_socketName = socketName;
		_socketState = socketState;

		_volume = volume;

		_youtubeId = youtubeId;
		_youtubeIsPlaying = youtubeIsPlaying;
		_youtubeVideoCurrentPlayTime = youtubeVideoCurrentPlayTime;
		_youtubeVideoDuration = youtubeVideoDuration;
		_playedYoutubeIds = playedYoutubeIds;

		_sleepTimerEnabled = sleepTimerEnabled;
		_countDownSec = countDownSec;

		_serverVersion = serverVersion;

		_screenBrightness = screenBrightness;
	}

	public MediaMirrorViewDto(MediaMirrorSelection mediaMirrorSelection, int batteryLevel, int volume, String youtubeId,
			String serverVersion) {
		this(mediaMirrorSelection, batteryLevel, "", false, volume, youtubeId, false, -1, -1,
				new ArrayList<PlayedYoutubeVideoDto>(), false, -1, serverVersion, -1);
	}

	public MediaMirrorSelection GetMediaMirrorSelection() {
		return _mediaMirrorSelection;
	}

	public int GetBatteryLevel() {
		return _batteryLevel;
	}

	public String GetSocketName() {
		return _socketName;
	}

	public boolean GetSocketState() {
		return _socketState;
	}

	public String GetYoutubeId() {
		return _youtubeId;
	}

	public boolean IsYoutubePlaying() {
		return _youtubeIsPlaying;
	}

	public int GetYoutubeVideoCurrentPlayTime() {
		return _youtubeVideoCurrentPlayTime;
	}

	public int GetYoutubeVideoDuration() {
		return _youtubeVideoDuration;
	}

	public int GetVolume() {
		return _volume;
	}

	public ArrayList<PlayedYoutubeVideoDto> GetPlayedYoutubeIds() {
		return _playedYoutubeIds;
	}

	public boolean GetSleepTimerEnabled() {
		return _sleepTimerEnabled;
	}

	public int GetCountDownSec() {
		return _countDownSec;
	}

	public String GetServerVersion() {
		return _serverVersion;
	}

	public int GetScreenBrightness() {
		return _screenBrightness;
	}

	@Override
	public String toString() {
		return String.format(
				"{%s:{MediaMirrorSelection:%s}{BatteryLevel:%s}{Socket:{Name:%s}{State:%s}}{Volume:%s}{YoutubeId:%s}{IsYoutubePlaying:%s}{YoutubeVideoCurrentPlayTime:%s}{YoutubeVideoDuration:%s}{PlayedYoutubeIds:%s}{SleepTimer:{State:%s}{Countdown:%s}}{ServerVersion:%s}{ScreenBrightness:%s}}",
				TAG, _mediaMirrorSelection, _batteryLevel, _socketName, _socketState, _volume, _youtubeId,
				_youtubeIsPlaying, _youtubeVideoCurrentPlayTime, _youtubeVideoDuration, _playedYoutubeIds,
				_sleepTimerEnabled, _countDownSec, _serverVersion, _screenBrightness);
	}
}
