package guepardoapps.lucahome.accesscontrol.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.accesscontrol.R;
import guepardoapps.lucahome.accesscontrol.common.Constants;
import guepardoapps.lucahome.common.classes.MediaServer;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.IDownloadController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.enums.AccessControlAlarmState;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.enums.MediaServerActionType;
import guepardoapps.lucahome.common.server.handler.IAccessControlDataHandler;
import guepardoapps.lucahome.common.services.MediaServerClientService;
import guepardoapps.lucahome.common.utils.Logger;

public class CountdownViewController {
    private static final String Tag = CountdownViewController.class.getSimpleName();

    private static final String TimeFormat = "%02d:%02d:%02d";
    private static final int CountdownTime = 15 * 1000;
    private static final int CountdownInterval = 10;

    private Context _context;
    private DownloadController _downloadController;
    private ReceiverController _receiverController;

    private TextView _countdownTextView;

    private CountDownTimer _countDownTimer;

    private boolean _isInitialized;
    private boolean _accessControlActive;
    private boolean _countdownActive;

    private BroadcastReceiver _alarmStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AccessControlAlarmState currentState = (AccessControlAlarmState) intent.getSerializableExtra(IAccessControlDataHandler.BundleAlarmState);
            if (currentState != null) {
                switch (currentState) {
                    case AccessControlActive:
                        _countdownTextView.setVisibility(View.GONE);
                        _accessControlActive = true;
                        _countdownActive = false;
                        break;

                    case RequestCode:
                        _countdownTextView.setVisibility(View.VISIBLE);
                        _countdownTextView.setTextColor(Color.WHITE);
                        _countDownTimer.start();
                        _accessControlActive = true;
                        _countdownActive = true;
                        break;

                    case AccessSuccessful:
                        _countdownTextView.setVisibility(View.GONE);
                        _countDownTimer.cancel();
                        _accessControlActive = false;
                        _countdownActive = false;
                        break;

                    case AlarmActive:
                        _countdownTextView.setVisibility(View.VISIBLE);
                        _countdownTextView.setTextColor(Color.RED);
                        _countdownTextView.setText(R.string.countdownZero);
                        _accessControlActive = true;
                        _countdownActive = true;
                        break;

                    case AccessFailed:
                    case Null:
                    default:
                        Logger.getInstance().Warning(Tag, "State not supported!");
                        _accessControlActive = true;
                        _countdownActive = true;
                        break;
                }
            }
        }
    };

    private BroadcastReceiver _codeInvalidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Warning(Tag, "_codeInvalidReceiver onReceive");
            _countdownTextView.setTextColor(Color.YELLOW);
        }
    };

    private BroadcastReceiver _codeValidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Information(Tag, "_codeValidReceiver onReceive");
            _countdownTextView.setTextColor(Color.GREEN);
            _countDownTimer.cancel();
        }
    };

    public CountdownViewController(@NonNull Context context) {
        _context = context;
        _downloadController = new DownloadController(_context);
        _receiverController = new ReceiverController(_context);

        _countDownTimer = new CountDownTimer(CountdownTime, CountdownInterval) {
            public void onTick(long millisUntilFinished) {
                _countdownTextView.setText(String.format(Locale.getDefault(),
                        TimeFormat,
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toMillis(millisUntilFinished) - TimeUnit.MILLISECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished))));
            }

            public void onFinish() {
                _countdownTextView.setTextColor(Color.RED);
                _countdownTextView.setText(R.string.countdownZero);
                sendAlarm();
            }
        };
    }

    public void onCreate() {
        Logger.getInstance().Verbose(Tag, "onCreate");
        _countdownTextView = ((Activity) _context).findViewById(R.id.countdownText);
        _countdownTextView.setVisibility(View.GONE);
    }

    public void onPause() {
        Logger.getInstance().Verbose(Tag, "onPause");
        if (_accessControlActive || _countdownActive) {
            Logger.getInstance().Warning(Tag, "Do not pause me while access control or countdown is active!");
            Toasty.error(_context, "Alarm activated!", Toast.LENGTH_LONG).show();
            sendAlarm();
        }
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
        if (_accessControlActive || _countdownActive) {
            Logger.getInstance().Warning(Tag, "Do not stop me while access control or countdown is active!");
            Toasty.error(_context, "Alarm activated!", Toast.LENGTH_LONG).show();
            sendAlarm();
        }
        _receiverController.Dispose();
        _isInitialized = false;
    }

    private void sendAlarm() {
        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + guepardoapps.lucahome.common.constants.Constants.ActionPath
                + Constants.UserName + "&password=" + Constants.UserPassPhrase
                + "&action=" + LucaServerActionTypes.PLAY_ACCESS_ALARM.toString();
        _downloadController.SendCommandToWebsiteAsync(requestUrl, IDownloadController.DownloadType.AccessAlarm, true);

        for (MediaServer mediaServer : MediaServerClientService.getInstance().GetDataList()) {
            MediaServerClientService.getInstance().SetActiveMediaServer(mediaServer);
            MediaServerClientService.getInstance().SendCommand(MediaServerActionType.PLAY_ACCESS_ALARM.toString(), "");
        }
    }
}
