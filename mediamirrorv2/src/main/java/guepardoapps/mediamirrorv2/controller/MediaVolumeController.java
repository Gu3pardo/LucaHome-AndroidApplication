package guepardoapps.mediamirrorv2.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirrorv2.common.constants.Broadcasts;
import guepardoapps.mediamirrorv2.common.constants.Bundles;

public class MediaVolumeController {
    private static final MediaVolumeController SINGLETON_CONTROLLER = new MediaVolumeController();

    private static final String TAG = MediaVolumeController.class.getSimpleName();
    private Logger _logger;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private static final int VOLUME_CHANGE_STEP = 1;

    private AudioManager _audioManager;
    private int _currentVolume = -1;
    private int _maxVolume = -1;
    private boolean _mute;

    private boolean _isInitialized;

    public static MediaVolumeController getInstance() {
        return SINGLETON_CONTROLLER;
    }

    private MediaVolumeController() {
        _logger = new Logger(TAG);
        _logger.Debug(TAG + " created...");
    }

    public void Initialize(@NonNull Context context) {
        if (!_isInitialized) {
            _broadcastController = new BroadcastController(context);
            _receiverController = new ReceiverController(context);
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});

            _audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            _currentVolume = _audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            _maxVolume = _audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            _mute = _audioManager.isStreamMute(AudioManager.STREAM_MUSIC);

            sendVolumeBroadcast();

            _isInitialized = true;
        }
    }

    public boolean IncreaseVolume() {
        _logger.Debug("IncreaseVolume");

        if (!_isInitialized) {
            _logger.Error("not initialized!");
            return false;
        }

        if (_mute) {
            _logger.Warning("Audio stream is muted!");
            return false;
        }

        if (_currentVolume >= _maxVolume) {
            _logger.Warning("Current volume is already _maxVolume: " + String.valueOf(_maxVolume));
            return false;
        }

        int newVolume = _currentVolume + VOLUME_CHANGE_STEP;
        if (newVolume > _maxVolume) {
            newVolume = _maxVolume;
        }

        _logger.Debug("newVolume: " + String.valueOf(newVolume));
        _audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        sendVolumeBroadcast();

        return true;
    }

    public boolean DecreaseVolume() {
        _logger.Debug("DecreaseVolume");

        if (!_isInitialized) {
            _logger.Error("not initialized!");
            return false;
        }

        if (_mute) {
            _logger.Warning("Audio stream is muted!");
            return false;
        }

        if (_currentVolume <= 0) {
            _logger.Warning("Current volume is already 0!");
            return false;
        }

        int newVolume = _currentVolume - VOLUME_CHANGE_STEP;
        if (newVolume < 0) {
            newVolume = 0;
        }

        _logger.Debug("newVolume: " + String.valueOf(newVolume));
        _audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        sendVolumeBroadcast();

        return true;
    }

    public boolean SetVolume(int volume) {
        _logger.Debug("SetVolume: " + String.valueOf(volume));

        if (!_isInitialized) {
            _logger.Error("not initialized!");
            return false;
        }

        if (_mute) {
            _logger.Warning("Audio stream is muted!");
            UnMuteVolume();
        }

        if (volume < 0) {
            volume = 0;
        }
        if (volume > _maxVolume) {
            volume = _maxVolume;
        }

        _logger.Debug("newVolume: " + String.valueOf(volume));
        _audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        sendVolumeBroadcast();

        return true;
    }

    @SuppressWarnings("deprecation")
    public boolean MuteVolume() {
        _logger.Debug("MuteVolume");

        if (!_isInitialized) {
            _logger.Error("not initialized!");
            return false;
        }

        if (_mute) {
            _logger.Warning("Audio stream is already muted!");
            return false;
        }

        _audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        _mute = _audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
        sendVolumeBroadcast();

        return true;
    }

    @SuppressWarnings("deprecation")
    public boolean UnMuteVolume() {
        _logger.Debug("UnMuteVolume");

        if (!_isInitialized) {
            _logger.Error("not initialized!");
            return false;
        }

        if (!_mute) {
            _logger.Warning("Audio stream is already unmuted!");
            return false;
        }

        _audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        _mute = _audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
        sendVolumeBroadcast();

        return true;
    }

    public int GetMaxVolume() {
        return _maxVolume;
    }

    public int GetCurrentVolume() {
        return _currentVolume;
    }

    public boolean IsInitialized() {
        return _isInitialized;
    }

    @SuppressWarnings("deprecation")
    public boolean SetCurrentVolume(int currentVolume) {
        _logger.Debug("SetCurrentVolume: " + String.valueOf(currentVolume));

        if (!_isInitialized) {
            _logger.Error("not initialized!");
            return false;
        }

        if (currentVolume == 0) {
            _audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            _mute = _audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
        } else {
            _audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            _mute = _audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
        }
        _currentVolume = currentVolume;

        return true;
    }

    public boolean Dispose() {
        _logger.Debug("Dispose");

        if (!_isInitialized) {
            _logger.Error("not initialized!");
            return false;
        }

        _receiverController.Dispose();

        _broadcastController = null;
        _receiverController = null;

        return true;
    }

    private void sendVolumeBroadcast() {
        _currentVolume = _audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        _mute = _audioManager.isStreamMute(AudioManager.STREAM_MUSIC);

        String volumeText;
        if (_mute) {
            volumeText = "mute";
        } else {
            volumeText = String.valueOf(_currentVolume);
        }

        _broadcastController.SendStringBroadcast(Broadcasts.SHOW_VOLUME_MODEL, Bundles.VOLUME_MODEL, volumeText);
    }

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendVolumeBroadcast();
        }
    };
}
