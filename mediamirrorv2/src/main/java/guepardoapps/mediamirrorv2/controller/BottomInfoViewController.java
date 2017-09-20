package guepardoapps.mediamirrorv2.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirrorv2.R;
import guepardoapps.mediamirrorv2.common.constants.Broadcasts;
import guepardoapps.mediamirrorv2.common.constants.Bundles;
import guepardoapps.mediamirrorv2.common.models.IpAddressModel;
import guepardoapps.mediamirrorv2.interfaces.IViewController;
import guepardoapps.mediamirrorv2.observer.SettingsContentObserver;

public class BottomInfoViewController implements IViewController {
    private static final String TAG = BottomInfoViewController.class.getSimpleName();
    private Logger _logger;

    private Context _context;
    private ReceiverController _receiverController;

    private SettingsContentObserver _settingsContentObserver;

    private View _batteryAlarmView;
    private TextView _batteryTextView;
    private TextView _ipAddressTextView;
    private TextView _volumeTextView;

    private BroadcastReceiver _batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_batteryInfoReceiver onReceive");
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (level != -1) {
                _batteryTextView.setText(String.format(Locale.getDefault(), "%d %%", level));
                _batteryAlarmView.setVisibility(View.VISIBLE);
                if (level > BatterySocketController.UPPER_BATTERY_LIMIT) {
                    _batteryAlarmView.setBackgroundResource(R.drawable.circle_green);
                } else if (level < BatterySocketController.LOWER_BATTERY_LIMIT) {
                    _batteryAlarmView.setBackgroundResource(R.drawable.circle_red);
                } else {
                    _batteryAlarmView.setBackgroundResource(R.drawable.circle_green);
                }
            } else {
                _batteryTextView.setText("Error!");
                _batteryAlarmView.setVisibility(View.GONE);
            }
        }
    };

    private BroadcastReceiver _ipAddressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_ipAddressReceiver onReceive");
            IpAddressModel model = (IpAddressModel) intent.getSerializableExtra(Bundles.IP_ADDRESS_MODEL);
            if (model != null) {
                _ipAddressTextView.setText(model.GetIpAddress());
            } else {
                _logger.Error("IpAddressModel is null!");
            }
        }
    };

    private BroadcastReceiver _volumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_volumeReceiver onReceive");
            SettingsContentObserver.VolumeChangeModel model =
                    (SettingsContentObserver.VolumeChangeModel) intent.getSerializableExtra(SettingsContentObserver.VOLUME_CHANGE_BUNDLE);
            if (model != null) {
                _volumeTextView.setText(String.format(Locale.getDefault(), "Vol.: %d", model.CurrentVolume));
            } else {
                _logger.Error("VolumeChangeModel is null!");
            }
        }
    };

    public BottomInfoViewController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
        _receiverController = new ReceiverController(_context);
    }

    @Override
    public void onCreate() {
        _logger.Debug("onCreate");

        _settingsContentObserver = new SettingsContentObserver(_context, new Handler());
        _context.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, _settingsContentObserver);

        _batteryAlarmView = ((Activity) _context).findViewById(R.id.batteryAlarmView);
        _batteryTextView = ((Activity) _context).findViewById(R.id.batteryTextView);
        _ipAddressTextView = ((Activity) _context).findViewById(R.id.ipAddressTextView);
        _volumeTextView = ((Activity) _context).findViewById(R.id.volumeTextView);

        AudioManager audioManager = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            _volumeTextView.setText(String.format(Locale.getDefault(), "Vol.: %d", audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)));
        } else {
            _logger.Error("audioManager is null!");
        }
    }

    @Override
    public void onStart() {
        _logger.Debug("onStart");
    }

    @Override
    public void onResume() {
        _logger.Debug("onResume");
        _receiverController.RegisterReceiver(_batteryInfoReceiver, new String[]{Intent.ACTION_BATTERY_CHANGED});
        _receiverController.RegisterReceiver(_ipAddressReceiver, new String[]{Broadcasts.SHOW_IP_ADDRESS_MODEL});
        _receiverController.RegisterReceiver(_volumeReceiver, new String[]{SettingsContentObserver.VOLUME_CHANGE_BROADCAST});
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _context.getContentResolver().unregisterContentObserver(_settingsContentObserver);
    }
}
