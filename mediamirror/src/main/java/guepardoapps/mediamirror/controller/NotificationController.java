package guepardoapps.mediamirror.controller;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;

import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.models.CenterModel;

public class NotificationController {
    private static final String TAG = NotificationController.class.getSimpleName();

    private static final NotificationController SINGLETON = new NotificationController();

    private boolean _isInitialized;
    private boolean _screenEnabled;

    private Context _context;
    private DatabaseController _databaseController;
    private MediaVolumeController _mediaVolumeController;
    private ReceiverController _receiverController;

    private BroadcastReceiver _playBirthdaySongReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                return;
            }
            // TODO disable BubbleUpnp if playing ... or other media
        }
    };

    private BroadcastReceiver _playRadioStreamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                return;
            }
            // TODO disable BubbleUpnp if playing ... or other media

        }
    };

    private BroadcastReceiver _playVideoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                return;
            }
            // TODO disable BubbleUpnp if playing ... or other media
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = true;
        }
    };

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = false;
        }
    };

    private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                return;
            }

            CenterModel model = (CenterModel) intent.getSerializableExtra(Bundles.CENTER_MODEL);

            if (model != null) {
                if (model.IsYoutubeVisible() || model.IsRadioStreamVisible()) {
                    // TODO disable BubbleUpnp if playing ... or other media
                } else if (model.IsMediaNotificationVisible()) {
                    // TODO enable BubbleUpnp if available ... or other media ... or other handling :P
                }
            } else {
                Logger.getInstance().Warning(TAG, "model is null!");
            }
        }
    };

    private NotificationController() {
    }

    public static NotificationController getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        if (!_isInitialized) {
            _context = context;
            _databaseController = DatabaseController.getSingleton();
            _databaseController.Initialize(_context);
            _mediaVolumeController = MediaVolumeController.getInstance();
            _receiverController = new ReceiverController(_context);

            _receiverController.RegisterReceiver(_playBirthdaySongReceiver, new String[]{Broadcasts.PLAY_BIRTHDAY_SONG});
            _receiverController.RegisterReceiver(_playRadioStreamReceiver, new String[]{Broadcasts.PLAY_RADIO_STREAM});
            _receiverController.RegisterReceiver(_playVideoReceiver, new String[]{Broadcasts.PLAY_VIDEO});
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_updateViewReceiver, new String[]{Broadcasts.SHOW_CENTER_MODEL});

            _screenEnabled = true;
            _isInitialized = true;
        } else {
            Logger.getInstance().Warning(TAG, "Is ALREADY initialized!");
        }
    }

    public void Dispose() {
        _receiverController.Dispose();
        _isInitialized = false;
    }
}
