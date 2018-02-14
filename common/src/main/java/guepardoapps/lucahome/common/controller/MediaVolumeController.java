package guepardoapps.lucahome.common.controller;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.common.observer.SettingsContentObserver;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"deprecation", "WeakerAccess"})
public class MediaVolumeController implements IMediaVolumeController {
    private static final String Tag = MediaVolumeController.class.getSimpleName();

    private BroadcastController _broadcastController;

    private static final int VolumeChangeStep = 1;

    private AudioManager _audioManager;
    private int _volume = -1;
    private int _maxVolume = -1;
    private boolean _mute;

    private boolean _isInitialized;

    private static final MediaVolumeController SINGLETON_CONTROLLER = new MediaVolumeController();

    public static MediaVolumeController getInstance() {
        return SINGLETON_CONTROLLER;
    }

    private MediaVolumeController() {
    }

    @Override
    public void Initialize(@NonNull Context context) {
        if (!_isInitialized) {
            _broadcastController = new BroadcastController(context);

            _audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (_audioManager != null) {
                _volume = _audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                _maxVolume = _audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                _mute = _audioManager.isStreamMute(AudioManager.STREAM_MUSIC);

                sendVolumeBroadcast();

                _isInitialized = true;
            } else {
                Logger.getInstance().Error(Tag, "AudioManager is null!");
            }
        }
    }

    @Override
    public boolean IsInitialized() {
        return _isInitialized;
    }

    @Override
    public boolean Dispose() {
        if (!_isInitialized) {
            Logger.getInstance().Error(Tag, "not initialized!");
            return false;
        }
        _broadcastController = null;
        return true;
    }

    @Override
    public boolean IncreaseVolume() {
        if (!_isInitialized) {
            Logger.getInstance().Error(Tag, "not initialized!");
            return false;
        }

        if (_mute) {
            Logger.getInstance().Warning(Tag, "Audio stream is muted!");
            return false;
        }

        if (_volume >= _maxVolume) {
            Logger.getInstance().Warning(Tag, "Current volume is already _maxVolume: " + String.valueOf(_maxVolume));
            return false;
        }

        int newVolume = _volume + VolumeChangeStep;
        if (newVolume > _maxVolume) {
            newVolume = _maxVolume;
        }

        _audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        sendVolumeBroadcast();
        return true;
    }

    @Override
    public boolean DecreaseVolume() {
        if (!_isInitialized) {
            Logger.getInstance().Error(Tag, "not initialized!");
            return false;
        }

        if (_mute) {
            Logger.getInstance().Warning(Tag, "Audio stream is muted!");
            return false;
        }

        if (_volume <= 0) {
            Logger.getInstance().Warning(Tag, "Current volume is already 0!");
            return false;
        }

        int newVolume = _volume - VolumeChangeStep;
        if (newVolume < 0) {
            newVolume = 0;
        }

        _audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        sendVolumeBroadcast();
        return true;
    }

    @Override
    public boolean SetVolume(int volume) {
        if (!_isInitialized) {
            Logger.getInstance().Error(Tag, "not initialized!");
            return false;
        }

        if (_volume == 0) {
            _audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        } else {
            _audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            _audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        }

        sendVolumeBroadcast();
        return true;
    }

    @Override
    public boolean MuteVolume() {
        if (!_isInitialized) {
            Logger.getInstance().Error(Tag, "not initialized!");
            return false;
        }

        if (_mute) {
            Logger.getInstance().Warning(Tag, "Audio stream is already muted!");
            return false;
        }

        _audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        sendVolumeBroadcast();

        return true;
    }

    @Override
    public boolean UnMuteVolume() {
        if (!_isInitialized) {
            Logger.getInstance().Error(Tag, "not initialized!");
            return false;
        }

        if (!_mute) {
            Logger.getInstance().Warning(Tag, "Audio stream is already unmuted!");
            return false;
        }

        _audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        _audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, _volume, 0);
        sendVolumeBroadcast();

        return true;
    }

    @Override
    public int GetMaxVolume() {
        return _maxVolume;
    }

    @Override
    public int GetVolume() {
        return _volume;
    }

    private void sendVolumeBroadcast() {
        _volume = _audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        _mute = _audioManager.isStreamMute(AudioManager.STREAM_MUSIC);

        SettingsContentObserver.VolumeChangeModel volumeChangeModel = new SettingsContentObserver.VolumeChangeModel(_volume, -1, -1, SettingsContentObserver.VolumeChangeState.Null);

        String volumeText;
        if (_mute) {
            volumeText = "mute";
        } else {
            volumeText = String.valueOf(_volume);
        }

        _broadcastController.SendStringBroadcast(CurrentVolumeBroadcast, CurrentVolumeBundle, volumeText);
        _broadcastController.SendSerializableBroadcast(SettingsContentObserver.BroadcastVolumeChange, SettingsContentObserver.BundleVolumeChange, volumeChangeModel);
    }
}
