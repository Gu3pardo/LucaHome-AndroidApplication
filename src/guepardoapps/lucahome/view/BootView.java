package guepardoapps.lucahome.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.Color;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.enums.Command;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.common.enums.NavigateData;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.ControlMainServiceState;
import guepardoapps.lucahome.services.MainService;
import guepardoapps.lucahome.services.helper.DialogService;
import guepardoapps.lucahome.services.helper.NavigationService;
import guepardoapps.lucahome.services.wearcontrol.PhoneMessageListenerService;
import guepardoapps.toolset.controller.NetworkController;
import guepardoapps.toolset.controller.ReceiverController;

public class BootView extends Activity {

	private static final String TAG = BootView.class.getName();
	private LucaHomeLogger _logger;

	private int _progressBarMax = 100;
	private int _progressBarSteps = Constants.DOWNLOAD_STEPS;

	private boolean _isInitialized;

	private ProgressBar _percentProgressBar;
	private TextView _progressTextView;

	private Context _context;

	private DialogService _dialogService;
	private NavigationService _navigationService;
	private NetworkController _networkController;
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
			_context.startService(new Intent(_context, ControlMainServiceState.class));
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
					_dialogService.ShowUserCredentialsDialog(null, _startDownloadRunnable, false);
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

			_percentProgressBar.setProgress((int) (progress * _progressBarMax / _progressBarSteps));
			_progressTextView.setText(String.valueOf((int) (progress * _progressBarMax / _progressBarSteps)) + " %");
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_boot);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));

		_percentProgressBar = (ProgressBar) findViewById(R.id.percentProgressBar);
		_percentProgressBar.setMax(_progressBarMax);
		_percentProgressBar.setProgress(0);

		_progressTextView = (TextView) findViewById(R.id.percentProgressTextView);
		_progressTextView.setText("0 %");

		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_context = this;

		_dialogService = new DialogService(_context);
		_navigationService = new NavigationService(_context);
		_networkController = new NetworkController(_context, _dialogService);
		_receiverController = new ReceiverController(_context);

		if (_networkController.IsNetworkAvailable()) {
			if (_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
				_startDownloadRunnable.run();
			} else {
				_logger.Warn("No LucaHome network! ...");
				Toast.makeText(_context, "No LucaHome network! ...", Toast.LENGTH_LONG).show();
				_navigationService.NavigateTo(HomeView.class, null, true);
			}
		} else {
			_logger.Warn("No network available!");
			Toast.makeText(_context, "No network available!", Toast.LENGTH_LONG).show();
			_navigationService.NavigateTo(HomeView.class, null, true);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		_logger.Debug("onResume");

		if (!_isInitialized) {
			_receiverController.RegisterReceiver(_commandReceiver, new String[] { Broadcasts.COMMAND });
			_receiverController.RegisterReceiver(_updateProgressBarReceiver,
					new String[] { Broadcasts.UPDATE_PROGRESSBAR });
			_isInitialized = true;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		_logger.Debug("onPause");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_logger.Debug("onDestroy");

		_receiverController.UnregisterReceiver(_commandReceiver);
		_receiverController.UnregisterReceiver(_updateProgressBarReceiver);
	}
}