package guepardoapps.mediamirror.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.TextView;

import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.models.DateModel;
import guepardoapps.mediamirror.interfaces.IViewController;

public class DateTimeViewController implements IViewController {
    private static final String TAG = DateTimeViewController.class.getSimpleName();
    private Logger _logger;

    private boolean _isInitialized;
    private boolean _screenEnabled;

    private Context _context;
    private ReceiverController _receiverController;

    private TextView _weekdayTextView;
    private TextView _dateTextView;
    private TextView _timeTextView;

    private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_updateViewReceiver onReceive");
            DateModel model = (DateModel) intent.getSerializableExtra(Bundles.DATE_MODEL);
            if (model != null) {
                _logger.Debug(model.toString());

                _weekdayTextView.setText(model.GetWeekday());
                _dateTextView.setText(model.GetDate());
                _timeTextView.setText(model.GetTime());
            } else {
                _logger.Warning("model is null!");
            }
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = true;

            _weekdayTextView = ((Activity) _context).findViewById(R.id.weekdayTextView);
            _dateTextView = ((Activity) _context).findViewById(R.id.dateTextView);
            _timeTextView = ((Activity) _context).findViewById(R.id.timeTextView);
        }
    };

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = false;
        }
    };

    public DateTimeViewController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
        _receiverController = new ReceiverController(_context);
    }

    @Override
    public void onCreate() {
        _logger.Debug("onCreate");

        _screenEnabled = true;

        _weekdayTextView = ((Activity) _context).findViewById(R.id.weekdayTextView);
        _dateTextView = ((Activity) _context).findViewById(R.id.dateTextView);
        _timeTextView = ((Activity) _context).findViewById(R.id.timeTextView);
    }

    @Override
    public void onStart() {
        _logger.Debug("onStart");
    }

    @Override
    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_updateViewReceiver, new String[]{Broadcasts.SHOW_DATE_MODEL});
            _isInitialized = true;
            _logger.Debug("Initializing!");
        } else {
            _logger.Warning("Is ALREADY initialized!");
        }
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
    }
}
