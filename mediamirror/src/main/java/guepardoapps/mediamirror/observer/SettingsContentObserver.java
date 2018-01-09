package guepardoapps.mediamirror.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.utils.Logger;

@SuppressWarnings("WeakerAccess")
public class SettingsContentObserver extends ContentObserver {
    private static final String TAG = SettingsContentObserver.class.getSimpleName();

    public static final String VOLUME_CHANGE_BROADCAST = "guepardoapps.mediamirror.observer.settingsvontentobserver.volume.change";
    public static final String VOLUME_CHANGE_BUNDLE = "VOLUME_CHANGE_BUNDLE";

    public static class VolumeChangeModel implements Serializable {
        public int CurrentVolume;
        public int PreviousVolume;
        public int Difference;
        public VolumeChangeState ChangeState;

        public VolumeChangeModel(int currentVolume, int previousVolume, int difference, @NonNull VolumeChangeState changeState) {
            CurrentVolume = currentVolume;
            PreviousVolume = previousVolume;
            Difference = difference;
            ChangeState = changeState;
        }
    }

    public enum VolumeChangeState implements Serializable {NULL, INCREASED, DECREASED}

    private int _previousVolume;

    private Context _context;
    private BroadcastController _broadcastController;

    public SettingsContentObserver(@NonNull Context context, @NonNull Handler handler) {
        super(handler);

        _context = context;
        _broadcastController = new BroadcastController(_context);

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            _previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        } else {
            Logger.getInstance().Error(TAG, "audioManager is null!");
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

            VolumeChangeModel volumeChangeModel = new VolumeChangeModel(currentVolume, _previousVolume, delta, VolumeChangeState.NULL);

            if (delta > 0) {
                _previousVolume = currentVolume;
                volumeChangeModel.ChangeState = VolumeChangeState.DECREASED;
            } else if (delta < 0) {
                _previousVolume = currentVolume;
                volumeChangeModel.ChangeState = VolumeChangeState.INCREASED;
            } else {
                _previousVolume = currentVolume;
                volumeChangeModel.ChangeState = VolumeChangeState.NULL;
            }

            _broadcastController.SendSerializableBroadcast(VOLUME_CHANGE_BROADCAST, VOLUME_CHANGE_BUNDLE, volumeChangeModel);
        } else {
            Logger.getInstance().Error(TAG, "audioManager is null!");
        }
    }
}