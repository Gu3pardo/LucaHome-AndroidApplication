package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.adapter.ScheduleListViewAdapter;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.Schedule;
import guepardoapps.lucahome.common.dto.ScheduleDto;
import guepardoapps.lucahome.common.enums.SocketAction;
import guepardoapps.lucahome.common.service.ScheduleService;
import guepardoapps.lucahome.service.NavigationService;

public class ScheduleActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = ScheduleActivity.class.getSimpleName();
    private Logger _logger;

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
     * ScheduleService manages data for schedules
     */
    private ScheduleService _scheduleService;

    /**
     * Adapter for the schedule entries of the listView
     */
    private ScheduleListViewAdapter _scheduleListViewAdapter;

    /**
     * BroadcastReceiver to receive updates for the schedules
     */
    private BroadcastReceiver _scheduleUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_scheduleUpdateReceiver");
            ScheduleService.ScheduleDownloadFinishedContent result =
                    (ScheduleService.ScheduleDownloadFinishedContent) intent.getSerializableExtra(ScheduleService.ScheduleDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(_scheduleService.GetLastUpdate().toString());

                if (result.ScheduleList != null) {
                    if (result.ScheduleList.getSize() > 0) {
                        _scheduleListViewAdapter = new ScheduleListViewAdapter(_context, result.ScheduleList);
                        _listView.setAdapter(_scheduleListViewAdapter);

                        _noDataFallback.setVisibility(View.GONE);
                        _listView.setVisibility(View.VISIBLE);
                        _searchField.setVisibility(View.VISIBLE);

                        _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d schedules", result.ScheduleList.getSize()));
                    } else {
                        _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d schedules", 0));
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

        _logger = new Logger(TAG);
        _logger.Debug("onCreate");

        setContentView(R.layout.activity_schedule);

        Toolbar toolbar = findViewById(R.id.toolbar_schedule);
        //setSupportActionBar(toolbar);

        _listView = findViewById(R.id.listView_schedule);
        _progressBar = findViewById(R.id.progressBar_schedule);
        _noDataFallback = findViewById(R.id.fallBackTextView_schedule);
        _lastUpdateTextView = findViewById(R.id.lastUpdateTextView_schedule);

        _searchField = findViewById(R.id.search_schedule);
        _searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                SerializableList<Schedule> filteredScheduleList = _scheduleService.SearchDataList(charSequence.toString());
                _scheduleListViewAdapter = new ScheduleListViewAdapter(_context, filteredScheduleList);
                _listView.setAdapter(_scheduleListViewAdapter);
                _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d schedules", filteredScheduleList.getSize()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _collapsingToolbar = findViewById(R.id.collapsing_toolbar_schedule);
        _collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        _collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _context = this;

        _receiverController = new ReceiverController(_context);

        _navigationService = NavigationService.getInstance();
        _scheduleService = ScheduleService.getInstance();

        _lastUpdateTextView.setText(_scheduleService.GetLastUpdate().toString());

        SerializableList<Schedule> scheduleList = _scheduleService.GetDataList();
        if (scheduleList.getSize() > 0) {
            _scheduleListViewAdapter = new ScheduleListViewAdapter(_context, scheduleList);
            _listView.setAdapter(_scheduleListViewAdapter);

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d schedules", scheduleList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);

        FloatingActionButton addButton = findViewById(R.id.floating_action_button_add_schedule);
        addButton.setOnClickListener(view -> {
            Bundle data = new Bundle();
            data.putSerializable(ScheduleService.ScheduleIntent, new ScheduleDto(-1, "", null, null, new SerializableTime(), SocketAction.Activate, ScheduleDto.Action.Add));

            NavigationService.NavigationResult navigationResult = _navigationService.NavigateToActivityWithData(_context, ScheduleEditActivity.class, data);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout_schedule);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_schedules);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_schedule);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _logger.Debug("onRefresh " + TAG);

            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.INVISIBLE);

            _scheduleService.LoadData();
        });
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

        _receiverController.RegisterReceiver(_scheduleUpdateReceiver, new String[]{ScheduleService.ScheduleDownloadFinishedBroadcast});

        SerializableList<Schedule> scheduleList = _scheduleService.GetDataList();
        if (scheduleList.getSize() > 0) {
            _scheduleListViewAdapter = new ScheduleListViewAdapter(_context, scheduleList);
            _listView.setAdapter(_scheduleListViewAdapter);

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d schedules", scheduleList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout_schedule);
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

        if (id == R.id.nav_socket) {
            navigationResult = _navigationService.NavigateToActivity(_context, WirelessSocketActivity.class);
        } else if (id == R.id.nav_timer) {
            navigationResult = _navigationService.NavigateToActivity(_context, TimerActivity.class);
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
        } else if (id == R.id.nav_security) {
            navigationResult = _navigationService.NavigateToActivity(_context, SecurityActivity.class);
        } else if (id == R.id.nav_settings) {
            navigationResult = _navigationService.NavigateToActivity(_context, SettingsActivity.class);
        }

        if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
            _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
            displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_schedule);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(ScheduleActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }
}
