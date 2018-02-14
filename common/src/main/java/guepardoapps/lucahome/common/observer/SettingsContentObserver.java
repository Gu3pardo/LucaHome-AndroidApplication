package guepardoapps.lucahome.common.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings("WeakerAccess")
public class SettingsContentObserver extends ContentObserver implements ISettingsContentObserver {
    private static final String Tag = SettingsContentObserver.class.getSimpleName();

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

    private Context _context;
    private BroadcastController _broadcastController;
    private int _previousVolume;

    public SettingsContentObserver(@NonNull Context context, @NonNull Handler handler) {
        super(handler);

        _context = context;
        _broadcastController = new BroadcastController(_context);

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            _previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        } else {
            Logger.getInstance().Error(Tag, "audioManager is null!");
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

            VolumeChangeModel volumeChangeModel = new VolumeChangeModel(currentVolume, _previousVolume, delta, VolumeChangeState.Null);

            if (delta > 0) {
                _previousVolume = currentVolume;
                volumeChangeModel.ChangeState = VolumeChangeState.Decreased;
            } else if (delta < 0) {
                _previousVolume = currentVolume;
                volumeChangeModel.ChangeState = VolumeChangeState.Increased;
            } else {
                _previousVolume = currentVolume;
                volumeChangeModel.ChangeState = VolumeChangeState.Null;
            }

            _broadcastController.SendSerializableBroadcast(BroadcastVolumeChange, BundleVolumeChange, volumeChangeModel);
        } else {
            Logger.getInstance().Error(Tag, "audioManager is null!");
        }
    }
}