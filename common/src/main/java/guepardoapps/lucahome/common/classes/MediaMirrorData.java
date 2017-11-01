package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.lucahome.common.enums.MediaServerSelection;
import guepardoapps.lucahome.common.enums.RadioStreams;

public class MediaMirrorData implements Serializable {
    private static final long serialVersionUID = -6336573950675481913L;

    private static final String TAG = MediaMirrorData.class.getSimpleName();

    private MediaServerSelection _mediaServerSelection;

    private int _batteryLevel;

    private String _socketName;
    private boolean _socketState;

    private int _volume;

    private String _youtubeId;
    private boolean _youtubeIsPlaying;
    private int _youtubeVideoCurrentPlayTime;
    private int _youtubeVideoDuration;
    private ArrayList<PlayedYoutubeVideo> _playedYoutubeIds;

    private boolean _radioStreamIsPlaying;
    private int _radioStreamId;

    private boolean _sleepTimerEnabled;
    private int _countDownSec;

    private String _serverVersion;

    private int _screenBrightness;

    public MediaMirrorData(
            @NonNull MediaServerSelection mediaServerSelection,
            int batteryLevel,
            @NonNull String socketName,
            boolean socketState,
            int volume,
            @NonNull String youtubeId,
            boolean youtubeIsPlaying,
            int youtubeVideoCurrentPlayTime,
            int youtubeVideoDuration,
            @NonNull ArrayList<PlayedYoutubeVideo> playedYoutubeIds,
            boolean radioStreamIsPlaying,
            int radioStreamId,
            boolean sleepTimerEnabled,
            int countDownSec,
            @NonNull String serverVersion,
            int screenBrightness) {
        _mediaServerSelection = mediaServerSelection;

        _batteryLevel = batteryLevel;

        _socketName = socketName;
        _socketState = socketState;

        _volume = volume;

        _youtubeId = youtubeId;
        _youtubeIsPlaying = youtubeIsPlaying;
        _youtubeVideoCurrentPlayTime = youtubeVideoCurrentPlayTime;
        _youtubeVideoDuration = youtubeVideoDuration;
        _playedYoutubeIds = playedYoutubeIds;

        _radioStreamIsPlaying = radioStreamIsPlaying;
        _radioStreamId = radioStreamId;

        _sleepTimerEnabled = sleepTimerEnabled;
        _countDownSec = countDownSec;

        _serverVersion = serverVersion;

        _screenBrightness = screenBrightness;
    }

    public MediaMirrorData(
            @NonNull MediaServerSelection mediaServerSelection,
            int batteryLevel,
            int volume,
            @NonNull String youtubeId,
            @NonNull String serverVersion) {
        this(mediaServerSelection,
                batteryLevel,
                "",
                false,
                volume,
                youtubeId,
                false,
                -1,
                -1,
                new ArrayList<>(),
                false,
                RadioStreams.PLANET_RADIO_LIVE.GetId(),
                false,
                -1,
                serverVersion,
                -1);
    }

    public MediaServerSelection GetMediaServerSelection() {
        return _mediaServerSelection;
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

    public ArrayList<PlayedYoutubeVideo> GetPlayedYoutubeIds() {
        return _playedYoutubeIds;
    }

    public boolean IsRadioStreamPlaying() {
        return _radioStreamIsPlaying;
    }

    public int GetRadioStreamId() {
        return _radioStreamId;
    }

    public boolean IsSleepTimerEnabled() {
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
                Locale.getDefault(),
                "{%s:{MediaServerSelection:%s}{BatteryLevel:%s}{Socket:{Name:%s}{State:%s}}{Volume:%s}{YoutubeId:%s}{IsYoutubePlaying:%s}{YoutubeVideoCurrentPlayTime:%s}{YoutubeVideoDuration:%s}{PlayedYoutubeIds:%s}{IsRadioStreamPlaying:%s}{RadioStreamId:%d}{SleepTimer:{State:%s}{Countdown:%s}}{ServerVersion:%s}{ScreenBrightness:%s}}",
                TAG,
                _mediaServerSelection,
                _batteryLevel,
                _socketName,
                _socketState,
                _volume,
                _youtubeId,
                _youtubeIsPlaying,
                _youtubeVideoCurrentPlayTime,
                _youtubeVideoDuration,
                _playedYoutubeIds,
                _radioStreamIsPlaying,
                _radioStreamId,
                _sleepTimerEnabled,
                _countDownSec,
                _serverVersion,
                _screenBrightness);
    }
}
