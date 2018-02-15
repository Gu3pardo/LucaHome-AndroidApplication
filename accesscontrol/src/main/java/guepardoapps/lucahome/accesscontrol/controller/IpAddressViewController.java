package guepardoapps.lucahome.accesscontrol.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import guepardoapps.lucahome.accesscontrol.R;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.UserInformationController;
import guepardoapps.lucahome.common.enums.AccessControlAlarmState;
import guepardoapps.lucahome.common.server.handler.IAccessControlDataHandler;
import guepardoapps.lucahome.common.utils.Logger;

public class IpAddressViewController {
    private static final String Tag = IpAddressViewController.class.getSimpleName();

    private static final int _reloadTimeout = 15 * 60 * 1000;

    private Context _context;
    private ReceiverController _receiverController;
    private UserInformationController _userInformationController;

    private TextView _ipAddressTextView;

    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            updateIp();
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
        }
    };

    private boolean _isInitialized;

    private BroadcastReceiver _alarmStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Debug(Tag, "_alarmStateReceiver onReceive");
            AccessControlAlarmState currentState = (AccessControlAlarmState) intent.getSerializableExtra(IAccessControlDataHandler.BundleAlarmState);
            if (currentState != null) {
                switch (currentState) {
                    case AccessSuccessful:
                        _ipAddressTextView.setVisibility(View.VISIBLE);
                        break;
                    case AlarmActive:
                    case AccessControlActive:
                    case RequestCode:
                        _ipAddressTextView.setVisibility(View.INVISIBLE);
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

    public IpAddressViewController(@NonNull Context context) {
        _context = context;
        _receiverController = new ReceiverController(_context);
        _userInformationController = new UserInformationController(_context);
    }

    public void onCreate() {
        Logger.getInstance().Verbose(Tag, "onCreate");
        _ipAddressTextView = ((Activity) _context).findViewById(R.id.ipAddressTextView);
        updateIp();
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
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            _isInitialized = true;
        }
    }

    public void onDestroy() {
        Logger.getInstance().Verbose(Tag, "onDestroy");
        _receiverController.Dispose();
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _isInitialized = false;
    }

    private void updateIp() {
        String ip = _userInformationController.GetIp();
        _ipAddressTextView.setText(ip);
    }
}
