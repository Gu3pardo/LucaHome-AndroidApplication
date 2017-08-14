package guepardoapps.lucahome.views;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.rey.material.widget.Button;

import java.util.ArrayList;
import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.Security;
import guepardoapps.lucahome.common.service.SecurityService;
import guepardoapps.lucahome.service.NavigationService;

public class SecurityActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = SecurityActivity.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    /**
     * Initiate UI
     */
    private TextView _cameraStateTextView;
    private WebView _cameraWebView;
    private ListView _registeredEventsListView;
    private Button _setCameraButton;
    private Button _setMotionControlButton;

    /**
     * ReceiverController to register and unregister from broadcasts of the UserService
     */
    private ReceiverController _receiverController;

    /**
     * NavigationService manages navigation between activities
     */
    private NavigationService _navigationService;

    /**
     * BirthdayService manages data for birthdays
     */
    private SecurityService _securityService;

    /**
     * BroadcastReceiver to receive updates for the security
     */
    private BroadcastReceiver _securityUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_securityUpdateReceiver");
            SecurityService.SecurityDownloadFinishedContent result =
                    (SecurityService.SecurityDownloadFinishedContent) intent.getSerializableExtra(SecurityService.SecurityDownloadFinishedBundle);

            if (result.Success) {
                if (result.SecurityList != null) {
                    if (result.SecurityList.getSize() > 0) {
                        setUI(result.SecurityList.getValue(0));
                    }
                } else {
                    Snacky.builder()
                            .setActivty(SecurityActivity.this)
                            .setText(Tools.DecompressByteArrayToString(result.Response))
                            .setDuration(Snacky.LENGTH_INDEFINITE)
                            .setActionText(android.R.string.ok)
                            .error()
                            .show();
                }
            } else {
                Snacky.builder()
                        .setActivty(SecurityActivity.this)
                        .setText(Tools.DecompressByteArrayToString(result.Response))
                        .setDuration(Snacky.LENGTH_INDEFINITE)
                        .setActionText(android.R.string.ok)
                        .error()
                        .show();
            }
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _logger = new Logger(TAG);
        _logger.Debug("onCreate");

        setContentView(R.layout.activity_security);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_security);

        _context = this;

        _receiverController = new ReceiverController(_context);

        _navigationService = NavigationService.getInstance();
        _securityService = SecurityService.getInstance();

        _cameraStateTextView = (TextView) findViewById(R.id.cameraStateTextView);
        _cameraWebView = (WebView) findViewById(R.id.cameraWebView);
        _registeredEventsListView = (ListView) findViewById(R.id.registeredEventsListView);
        _setCameraButton = (Button) findViewById(R.id.setCameraButton);
        _setMotionControlButton = (Button) findViewById(R.id.setMotionControlButton);

        _cameraWebView.getSettings().setUseWideViewPort(true);
        _cameraWebView.getSettings().setBuiltInZoomControls(true);
        _cameraWebView.getSettings().setSupportZoom(true);
        _cameraWebView.getSettings().setJavaScriptEnabled(true);
        _cameraWebView.getSettings().setLoadWithOverviewMode(true);
        _cameraWebView.setWebViewClient(new WebViewClient());
        _cameraWebView.setInitialScale(100);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(false);

        _setCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_setCameraButton onClick");

                SerializableList<Security> securityList = _securityService.GetSecurityList();
                if (securityList != null) {
                    if (securityList.getSize() > 0) {
                        boolean newState = !securityList.getValue(0).IsCameraActive();
                        if (!newState) {
                            _securityService.SetMotionState(false);
                        }
                        _securityService.SetCameraState(newState);
                        return;
                    }
                }

                String message = "Could not activate camera!";
                _logger.Warning(message);
                Snacky.builder()
                        .setActivty(SecurityActivity.this)
                        .setText(message)
                        .setDuration(Snacky.LENGTH_INDEFINITE)
                        .setActionText(android.R.string.ok)
                        .warning()
                        .show();
            }
        });
        _setMotionControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_setMotionControlButton onClick");

                SerializableList<Security> securityList = _securityService.GetSecurityList();
                if (securityList != null) {
                    if (securityList.getSize() > 0) {
                        _securityService.SetMotionState(!securityList.getValue(0).IsMotionControlActive());
                        return;
                    }
                }

                String message = "Could not activate camera!";
                _logger.Warning(message);
                Snacky.builder()
                        .setActivty(SecurityActivity.this)
                        .setText(message)
                        .setDuration(Snacky.LENGTH_INDEFINITE)
                        .setActionText(android.R.string.ok)
                        .warning()
                        .show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_security);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_security);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        _logger.Debug("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        _logger.Debug("onResume");

        _receiverController.RegisterReceiver(_securityUpdateReceiver, new String[]{SecurityService.SecurityDownloadFinishedBroadcast});

        SerializableList<Security> securityList = _securityService.GetSecurityList();
        if (securityList != null) {
            if (securityList.getSize() > 0) {
                setUI(securityList.getValue(0));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        _logger.Debug("onPause");
        _receiverController.Dispose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_security);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            _navigationService.GoBack(this);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        NavigationService.NavigationResult navigationResult = NavigationService.NavigationResult.NULL;

        if (id == R.id.nav_schedule) {
            navigationResult = _navigationService.NavigateToActivity(_context, ScheduleActivity.class);
        } else if (id == R.id.nav_timer) {
            navigationResult = _navigationService.NavigateToActivity(_context, TimerActivity.class);
        } else if (id == R.id.nav_socket) {
            navigationResult = _navigationService.NavigateToActivity(_context, WirelessSocketActivity.class);
        } else if (id == R.id.nav_movie) {
            navigationResult = _navigationService.NavigateToActivity(_context, MovieActivity.class);
        } else if (id == R.id.nav_mediamirror) {
            navigationResult = _navigationService.NavigateToActivity(_context, MediaMirrorActivity.class);
        } else if (id == R.id.nav_coins) {
            navigationResult = _navigationService.NavigateToActivity(_context, CoinActivity.class);
        } else if (id == R.id.nav_menu) {
            navigationResult = _navigationService.NavigateToActivity(_context, MenuActivity.class);
        } else if (id == R.id.nav_shopping) {
            navigationResult = _navigationService.NavigateToActivity(_context, ShoppingListActivity.class);
        } else if (id == R.id.nav_forecast_weather) {
            navigationResult = _navigationService.NavigateToActivity(_context, ForecastWeatherActivity.class);
        } else if (id == R.id.nav_birthday) {
            navigationResult = _navigationService.NavigateToActivity(_context, BirthdayActivity.class);
        } else if (id == R.id.nav_settings) {
            navigationResult = _navigationService.NavigateToActivity(_context, SettingsActivity.class);
        }

        if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
            _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));

            Snacky.builder()
                    .setActivty(SecurityActivity.this)
                    .setText("Failed to navigate! Please contact LucaHome support!")
                    .setDuration(Snacky.LENGTH_INDEFINITE)
                    .setActionText(android.R.string.ok)
                    .error()
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_security);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setUI(@NonNull Security security) {
        _logger.Debug(String.format(Locale.getDefault(), "setUI with security %s", security));

        _cameraStateTextView.setVisibility(security.IsCameraActive() ? View.GONE : View.VISIBLE);
        _setMotionControlButton.setVisibility(security.IsCameraActive() ? View.VISIBLE : View.INVISIBLE);
        _cameraWebView.setVisibility(security.IsCameraActive() ? View.VISIBLE : View.GONE);

        _registeredEventsListView.setVisibility(security.GetRegisteredEvents() != null ? View.VISIBLE : View.GONE);
        if (security.GetRegisteredEvents() != null) {
            if (security.GetRegisteredEvents().getSize() > 0) {
                ArrayList<String> registeredEventList = new ArrayList<>();
                for (int index = 0; index < security.GetRegisteredEvents().getSize(); index++) {
                    registeredEventList.add(security.GetRegisteredEvents().getValue(index));
                }
                _registeredEventsListView.setAdapter(new ArrayAdapter<>(SecurityActivity.this, android.R.layout.simple_dropdown_item_1line, registeredEventList));
            }
        }

        if (security.IsCameraActive() && security.GetCameraUrl() != null) {
            if (security.GetCameraUrl().length() > 0) {
                String url = "http://" + security.GetCameraUrl();
                _logger.Information("Load url " + url);
                _cameraWebView.loadUrl(url);
            }
        }

        _setCameraButton.setText(security.IsCameraActive() ? "Deactivate camera" : "Activate camera");
        _setMotionControlButton.setEnabled(security.IsCameraActive());
        _setMotionControlButton.setText(security.IsMotionControlActive() ? "Deactivate motion control" : "Activate motion control");
    }
}
