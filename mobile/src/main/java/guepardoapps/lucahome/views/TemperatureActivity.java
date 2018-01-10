package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;

import de.mateware.snacky.Snacky;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.Temperature;
import guepardoapps.lucahome.common.service.TemperatureService;

@SuppressWarnings({"deprecation", "SetJavaScriptEnabled"})
public class TemperatureActivity extends AppCompatBaseActivity {
    /**
     * Initiate UI
     */
    private Spinner _spinner;
    private TextView _currentTemperatureTextView;
    private WebView _temperatureWebView;

    private boolean _spinnerEnabled = true;

    /**
     * BroadcastReceiver to receive the event after download of temperature has finished
     */
    private BroadcastReceiver _temperatureDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TemperatureService.TemperatureDownloadFinishedContent result = (TemperatureService.TemperatureDownloadFinishedContent) intent.getSerializableExtra(TemperatureService.TemperatureDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);

            if (result.Success) {
                _lastUpdateTextView.setText(TemperatureService.getInstance().GetLastUpdate().toString());
                updateView();
            } else {
                displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                _noDataFallback.setVisibility(View.VISIBLE);
                _searchField.setVisibility(View.INVISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = TemperatureActivity.class.getSimpleName();

        setContentView(R.layout.activity_temperature);

        int id = getIntent().getIntExtra(TemperatureService.TemperatureDataIntent, 0);
        Temperature temperature = TemperatureService.getInstance().GetById(id);

        Toolbar toolbar = findViewById(R.id.toolbar_temperature);
        //setSupportActionBar(toolbar);

        _progressBar = findViewById(R.id.progressBar_temperature);
        _noDataFallback = findViewById(R.id.fallBackTextView_temperature);
        _lastUpdateTextView = findViewById(R.id.lastUpdateTextView_temperature);

        _collapsingToolbar = findViewById(R.id.collapsing_toolbar_temperature);
        _collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        _collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _spinner = findViewById(R.id.spinner_temperature);

        _currentTemperatureTextView = findViewById(R.id.textview_currentTemperature);

        _temperatureWebView = findViewById(R.id.temperatureWebView);
        _temperatureWebView.getSettings().setUseWideViewPort(true);
        _temperatureWebView.getSettings().setBuiltInZoomControls(true);
        _temperatureWebView.getSettings().setSupportZoom(true);
        _temperatureWebView.getSettings().setJavaScriptEnabled(true);
        _temperatureWebView.getSettings().setLoadWithOverviewMode(true);
        _temperatureWebView.setWebViewClient(new WebViewClient());
        _temperatureWebView.setInitialScale(100);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(false);

        _context = this;

        _receiverController = new ReceiverController(_context);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item, TemperatureService.getInstance().GetAreaList());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinner.setAdapter(dataAdapter);
        _spinnerEnabled = false;
        if (temperature != null) {
            TemperatureService.getInstance().SetActiveTemperature(temperature.GetId());
        }
        _spinner.setSelection(TemperatureService.getInstance().GetActiveTemperature().GetId(), true);
        _spinnerEnabled = true;
        _spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!_spinnerEnabled) {
                    Logger.getInstance().Warning(TAG, "_spinnerEnabled is disabled!");
                    return;
                }
                TemperatureService.getInstance().SetActiveTemperature(position);
                updateView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        _lastUpdateTextView.setText(TemperatureService.getInstance().GetLastUpdate().toString());
        updateView();

        _drawerLayout = findViewById(R.id.drawer_layout_temperature);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_temperature);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_temperatureDownloadReceiver, new String[]{TemperatureService.TemperatureDownloadFinishedBroadcast});
        updateView();
    }

    @Override
    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(TemperatureActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void updateView() {
        Temperature activeTemperature = TemperatureService.getInstance().GetActiveTemperature();
        if (activeTemperature != null) {
            _noDataFallback.setVisibility(View.GONE);

            _spinnerEnabled = false;
            _spinner.setSelection(activeTemperature.GetId(), true);
            _spinnerEnabled = true;

            String url = activeTemperature.GetGraphPath();
            if (url.length() > 0) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                _temperatureWebView.setVisibility(View.VISIBLE);
                _temperatureWebView.loadUrl(url);
            } else {
                _temperatureWebView.setVisibility(View.GONE);
            }

            _currentTemperatureTextView.setVisibility(View.VISIBLE);
            _currentTemperatureTextView.setText(activeTemperature.GetTemperatureString());
            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "Location: %s", activeTemperature.GetArea()));
        }

        _progressBar.setVisibility(View.GONE);
    }
}
