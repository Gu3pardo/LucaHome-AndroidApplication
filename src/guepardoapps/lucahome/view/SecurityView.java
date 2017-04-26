package guepardoapps.lucahome.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import guepardoapps.library.lucahome.common.constants.Color;
import guepardoapps.library.lucahome.common.constants.IDs;
import guepardoapps.library.lucahome.common.constants.ServerActions;
import guepardoapps.library.lucahome.common.dto.MotionCameraDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.MainServiceAction;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaNotificationController;
import guepardoapps.library.lucahome.controller.MediaStorageController;
import guepardoapps.library.lucahome.controller.ServiceController;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.toastview.ToastView;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.NetworkController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.Constants;

public class SecurityView extends Activity {

	private static final String TAG = SecurityView.class.getSimpleName();
	private LucaHomeLogger _logger;

	private boolean _isInitialized;
	private MotionCameraDto _motionCameraDto;

	private TextView _motionCameraState;
	private WebView _motionCamera;
	private ListView _listView;
	private Button _buttionMotionHandle;
	private Button _buttonSetMotionControl;

	private Context _context;

	private BroadcastController _broadcastController;
	private MediaStorageController _mediaStorageController;
	private NavigationService _navigationService;
	private NetworkController _networkController;
	private LucaNotificationController _notificationController;
	private ReceiverController _receiverController;
	private ServiceController _serviceController;

	private BroadcastReceiver _motionCameraDtoUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_motionCameraDtoUpdateReceiver onReceive");
			MotionCameraDto motionCameraDto = (MotionCameraDto) intent.getSerializableExtra(Bundles.MOTION_CAMERA_DTO);
			if (motionCameraDto != null) {
				_motionCameraDto = motionCameraDto;
				updateView();
				if (_motionCameraDto.GetCameraState()) {
					_notificationController.CreateCameraNotification(_motionCameraDto, SecurityView.class);
				} else {
					_notificationController.CloseNotification(IDs.NOTIFICATION_CAMERA);
				}
			} else {
				_logger.Warn("Received NULL motionCameraDto!");
				_buttionMotionHandle.setText("Not available!");
				_buttionMotionHandle.setEnabled(false);
			}
		}
	};

	private BroadcastReceiver _wifiStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_wifiStateReceiver onReceive");
			updateView();
		}
	};

	private Runnable _getDataRunnable = new Runnable() {
		public void run() {
			_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
					new String[] { Bundles.MAIN_SERVICE_ACTION },
					new Object[] { MainServiceAction.GET_MOTION_CAMERA_DTO });
		}
	};

	private static final int UPDATE_TIMEOUT = 5 * 1000;
	private Handler _updateHandler = new Handler();
	private Runnable _updateRunnable = new Runnable() {
		@Override
		public void run() {
			_broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_MOTION_CAMERA_DTO);
			_updateHandler.postDelayed(_updateRunnable, UPDATE_TIMEOUT);
		}
	};

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_security);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));

		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_context = this;

		_broadcastController = new BroadcastController(_context);
		_mediaStorageController = MediaStorageController.getInstance();
		_mediaStorageController.initialize(_context);
		_navigationService = new NavigationService(_context);
		_networkController = new NetworkController(_context);
		_notificationController = new LucaNotificationController(_context);
		_receiverController = new ReceiverController(_context);
		_serviceController = new ServiceController(_context);

		_motionCameraState = (TextView) findViewById(R.id.motionStateText);

		_motionCamera = (WebView) findViewById(R.id.webViewCamera);
		final Activity activity = this;
		_motionCamera.setWebChromeClient(new WebChromeClient() {
			@SuppressWarnings("deprecation")
			public void onProgressChanged(WebView view, int progress) {
				activity.setProgress(progress * 100);
			}
		});

		_motionCamera.getSettings().setUseWideViewPort(true);
		_motionCamera.getSettings().setBuiltInZoomControls(true);
		_motionCamera.getSettings().setSupportZoom(true);
		_motionCamera.getSettings().setJavaScriptEnabled(true);
		_motionCamera.getSettings().setLoadWithOverviewMode(true);
		_motionCamera.setWebViewClient(new WebViewClient());
		_motionCamera.setInitialScale(100);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(false);

		_listView = (ListView) findViewById(R.id.motionActionsListView);

		_buttionMotionHandle = (Button) findViewById(R.id.buttonMotionHandle);
		_buttionMotionHandle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttionMotionHandle onClick");
				if (_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
					if (_motionCameraDto != null) {
						if (_motionCameraDto.GetCameraState()) {
							_serviceController.StartRestService(Bundles.MOTION_CAMERA_DTO, ServerActions.STOP_MOTION,
									Broadcasts.RELOAD_MOTION_CAMERA_DTO, LucaObject.MOTION_CAMERA_DTO,
									RaspberrySelection.BOTH);
							_notificationController.CloseNotification(IDs.NOTIFICATION_CAMERA);
						} else {
							_serviceController.StartRestService(Bundles.MOTION_CAMERA_DTO, ServerActions.START_MOTION,
									Broadcasts.RELOAD_MOTION_CAMERA_DTO, LucaObject.MOTION_CAMERA_DTO,
									RaspberrySelection.BOTH);
							_notificationController.CreateCameraNotification(_motionCameraDto, SecurityView.class);
						}
					} else {
						_logger.Warn("No data from server available!");
						ToastView.warning(_context, "No data from server available!", Toast.LENGTH_LONG).show();
					}
				} else {
					_logger.Warn("Not possible outside home WIFI!");
					ToastView.warning(_context, "Not possible outside home WIFI!", Toast.LENGTH_LONG).show();
					updateView();
				}
			}
		});

		_buttonSetMotionControl = (Button) findViewById(R.id.buttonSetMotionControl);
		_buttonSetMotionControl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttonSetMotionControl onClick");
				if (_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
					if (_motionCameraDto != null) {
						if (_motionCameraDto.GetCameraState()) {
							if (_motionCameraDto.GetCameraControlState()) {
								String action = ServerActions.SET_MOTION_CONTROL_TASK + "OFF";
								_serviceController.StartRestService(Bundles.MOTION_CAMERA_DTO, action,
										Broadcasts.RELOAD_MOTION_CAMERA_DTO, LucaObject.MOTION_CAMERA_DTO,
										RaspberrySelection.BOTH);
							} else {
								String action = ServerActions.SET_MOTION_CONTROL_TASK + "ON";
								_serviceController.StartRestService(Bundles.MOTION_CAMERA_DTO, action,
										Broadcasts.RELOAD_MOTION_CAMERA_DTO, LucaObject.MOTION_CAMERA_DTO,
										RaspberrySelection.BOTH);
							}
						} else {
							_logger.Warn("Motion is not running!");
							ToastView.warning(_context, "Motion is not running!", Toast.LENGTH_LONG).show();
						}
					} else {
						_logger.Warn("No data from server available!");
						ToastView.warning(_context, "No data from server available!", Toast.LENGTH_LONG).show();
					}
				} else {
					_logger.Warn("Not possible outside home WIFI!");
					ToastView.warning(_context, "Not possible outside home WIFI!", Toast.LENGTH_LONG).show();
					updateView();
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		_logger.Debug("onResume");
		if (!_isInitialized) {
			if (_receiverController != null && _broadcastController != null) {
				_isInitialized = true;
				_receiverController.RegisterReceiver(_motionCameraDtoUpdateReceiver,
						new String[] { Broadcasts.UPDATE_MOTION_CAMERA_DTO });
				_receiverController.RegisterReceiver(_wifiStateReceiver,
						new String[] { "android.net.wifi.supplicant.CONNECTION_CHANGE" });
				_getDataRunnable.run();
				_updateHandler.postDelayed(_updateRunnable, UPDATE_TIMEOUT);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		_logger.Debug("onPause");
		_mediaStorageController.Dispose();
		_receiverController.Dispose();
		_updateHandler.removeCallbacks(_updateRunnable);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_logger.Debug("onDestroy");
		_mediaStorageController.Dispose();
		_receiverController.Dispose();
		_updateHandler.removeCallbacks(_updateRunnable);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			_navigationService.NavigateTo(HomeView.class, true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_basic_reload, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.buttonReload) {
			_broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_MOTION_CAMERA_DTO);
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateView() {
		_logger.Debug("updateView");

		if (_motionCameraDto.GetCameraState()) {
			_motionCameraState.setVisibility(View.INVISIBLE);
			_motionCamera.setVisibility(View.VISIBLE);
			_motionCamera.loadUrl("http://" + _motionCameraDto.GetCameraUrl());
			_buttionMotionHandle.setText("Deactivate motion");
			_buttonSetMotionControl.setVisibility(View.VISIBLE);
			_buttonSetMotionControl.setEnabled(true);
			if (_motionCameraDto.GetCameraControlState()) {
				_buttonSetMotionControl.setText("Deactivate motion control");
			} else {
				_buttonSetMotionControl.setText("Activate motion control");
			}
		} else {
			_motionCameraState.setVisibility(View.VISIBLE);
			_motionCamera.setVisibility(View.INVISIBLE);
			_buttionMotionHandle.setText("Activate motion");
			_buttonSetMotionControl.setVisibility(View.INVISIBLE);
			_buttonSetMotionControl.setEnabled(false);
		}
		_buttionMotionHandle.setEnabled(true);

		int motionEventCount = _motionCameraDto.GetMotionEvents().getSize();
		if (motionEventCount > 0) {
			String[] list = new String[motionEventCount];
			for (int index = 0; index < motionEventCount; index++) {
				list[index] = _motionCameraDto.GetMotionEvents().getValue(index);
			}

			_listView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, list));
			_listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long arg3) {
					_logger.Debug("_listView.onItemClick");
					_logger.Debug("adapterView: " + adapterView.toString());
					_logger.Debug("view: " + view.toString());
					_logger.Debug("arg2: " + String.valueOf(arg2));
					_logger.Debug("arg3: " + String.valueOf(arg3));
				}
			});
		} else {
			_listView.setVisibility(View.INVISIBLE);
		}
	}
}
