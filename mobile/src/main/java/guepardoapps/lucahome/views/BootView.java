package guepardoapps.lucahome.views;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Color;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.enums.Command;
import guepardoapps.library.lucahome.common.enums.MainServiceAction;
import guepardoapps.library.lucahome.common.enums.NavigateData;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.services.helper.NavigationService;
import guepardoapps.library.lucahome.services.wearcontrol.PhoneMessageListenerService;

import guepardoapps.library.toolset.controller.NetworkController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.services.ControlServiceStateService;
import guepardoapps.lucahome.services.MainService;

public class BootView extends Activity {

    private static final String TAG = BootView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private int _progressBarMax = 100;

    private boolean _isInitialized;

    private ProgressBar _percentProgressBar;
    private TextView _progressTextView;

    private Context _context;

    private LucaDialogController _dialogController;
    private NavigationService _navigationService;
    private ReceiverController _receiverController;

    private Runnable _startDownloadRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_startDownloadRunnable run");

            Intent startMainService = new Intent(_context, MainService.class);
            Bundle mainServiceBundle = new Bundle();
            mainServiceBundle.putSerializable(Bundles.MAIN_SERVICE_ACTION, MainServiceAction.BOOT);
            startMainService.putExtras(mainServiceBundle);
            _context.startService(startMainService);

            _context.startService(new Intent(_context, PhoneMessageListenerService.class));
            _context.startService(new Intent(_context, ControlServiceStateService.class));
        }
    };

    private BroadcastReceiver _commandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_commandReceiver");

            Command command = (Command) intent.getSerializableExtra(Bundles.COMMAND);
            if (command != null) {
                _logger.Debug("Command: " + command.toString());

                switch (command) {
                    case NAVIGATE:
                        NavigateData navigateData = (NavigateData) intent.getSerializableExtra(Bundles.NAVIGATE_DATA);

                        if (navigateData != null) {
                            switch (navigateData) {
                                case HOME:
                                    _navigationService.NavigateTo(HomeView.class, null, true);
                                    break;
                                case FINISH:
                                    finish();
                                    break;
                                default:
                                    _logger.Warn("NavigateData is not supported: " + navigateData.toString());
                                    break;
                            }
                        } else {
                            _logger.Warn("NavigateData is null!");
                        }
                        break;
                    case SHOW_USER_LOGIN_DIALOG:
                        _logger.Debug("SHOW_USER_LOGIN_DIALOG");
                        _dialogController.ShowUserCredentialsDialog(null, _startDownloadRunnable, false);
                        break;
                    default:
                        _logger.Warn("Command is not supported: " + command.toString());
                        break;
                }
            } else {
                _logger.Warn("Command is null!");
            }
        }
    };

    private BroadcastReceiver _updateProgressBarReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateProgressBarReceiver");
            int progress = intent.getIntExtra(Bundles.VIEW_PROGRESS, -1);
            _logger.Debug("Progress is: " + String.valueOf(progress));

            int currentProgress = progress * _progressBarMax / Constants.DOWNLOAD_STEPS;
            if (currentProgress > 100) {
                currentProgress = 100;
            }

            _percentProgressBar.setProgress(currentProgress);
            _progressTextView.setText(String.format(Locale.GERMAN, "%d%%", currentProgress));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_boot);

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));
        } else {
            _logger.Error("ActionBar is null!");
        }

        _percentProgressBar = (ProgressBar) findViewById(R.id.percentProgressBar);
        _percentProgressBar.setMax(_progressBarMax);
        _percentProgressBar.setProgress(0);

        _progressTextView = (TextView) findViewById(R.id.percentProgressTextView);
        _progressTextView.setText("0 %");

        _context = this;

        _dialogController = new LucaDialogController(_context);
        _navigationService = new NavigationService(_context);
        _receiverController = new ReceiverController(_context);

        _startDownloadRunnable.run();

        NetworkController networkController = new NetworkController(_context, _dialogController);
        if (networkController.IsNetworkAvailable()) {
            if (!networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                _logger.Warn("No LucaHome network! ...");
                Toasty.warning(_context, "No LucaHome network! ...", Toast.LENGTH_LONG).show();
                _navigationService.NavigateTo(HomeView.class, null, true);
            }
        } else {
            _logger.Warn("No network available!");
            Toasty.warning(_context, "No network available!", Toast.LENGTH_LONG).show();
            _navigationService.NavigateTo(HomeView.class, null, true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        _logger.Debug("onResume");

        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_commandReceiver, new String[]{Broadcasts.COMMAND});
            _receiverController.RegisterReceiver(_updateProgressBarReceiver,
                    new String[]{Broadcasts.UPDATE_PROGRESSBAR});
            _isInitialized = true;
        }
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
        _dialogController.Dispose();
        _receiverController.Dispose();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        _dialogController.Dispose();
        _receiverController.Dispose();
        super.onDestroy();
    }
}