package guepardoapps.mediamirror.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.LucaBirthday;
import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.interfaces.IViewController;

public class BirthdayViewController implements IViewController {
    private static final String TAG = BirthdayViewController.class.getSimpleName();
    private Logger _logger;

    private static final int INVERT_TIME = 1000;

    private boolean _isInitialized;
    private boolean _screenEnabled;

    private Context _context;
    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private View _birthdayAlarmView;
    private TextView _birthdayTextView;

    private boolean _hasBirthday;
    private Handler _updateBirthdayAlarmHandler = new Handler();

    private BroadcastReceiver _dateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_dateChangedReceiver onReceive");
            final String action = intent.getAction();

            if (action == null) {
                _logger.Error("action is null!");
                return;
            }

            if (action.equals(Intent.ACTION_DATE_CHANGED)) {
                _logger.Debug("ACTION_DATE_CHANGED");
                _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_BIRTHDAY_UPDATE);
            }
        }
    };

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = false;
            _updateBirthdayAlarmHandler.removeCallbacks(_updateBirthdayAlarmViewRunnable);
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_BIRTHDAY_UPDATE);
        }
    };

    private BroadcastReceiver _updateBirthdayViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateBirthdayViewReceiver onReceive");
            SerializableList<LucaBirthday> birthdayList = new SerializableList<>();

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("Receiving Serializable object from intent and bundle birthdayModel and trying to cast it to SerializableList<BirthdayDto>");
            SerializableList<?> serializableExtra = (SerializableList<?>) intent.getSerializableExtra(Bundles.BIRTHDAY_MODEL);
            if (serializableExtra != null) {
                for (int index = 0; index < serializableExtra.getSize(); index++) {
                    if (!(serializableExtra.getValue(index) instanceof LucaBirthday)) {
                        _logger.Error(String.format(Locale.getDefault(), "Value at index %d is not an instance of BirthdayDto: %s", index, serializableExtra.getValue(index)));
                        return;
                    } else {
                        birthdayList.addValue((LucaBirthday) serializableExtra.getValue(index));
                    }
                }
            }

            _logger.Debug("birthdayList: " + birthdayList.toString());

            for (int index = 0; index < birthdayList.getSize(); index++) {
                LucaBirthday entry = birthdayList.getValue(index);
                _logger.Debug(String.format(Locale.getDefault(), "Birthday: %s", entry));

                if (entry != null) {
                    if (entry.HasBirthday()) {
                        _logger.Debug("Entry has today birthday!");
                        _hasBirthday = true;
                        _birthdayTextView.setText(entry.GetNotificationBody());
                        _birthdayAlarmView.setVisibility(View.VISIBLE);
                        checkPlayBirthdaySong(entry);
                    } else {
                        _hasBirthday = false;
                        String text = String.format(Locale.getDefault(), "%s: %s %d", entry.GetName(), entry.GetDate().DDMMYYYY(), entry.GetAge());
                        _birthdayTextView.setText(text);
                        _birthdayAlarmView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    _logger.Warning("Birthday entry is null!");
                    _hasBirthday = false;
                    _birthdayTextView.setText("");
                    _birthdayAlarmView.setVisibility(View.INVISIBLE);
                }
            }

            _updateBirthdayAlarmHandler.removeCallbacks(_updateBirthdayAlarmViewRunnable);
            _updateBirthdayAlarmHandler.postDelayed(_updateBirthdayAlarmViewRunnable, INVERT_TIME);
        }

        private void checkPlayBirthdaySong(@NonNull LucaBirthday entry) {
            _logger.Debug("checkPlayBirthdaySong");
            if (entry.GetName().contains("Sandra Huber") || entry.GetName().contains("Jonas Schubert")) {
                _broadcastController.SendSimpleBroadcast(Broadcasts.PLAY_BIRTHDAY_SONG);
            }
        }
    };

    private Runnable _updateBirthdayAlarmViewRunnable = new Runnable() {
        private boolean _invert;

        public void run() {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            if (_hasBirthday) {
                if (_invert) {
                    _birthdayAlarmView.setBackgroundResource(R.drawable.circle_red);
                } else {
                    _birthdayAlarmView.setBackgroundResource(R.drawable.circle_yellow);
                }
            }

            _invert = !_invert;
            _updateBirthdayAlarmHandler.postDelayed(this, INVERT_TIME);
        }
    };

    public BirthdayViewController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);
    }

    @Override
    public void onCreate() {
        _logger.Debug("onCreate");

        _birthdayAlarmView = ((Activity) _context).findViewById(R.id.birthdayAlarmView);
        _birthdayTextView = ((Activity) _context).findViewById(R.id.birthdayTextView);

        _screenEnabled = true;
    }

    @Override
    public void onStart() {
        _logger.Debug("onStart");
    }

    @Override
    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _logger.Debug("Initializing!");

            _receiverController.RegisterReceiver(_dateChangedReceiver, new String[]{Intent.ACTION_DATE_CHANGED});
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_updateBirthdayViewReceiver, new String[]{Broadcasts.SHOW_BIRTHDAY_MODEL});

            _isInitialized = true;
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
