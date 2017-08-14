package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.service.UserService;
import guepardoapps.lucahome.service.MainService;
import guepardoapps.lucahome.service.NavigationService;

public class BootActivity extends AppCompatActivity {
    private static final String TAG = BootActivity.class.getSimpleName();
    private Logger _logger;

    private boolean _loginAttempt;

    /**
     * Initiate UI
     */
    private ProgressBar _percentProgressBar;
    private TextView _percentProgressTextView;

    /**
     * ReceiverController to register and unregister from broadcasts of the UserService
     */
    private ReceiverController _receiverController;

    /**
     * NavigationService manages navigation between activities
     */
    private NavigationService _navigationService;

    /**
     * UserService manages the handles for the user: validation, etc.
     */
    private UserService _userService;

    /**
     * Binder for MainService
     */
    private MainService _mainServiceBinder;

    /**
     * ServiceConnection for MainServiceBinder
     */
    private ServiceConnection _mainServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            _logger.Debug("onServiceConnected");
            _mainServiceBinder = ((MainService.MainServiceBinder) binder).getService();

            if (!_userService.IsAnUserSaved()) {
                _loginAttempt = true;
                NavigationService.NavigationResult navigationResult = _navigationService.NavigateToActivity(BootActivity.this, LoginActivity.class);

                if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                    _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));

                    Snacky.builder()
                            .setActivty(BootActivity.this)
                            .setText("Failed to navigate to LoginActivity! Please contact LucaHome support!")
                            .setDuration(Snacky.LENGTH_INDEFINITE)
                            .setActionText(android.R.string.ok)
                            .error()
                            .show();
                }
            } else {
                _mainServiceBinder.StartDownloadAll("_mainServiceConnection onServiceConnected");
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            _logger.Debug("onServiceDisconnected");
            _mainServiceBinder = null;
        }
    };

    /**
     * BroadcastReceiver to receive progress and success state of initial app download
     */
    private BroadcastReceiver _mainServiceDownloadProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_mainServiceDownloadProgress");
            MainService.MainServiceDownloadCountContent progress = (MainService.MainServiceDownloadCountContent) intent.getSerializableExtra(MainService.MainServiceDownloadCountBundle);

            if (progress != null) {
                _percentProgressBar.setProgress((int) progress.DownloadProgress);
                _percentProgressTextView.setText(String.format(Locale.getDefault(), "%.0f %%", progress.DownloadProgress));

                if (progress.DownloadFinished) {
                    NavigationService.NavigationResult navigationResult = _navigationService.NavigateToActivity(BootActivity.this, MainActivity.class);
                    if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                        _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                        Snacky.builder()
                                .setActivty(BootActivity.this)
                                .setText("Failed to navigate back to MainActivity! Please contact LucaHome support!")
                                .setDuration(Snacky.LENGTH_INDEFINITE)
                                .setActionText(android.R.string.ok)
                                .error()
                                .show();
                    } else {
                        finish();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot);

        _logger = new Logger(TAG);
        _logger.Debug("onCreate");

        _percentProgressBar = (ProgressBar) findViewById(R.id.percentProgressBar);
        _percentProgressBar.setProgress(0);
        _percentProgressTextView = (TextView) findViewById(R.id.percentProgressTextView);
        _percentProgressTextView.setText("0%");

        _receiverController = new ReceiverController(this);
        _navigationService = NavigationService.getInstance();
        _userService = UserService.getInstance();
    }


    @Override
    protected void onStart() {
        super.onStart();
        _logger.Debug("onStart");
        startService(new Intent(BootActivity.this, MainService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        _logger.Debug("onResume");

        if (_mainServiceBinder == null) {
            _logger.Debug("Not bound to service! Binding now...");
            bindService(new Intent(this, MainService.class), _mainServiceConnection, Context.BIND_AUTO_CREATE);
        }

        _receiverController.RegisterReceiver(_mainServiceDownloadProgressReceiver, new String[]{MainService.MainServiceDownloadCountBroadcast});

        if (_loginAttempt) {
            _loginAttempt = false;
            _mainServiceBinder.StartDownloadAll("onResume after _loginAttempt");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        _logger.Debug("onPause");
        _receiverController.Dispose();
        if (_mainServiceBinder != null) {
            _logger.Debug("Unbinding from server");
            try {
                unbindService(_mainServiceConnection);
            } catch (Exception exception) {
                _logger.Error(exception.getMessage());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _loginAttempt = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        _logger.Debug(String.format("onKeyDown: keyCode: %s | event: %s", keyCode, event));

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Snacky.builder()
                    .setActivty(BootActivity.this)
                    .setText("You cannot finish during boot!")
                    .setDuration(Snacky.LENGTH_LONG)
                    .setActionText(android.R.string.ok)
                    .info()
                    .show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}