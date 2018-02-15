package guepardoapps.lucahome.accesscontrol.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import guepardoapps.lucahome.accesscontrol.R;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.enums.AccessControlAlarmState;
import guepardoapps.lucahome.common.server.handler.IAccessControlDataHandler;
import guepardoapps.lucahome.common.utils.Logger;

public class AlarmStateViewController {
    private static final String Tag = AlarmStateViewController.class.getSimpleName();

    private Context _context;
    private ReceiverController _receiverController;

    private View _alarmStateIndicator;
    private TextView _alarmStateTextView;

    private boolean _isInitialized;

    private BroadcastReceiver _alarmStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Debug(Tag, "_alarmStateReceiver onReceive");
            AccessControlAlarmState currentState = (AccessControlAlarmState) intent.getSerializableExtra(IAccessControlDataHandler.BundleAlarmState);
            if (currentState != null) {
                switch (currentState) {
                    case AccessControlActive:
                        setAlarmState(R.xml.circle_blue, R.string.accessControlActive);
                        break;
                    case RequestCode:
                        setAlarmState(R.xml.circle_yellow, R.string.enterAccessCode);
                        break;
                    case AccessSuccessful:
                        setAlarmState(R.xml.circle_green, R.string.accessSuccessful);
                        break;
                    case AlarmActive:
                        setAlarmState(R.xml.circle_red, R.string.alarmActive);
                        break;
                    case AccessFailed:
                        setAlarmState(R.xml.circle_red, R.string.accessFailed);
                        break;
                    case Null:
                    default:
                        Logger.getInstance().Warning(Tag, "State not supported!");
                        break;
                }
            } else {
                Logger.getInstance().Warning(Tag, "model is null!");
            }
        }
    };

    private BroadcastReceiver _codeInvalidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Warning(Tag, "_codeInvalidReceiver onReceive");
            setAlarmState(R.xml.circle_red, R.string.enteredInvalidCode);
        }
    };

    private BroadcastReceiver _codeValidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Information(Tag, "_codeValidReceiver onReceive");
            setAlarmState(R.xml.circle_red, R.string.codeAccepted);
        }
    };

    public AlarmStateViewController(@NonNull Context context) {
        _context = context;
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        Logger.getInstance().Verbose(Tag, "onCreate");
        _alarmStateIndicator = ((Activity) _context).findViewById(R.id.alarmStateIndicator);
        _alarmStateTextView = ((Activity) _context).findViewById(R.id.alarmStateText);
    }

    public void onPause() {
        Logger.getInstance().Verbose(Tag, "onPause");
    }

    public void onResume() {
        Logger.getInstance().Verbose(Tag, "onResume");
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_alarmStateReceiver, new String[]{IAccessControlDataHandler.BroadcastAlarmState});
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

    private void setAlarmState(int alarmIndicator, int alarmMessage) {
        _alarmStateIndicator.setBackgroundResource(alarmIndicator);
        _alarmStateTextView.setText(alarmMessage);
    }
}
