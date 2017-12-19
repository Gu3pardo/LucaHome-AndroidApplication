package guepardoapps.mediamirror.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.utils.Logger;

public class SettingsContentObserver extends ContentObserver {
    private static final String TAG = SettingsContentObserver.class.getSimpleName();
    private Logger _logger;

    public static final String VOLUME_CHANGE_BROADCAST = "guepardoapps.mediamirrorv2.observer.settingsvontentobserver.volume.change";
    public static final String VOLUME_CHANGE_BUNDLE = "VOLUME_CHANGE_BUNDLE";

    public static class VolumeChangeModel implements Serializable {
        public int CurrentVolume;
        public int PreviousVolume;
        public int Difference;
        public VolumeChangeState ChangeState;

        public VolumeChangeModel() {
            CurrentVolume = -1;
            PreviousVolume = -1;
            Difference = -1;
            ChangeState = VolumeChangeState.NULL;
        }
    }

    public enum VolumeChangeState implements Serializable {NULL, INCREASED, DECREASED}

    private int _previousVolume;

    private Context _context;
    private BroadcastController _broadcastController;

    public SettingsContentObserver(@NonNull Context context, @NonNull Handler handler) {
        super(handler);

        _logger = new Logger(TAG);

        _context = context;
        _broadcastController = new BroadcastController(_context);

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            _previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        } else {
            _logger.Error("audioManager is null!");
        }
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        AudioManager audioManager = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int delta = _previousVolume - currentVolume;

            VolumeChangeModel volumeChangeModel = new VolumeChangeModel();
            volumeChangeModel.CurrentVolume = currentVolume;
            volumeChangeModel.PreviousVolume = _previousVolume;
            volumeChangeModel.Difference = delta;

            if (delta > 0) {
                _logger.Debug("Volume decreased");
                _previousVolume = currentVolume;
                volumeChangeModel.ChangeState = VolumeChangeState.DECREASED;
            } else if (delta < 0) {
                _logger.Debug("Volume increased");
                _previousVolume = currentVolume;
                volumeChangeModel.ChangeState = VolumeChangeState.INCREASED;
            } else {
                _logger.Debug("Volume did not change");
                _previousVolume = currentVolume;
                volumeChangeModel.ChangeState = VolumeChangeState.NULL;
            }

            _broadcastController.SendSerializableBroadcast(VOLUME_CHANGE_BROADCAST, VOLUME_CHANGE_BUNDLE, volumeChangeModel);
        } else {
            _logger.Error("audioManager is null!");
        }
    }
}