package guepardoapps.lucahome.accesscontrol.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import guepardoapps.lucahome.accesscontrol.R;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.enums.AccessControlAlarmState;
import guepardoapps.lucahome.common.server.handler.IAccessControlDataHandler;
import guepardoapps.lucahome.common.utils.Logger;

public class BatteryViewController {
    private static final String Tag = BatteryViewController.class.getSimpleName();

    private static final int BatteryLevelWarning = 15;
    private static final int BatteryLevelCritical = 5;

    private Context _context;
    private ReceiverController _receiverController;

    private View _batteryAlarmView;
    private TextView _batteryValueTextView;

    private Drawable _circleGreen;
    private Drawable _circleYellow;
    private Drawable _circleRed;

    private boolean _isInitialized;

    private BroadcastReceiver _alarmStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AccessControlAlarmState currentState = (AccessControlAlarmState) intent.getSerializableExtra(IAccessControlDataHandler.BundleAlarmState);
            if (currentState != null) {
                switch (currentState) {
                    case AccessSuccessful:
                        _batteryValueTextView.setVisibility(View.VISIBLE);
                        _batteryAlarmView.setVisibility(View.VISIBLE);
                        break;
                    case AlarmActive:
                    case AccessControlActive:
                    case RequestCode:
                        _batteryValueTextView.setVisibility(View.INVISIBLE);
                        _batteryAlarmView.setVisibility(View.INVISIBLE);
                        break;
                    case AccessFailed:
                    case Null:
                    default:
                        Logger.getInstance().Warning(Tag, "State not supported!");
                        break;
                }
            }
        }
    };

    private BroadcastReceiver _batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            _batteryValueTextView.setText(String.format("%s%%", level));

            try {
                if (level > BatteryLevelWarning) {
                    if (_circleGreen == null) {
                        tryToReadXmlCircle();
                    }
                    _batteryAlarmView.setBackground(_circleGreen);

                } else if (level <= BatteryLevelWarning && level > BatteryLevelCritical) {
                    if (_circleYellow == null) {
                        tryToReadXmlCircle();
                    }
                    _batteryAlarmView.setBackground(_circleYellow);

                } else {
                    if (_circleRed == null) {
                        tryToReadXmlCircle();
                    }
                    _batteryAlarmView.setBackground(_circleRed);

                }
            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.toString());
            }
        }
    };

    private BroadcastReceiver _codeInvalidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Warning(Tag, "_codeInvalidReceiver onReceive");
        }
    };

    private BroadcastReceiver _codeValidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Information(Tag, "_codeValidReceiver onReceive");
        }
    };

    public BatteryViewController(@NonNull Context context) {
        _context = context;
        _receiverController = new ReceiverController(_context);
        tryToReadXmlCircle();
    }

    public void onCreate() {
        Logger.getInstance().Verbose(Tag, "onCreate");
        _batteryAlarmView = ((Activity) _context).findViewById(R.id.batteryAlarm);
        _batteryValueTextView = ((Activity) _context).findViewById(R.id.batteryTextView);
    }

    public void onPause() {
        Logger.getInstance().Verbose(Tag, "onPause");
    }

    public void onResume() {
        Logger.getInstance().Verbose(Tag, "onResume");
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_alarmStateReceiver, new String[]{IAccessControlDataHandler.BroadcastAlarmState});
            _receiverController.RegisterReceiver(_batteryInfoReceiver, new String[]{Intent.ACTION_BATTERY_CHANGED});
            _receiverController.RegisterReceiver(_codeInvalidReceiver, new String[]{IAccessControlDataHandler.BroadcastEnteredCodeInvalid});
            _receiverController.RegisterReceiver(_codeValidReceiver, new String[]{IAccessControlDataHandler.BroadcastEnteredCodeValid});
            _isInitialized = true;
        }
    }

    public void onDestroy() {
        Logger.getInstance().Verbose(Tag, "onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
    }

    private void tryToReadXmlCircle() {
        try {
            Resources resources = _context.getResources();
            _circleGreen = Drawable.createFromXml(resources, resources.getXml(R.xml.circle_green));
            _circleYellow = Drawable.createFromXml(resources, resources.getXml(R.xml.circle_yellow));
            _circleRed = Drawable.createFromXml(resources, resources.getXml(R.xml.circle_red));
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
        }
    }
}
