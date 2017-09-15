package guepardoapps.mediamirrorv2.controller;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirrorv2.R;
import guepardoapps.mediamirrorv2.interfaces.IViewController;

public class BottomInfoViewController implements IViewController {
    private static final String TAG = BottomInfoViewController.class.getSimpleName();
    private Logger _logger;

    private Context _context;
    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private View _batteryAlarmView;
    private TextView _batteryTextView;
    private TextView _ipAddressTextView;
    private TextView _serverVersionTextView;
    private TextView _volumeTextView;
    private TextView _currentPlayingTextView;

    public BottomInfoViewController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);
    }

    @Override
    public void onCreate() {
        _logger.Debug("onCreate");

        _batteryAlarmView = ((Activity) _context).findViewById(R.id.batteryAlarmView);
        _batteryTextView = ((Activity) _context).findViewById(R.id.batteryTextView);
        _ipAddressTextView = ((Activity) _context).findViewById(R.id.ipAddressTextView);
        _serverVersionTextView = ((Activity) _context).findViewById(R.id.serverVersionTextView);
        _volumeTextView = ((Activity) _context).findViewById(R.id.volumeTextView);
        _currentPlayingTextView = ((Activity) _context).findViewById(R.id.currentPlayingTextView);
    }

    @Override
    public void onStart() {
        _logger.Debug("onStart");

    }

    @Override
    public void onResume() {
        _logger.Debug("onResume");

    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
        _receiverController.Dispose();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
    }
}
