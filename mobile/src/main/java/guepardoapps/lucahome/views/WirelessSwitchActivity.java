package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.adapter.SwitchListViewAdapter;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.dto.WirelessSwitchDto;
import guepardoapps.lucahome.common.service.WirelessSwitchService;
import guepardoapps.lucahome.service.NavigationService;

public class WirelessSwitchActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = WirelessSwitchActivity.class.getSimpleName();

    private Context _context;

    /**
     * Initiate UI
     */
    private EditText _searchField;
    private ProgressBar _progressBar;
    private ListView _listView;
    private TextView _noDataFallback;
    private TextView _lastUpdateTextView;
    private CollapsingToolbarLayout _collapsingToolbar;
    private PullRefreshLayout _pullRefreshLayout;

    /**
     * ReceiverController to register and unregister from broadcasts of the UserService
     */
    private ReceiverController _receiverController;

    /**
     * NavigationService manages navigation between activities
     */
    private NavigationService _navigationService;

    /**
     * WirelessSwitchService manages data for switches
     */
    private WirelessSwitchService _wirelessSwitchService;

    /**
     * Adapter for the switch entries of the listView
     */
    private SwitchListViewAdapter _switchListViewAdapter;

    /**
     * BroadcastReceiver to receive updates for the switches
     */
    private BroadcastReceiver _wirelessSwitchUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WirelessSwitchService.WirelessSwitchDownloadFinishedContent result =
                    (WirelessSwitchService.WirelessSwitchDownloadFinishedContent) intent.getSerializableExtra(WirelessSwitchService.WirelessSwitchDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(_wirelessSwitchService.GetLastUpdate().toString());

                if (result.WirelessSwitchList != null) {
                    if (result.WirelessSwitchList.getSize() > 0) {
                        _switchListViewAdapter = new SwitchListViewAdapter(_context, result.WirelessSwitchList);
                        _listView.setAdapter(_switchListViewAdapter);

                        _noDataFallback.setVisibility(View.GONE);
                        _listView.setVisibility(View.VISIBLE);
                        _searchField.setVisibility(View.VISIBLE);

                        _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d switches", result.WirelessSwitchList.getSize()));
                    } else {
                        _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d switches", 0));
                        _noDataFallback.setVisibility(View.VISIBLE);
                        _searchField.setVisibility(View.INVISIBLE);
                    }

                    return;
                }
            }

            displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
            _noDataFallback.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wireless_switch);

        Toolbar toolbar = findViewById(R.id.toolbar_switches);
        //setSupportActionBar(toolbar);

        _listView = findViewById(R.id.listView_switches);
        _progressBar = findViewById(R.id.progressBar_switches);
        _noDataFallback = findViewById(R.id.fallBackTextView_switches);
        _lastUpdateTextView = findViewById(R.id.lastUpdateTextView_switches);

        _searchField = findViewById(R.id.search_wireless_switches);
        _searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                SerializableList<WirelessSwitch> filteredWirelessSwitchList = _wirelessSwitchService.SearchDataList(charSequence.toString());
                _switchListViewAdapter = new SwitchListViewAdapter(_context, filteredWirelessSwitchList);
                _listView.setAdapter(_switchListViewAdapter);
                _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d switches", filteredWirelessSwitchList.getSize()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _collapsingToolbar = findViewById(R.id.collapsing_toolbar_switches);
        _collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        _collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _context = this;

        _receiverController = new ReceiverController(_context);

        _navigationService = NavigationService.getInstance();
        _wirelessSwitchService = WirelessSwitchService.getInstance();

        _lastUpdateTextView.setText(_wirelessSwitchService.GetLastUpdate().toString());

        SerializableList<WirelessSwitch> wirelessSwitchList = _wirelessSwitchService.GetDataList();
        if (wirelessSwitchList.getSize() > 0) {
            _switchListViewAdapter = new SwitchListViewAdapter(_context, wirelessSwitchList);
            _listView.setAdapter(_switchListViewAdapter);

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d switches", wirelessSwitchList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);

        FloatingActionButton addButton = findViewById(R.id.floating_action_button_add_wireless_switch);
        addButton.setOnClickListener(view -> {
            Bundle data = new Bundle();
            data.putSerializable(WirelessSwitchService.WirelessSwitchIntent, new WirelessSwitchDto(-1, "", "", -1, '1', WirelessSwitchDto.Action.Add));

            NavigationService.NavigationResult navigationResult = _navigationService.NavigateToActivityWithData(_context, WirelessSwitchEditActivity.class, data);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout_switches);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_switch);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_switches);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _wirelessSwitchService.LoadData();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        _receiverController.RegisterReceiver(_wirelessSwitchUpdateReceiver, new String[]{WirelessSwitchService.WirelessSwitchDownloadFinishedBroadcast});

        SerializableList<WirelessSwitch> wirelessSwitchList = _wirelessSwitchService.GetDataList();
        if (wirelessSwitchList.getSize() > 0) {
            _switchListViewAdapter = new SwitchListViewAdapter(_context, wirelessSwitchList);
            _listView.setAdapter(_switchListViewAdapter);

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d switches", wirelessSwitchList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        _receiverController.Dispose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _receiverController.Dispose();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_switches);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            _navigationService.GoBack(_context);
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
        } else if (id == R.id.nav_movie) {
            navigationResult = _navigationService.NavigateToActivity(_context, MovieActivity.class);
        } else if (id == R.id.nav_mediamirror) {
            navigationResult = _navigationService.NavigateToActivity(_context, MediaServerActivity.class);
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
        } else if (id == R.id.nav_security) {
            navigationResult = _navigationService.NavigateToActivity(_context, SecurityActivity.class);
        } else if (id == R.id.nav_settings) {
            navigationResult = _navigationService.NavigateToActivity(_context, SettingsActivity.class);
        } else if (id == R.id.nav_socket) {
            navigationResult = _navigationService.NavigateToActivity(_context, WirelessSocketActivity.class);
        } else if (id == R.id.nav_meter) {
            navigationResult = _navigationService.NavigateToActivity(_context, MeterDataActivity.class);
        } else if (id == R.id.nav_money) {
            navigationResult = _navigationService.NavigateToActivity(_context, MoneyMeterDataActivity.class);
        }

        if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
            Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
            displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_switches);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(WirelessSwitchActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

}
