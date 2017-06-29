package guepardoapps.lucahome.views;

import android.annotation.SuppressLint;
import android.app.ActionBar;
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
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Color;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.IDs;
import guepardoapps.library.lucahome.common.dto.MotionCameraDto;
import guepardoapps.library.lucahome.common.enums.HomeAutomationAction;
import guepardoapps.library.lucahome.common.enums.LucaServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaNotificationController;
import guepardoapps.library.lucahome.controller.ServiceController;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.NetworkController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;

public class SecurityView extends Activity {

    private static final String TAG = SecurityView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private boolean _isInitialized;
    private MotionCameraDto _motionCameraDto;

    private TextView _motionCameraState;
    private WebView _motionCamera;
    private ListView _listView;
    private Button _buttonMotionHandle;
    private Button _buttonSetMotionControl;

    private Context _context;

    private BroadcastController _broadcastController;
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
                    _notificationController.CreateCameraNotification(SecurityView.class);
                } else {
                    _notificationController.CloseNotification(IDs.NOTIFICATION_CAMERA);
                }
            } else {
                _logger.Warn("Received NULL motionCameraDto!");
                _buttonMotionHandle.setText("Not available!");
                _buttonMotionHandle.setEnabled(false);
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
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.HOME_AUTOMATION_COMMAND,
                    new String[]{Bundles.HOME_AUTOMATION_ACTION},
                    new Object[]{HomeAutomationAction.GET_MOTION_CAMERA_DATA});
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

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));
        } else {
            _logger.Error("ActionBar is null!");
        }

        _context = this;

        _broadcastController = new BroadcastController(_context);
        _navigationService = new NavigationService(_context);
        _networkController = new NetworkController(_context);
        _notificationController = new LucaNotificationController(_context);
        _receiverController = new ReceiverController(_context);
        _serviceController = new ServiceController(_context);

        _motionCameraState = findViewById(R.id.motionStateText);

        _motionCamera = findViewById(R.id.webViewCamera);
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

        _listView = findViewById(R.id.motionActionsListView);

        _buttonMotionHandle = findViewById(R.id.buttonMotionHandle);
        _buttonMotionHandle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _logger.Debug("_buttonMotionHandle onClick");
                if (_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                    if (_motionCameraDto != null) {
                        if (_motionCameraDto.GetCameraState()) {
                            _serviceController.StartRestService(
                                    Bundles.MOTION_CAMERA_DTO,
                                    LucaServerAction.STOP_MOTION.toString(),
                                    Broadcasts.RELOAD_MOTION_CAMERA_DTO);
                            _notificationController.CloseNotification(IDs.NOTIFICATION_CAMERA);
                        } else {
                            _serviceController.StartRestService(
                                    Bundles.MOTION_CAMERA_DTO,
                                    LucaServerAction.START_MOTION.toString(),
                                    Broadcasts.RELOAD_MOTION_CAMERA_DTO);
                            _notificationController.CreateCameraNotification(SecurityView.class);
                        }
                    } else {
                        _logger.Warn("No data from server available!");
                        Toasty.warning(_context, "No data from server available!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    _logger.Warn("Not possible outside home WIFI!");
                    Toasty.warning(_context, "Not possible outside home WIFI!", Toast.LENGTH_LONG).show();
                    updateView();
                }
            }
        });

        _buttonSetMotionControl = findViewById(R.id.buttonSetMotionControl);
        _buttonSetMotionControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_buttonSetMotionControl onClick");
                if (_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                    if (_motionCameraDto != null) {
                        if (_motionCameraDto.GetCameraState()) {
                            if (_motionCameraDto.GetCameraControlState()) {
                                String action = LucaServerAction.SET_MOTION_CONTROL_TASK.toString() + "OFF";
                                _serviceController.StartRestService(
                                        Bundles.MOTION_CAMERA_DTO,
                                        action,
                                        Broadcasts.RELOAD_MOTION_CAMERA_DTO);
                            } else {
                                String action = LucaServerAction.SET_MOTION_CONTROL_TASK.toString() + "ON";
                                _serviceController.StartRestService(
                                        Bundles.MOTION_CAMERA_DTO,
                                        action,
                                        Broadcasts.RELOAD_MOTION_CAMERA_DTO);
                            }
                        } else {
                            _logger.Warn("Motion is not running!");
                            Toasty.warning(_context, "Motion is not running!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        _logger.Warn("No data from server available!");
                        Toasty.warning(_context, "No data from server available!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    _logger.Warn("Not possible outside home WIFI!");
                    Toasty.warning(_context, "Not possible outside home WIFI!", Toast.LENGTH_LONG).show();
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
                        new String[]{Broadcasts.UPDATE_MOTION_CAMERA_DTO});
                _receiverController.RegisterReceiver(_wifiStateReceiver,
                        new String[]{"android.net.wifi.supplicant.CONNECTION_CHANGE"});
                _getDataRunnable.run();
                _updateHandler.postDelayed(_updateRunnable, UPDATE_TIMEOUT);
            }
        }
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
        _receiverController.Dispose();
        _updateHandler.removeCallbacks(_updateRunnable);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _updateHandler.removeCallbacks(_updateRunnable);
        super.onDestroy();
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
        if (item.getItemId() == R.id.buttonReload) {
            _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_MOTION_CAMERA_DTO);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void updateView() {
        _logger.Debug("updateView");

        if (_motionCameraDto.GetCameraState()) {
            _motionCameraState.setVisibility(View.INVISIBLE);
            _motionCamera.setVisibility(View.VISIBLE);
            _motionCamera.loadUrl("http://" + _motionCameraDto.GetCameraUrl());
            _buttonMotionHandle.setText("Deactivate motion");
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
            _buttonMotionHandle.setText("Activate motion");
            _buttonSetMotionControl.setVisibility(View.INVISIBLE);
            _buttonSetMotionControl.setEnabled(false);
        }
        _buttonMotionHandle.setEnabled(true);

        int motionEventCount = _motionCameraDto.GetMotionEvents().getSize();
        if (motionEventCount > 0) {
            String[] list = new String[motionEventCount];
            for (int index = 0; index < motionEventCount; index++) {
                list[index] = _motionCameraDto.GetMotionEvents().getValue(index);
            }

            _listView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, list));
            _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    _logger.Debug("_listView.onItemClick");
                    _logger.Debug("adapterView: " + adapterView.toString());
                    _logger.Debug("view: " + view.toString());
                    _logger.Debug("position: " + String.valueOf(position));
                    _logger.Debug("id: " + String.valueOf(id));
                }
            });
        } else {
            _listView.setVisibility(View.INVISIBLE);
        }
    }
}
