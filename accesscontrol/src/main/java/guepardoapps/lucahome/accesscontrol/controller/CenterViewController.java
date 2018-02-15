package guepardoapps.lucahome.accesscontrol.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import guepardoapps.lucahome.accesscontrol.R;
import guepardoapps.lucahome.accesscontrol.common.Constants;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.IDownloadController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.enums.AccessControlAlarmState;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.server.handler.IAccessControlDataHandler;
import guepardoapps.lucahome.common.utils.Logger;

public class CenterViewController {
    private static final String Tag = CenterViewController.class.getSimpleName();

    private static final int MaxCharLength = 10;
    private static final int LoginSuccessfulTimeoutMs = 1500;

    private boolean _isInitialized;

    private Context _context;
    private DownloadController _downloadController;
    private ReceiverController _receiverController;

    private Button _activateAlarmButton;
    private TextView _alarmTextView;
    private RelativeLayout _inputRelativeLayout;
    private TextView _codeTextView;
    private TextView _loginNotificationTextView;

    private String _code;

    private Runnable _loginSuccessfulRunnable = () -> setVisibilities(View.VISIBLE, View.GONE, View.GONE);

    private BroadcastReceiver _alarmStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AccessControlAlarmState currentState = (AccessControlAlarmState) intent.getSerializableExtra(IAccessControlDataHandler.BundleAlarmState);
            if (currentState != null) {
                switch (currentState) {
                    case AccessControlActive:
                        setVisibilities(View.GONE, View.GONE, View.GONE);
                        break;
                    case RequestCode:
                        setVisibilities(View.GONE, View.GONE, View.VISIBLE);
                        _loginNotificationTextView.setText("");
                        _code = "";
                        setCodeText(_code.length());
                        break;
                    case AccessSuccessful:
                        _loginNotificationTextView.setText(R.string.codeAccepted);
                        _loginNotificationTextView.setTextColor(0xFF00FF00);
                        _code = "";
                        setCodeText(_code.length());
                        Handler loginSuccessfulHandler = new Handler();
                        loginSuccessfulHandler.postDelayed(_loginSuccessfulRunnable, LoginSuccessfulTimeoutMs);
                        break;
                    case AlarmActive:
                        setVisibilities(View.GONE, View.VISIBLE, View.GONE);
                        break;
                    case AccessFailed:
                        setVisibilities(View.GONE, View.GONE, View.VISIBLE);
                        _loginNotificationTextView.setText(R.string.enteredInvalidCode);
                        _loginNotificationTextView.setTextColor(0xFFFF0000);
                        _code = "";
                        setCodeText(_code.length());
                        break;
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

    public CenterViewController(@NonNull Context context) {
        _context = context;
        _downloadController = new DownloadController(_context);
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        Logger.getInstance().Verbose(Tag, "onCreate");
        _alarmTextView = ((Activity) _context).findViewById(R.id.alarmText);
        _inputRelativeLayout = ((Activity) _context).findViewById(R.id.buttonArrayLayout);
        _codeTextView = ((Activity) _context).findViewById(R.id.codeTextView);
        _loginNotificationTextView = ((Activity) _context).findViewById(R.id.loginNotificationTextView);
        initializeButtons();
        setVisibilities(View.GONE, View.GONE, View.GONE);
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

    private void initializeButtons() {
        _activateAlarmButton = ((Activity) _context).findViewById(R.id.activateAlarmButton);
        _activateAlarmButton.setOnClickListener(view -> {
            String requestUrl = "http://"
                    + SettingsController.getInstance().GetServerIp()
                    + guepardoapps.lucahome.common.constants.Constants.ActionPath
                    + Constants.UserName + "&password=" + Constants.UserPassPhrase
                    + "&action=" + LucaServerActionTypes.START_ACCESS_ALARM.toString();
            _downloadController.SendCommandToWebsiteAsync(requestUrl, IDownloadController.DownloadType.AccessAlarm, true);
        });

        ((Activity) _context).findViewById(R.id.code0Button).setOnClickListener(view -> addCharToCode("0"));
        ((Activity) _context).findViewById(R.id.code1Button).setOnClickListener(view -> addCharToCode("1"));
        ((Activity) _context).findViewById(R.id.code2Button).setOnClickListener(view -> addCharToCode("2"));
        ((Activity) _context).findViewById(R.id.code3Button).setOnClickListener(view -> addCharToCode("3"));
        ((Activity) _context).findViewById(R.id.code4Button).setOnClickListener(view -> addCharToCode("4"));
        ((Activity) _context).findViewById(R.id.code5Button).setOnClickListener(view -> addCharToCode("5"));
        ((Activity) _context).findViewById(R.id.code6Button).setOnClickListener(view -> addCharToCode("6"));
        ((Activity) _context).findViewById(R.id.code7Button).setOnClickListener(view -> addCharToCode("7"));
        ((Activity) _context).findViewById(R.id.code8Button).setOnClickListener(view -> addCharToCode("8"));
        ((Activity) _context).findViewById(R.id.code9Button).setOnClickListener(view -> addCharToCode("9"));

        ((Activity) _context).findViewById(R.id.codeResetButton).setOnClickListener(view -> {
            _code = "";
            setCodeText(_code.length());
        });

        ((Activity) _context).findViewById(R.id.codeOkButton).setOnClickListener(arg0 -> {
            if (_code.length() > 0 && _code.length() <= MaxCharLength) {
                String requestUrl = "http://"
                        + SettingsController.getInstance().GetServerIp()
                        + guepardoapps.lucahome.common.constants.Constants.ActionPath
                        + Constants.UserName + "&password=" + Constants.UserPassPhrase
                        + "&action=" + LucaServerActionTypes.SEND_ACCESS_CODE.toString() + _code;
                _downloadController.SendCommandToWebsiteAsync(requestUrl, IDownloadController.DownloadType.AccessAlarm, true);
            } else {
                Logger.getInstance().Warning(Tag, "Code has invalid format!");
            }
        });
    }

    private void setCodeText(int length) {
        StringBuilder codeHide = new StringBuilder();
        for (int count = 0; count < length; count++) {
            codeHide.append("*");
        }
        _codeTextView.setText(codeHide.toString());
    }

    private void addCharToCode(@NonNull String number) {
        if (_code.length() < MaxCharLength) {
            _code += number;
            setCodeText(_code.length());
        }
    }

    private void setVisibilities(int alarmButtonVisibility, int alarmTextVisibility, int inputLayoutVisibility) {
        _activateAlarmButton.setVisibility(alarmButtonVisibility);
        _alarmTextView.setVisibility(alarmTextVisibility);
        _inputRelativeLayout.setVisibility(inputLayoutVisibility);
    }
}
