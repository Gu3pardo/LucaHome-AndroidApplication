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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.rey.material.widget.FloatingActionButton;

import java.util.Date;
import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.adapter.MoneyMeterListViewAdapter;
import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.MoneyMeter;
import guepardoapps.lucahome.common.classes.MoneyMeterData;
import guepardoapps.lucahome.common.service.MoneyMeterListService;
import guepardoapps.lucahome.common.service.UserService;
import guepardoapps.lucahome.service.NavigationService;

public class MoneyMeterDataActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MoneyMeterDataActivity.class.getSimpleName();

    private Context _context;

    /**
     * Initiate UI
     */
    private ProgressBar _progressBar;
    private Spinner _spinner;
    private GraphView _graphView;
    private ListView _listView;
    private TextView _noDataFallback;
    private TextView _lastUpdateTextView;
    private CollapsingToolbarLayout _collapsingToolbar;
    private PullRefreshLayout _pullRefreshLayout;

    private boolean _spinnerEnabled = true;

    /**
     * ReceiverController to register and unregister from broadcasts of the UserService
     */
    private ReceiverController _receiverController;

    /**
     * MoneyMeterListService manages data for money meter data
     */
    private MoneyMeterListService _moneyMeterListService;

    /**
     * NavigationService manages navigation between activities
     */
    private NavigationService _navigationService;

    /**
     * Adapter for the money meter list entries of the listView
     */
    private MoneyMeterListViewAdapter _moneyMeterListViewAdapter;

    /**
     * BroadcastReceiver to receive updates for the money meter data
     */
    private BroadcastReceiver _moneyMeterUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MoneyMeterListService.MoneyMeterListDownloadFinishedContent result =
                    (MoneyMeterListService.MoneyMeterListDownloadFinishedContent) intent.getSerializableExtra(MoneyMeterListService.MoneyMeterListDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(_moneyMeterListService.GetLastUpdate().toString());

                SerializableList<MoneyMeterData> moneyMeterDataList = new SerializableList<>();
                MoneyMeter activeMoneyMeter = _moneyMeterListService.GetActiveMoneyMeter();
                if (activeMoneyMeter != null) {
                    moneyMeterDataList = activeMoneyMeter.GetMoneyMeterDataList();

                    _spinnerEnabled = false;
                    _spinner.setSelection(activeMoneyMeter.GetTypeId(), true);
                    _spinnerEnabled = true;

                    createGraph();
                }

                _moneyMeterListViewAdapter = new MoneyMeterListViewAdapter(_context, moneyMeterDataList);
                _listView.setAdapter(_moneyMeterListViewAdapter);

                _noDataFallback.setVisibility(View.GONE);
                _listView.setVisibility(View.VISIBLE);

                _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d money meter entries", moneyMeterDataList.getSize()));
            }

            displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
            _noDataFallback.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_moneymeterdata);

        Toolbar toolbar = findViewById(R.id.toolbar_moneymeterdata);
        //setSupportActionBar(toolbar);

        _listView = findViewById(R.id.listView_moneymeterdata);
        _progressBar = findViewById(R.id.progressBar_moneymeterdata);
        _noDataFallback = findViewById(R.id.fallBackTextView_moneymeterdata);
        _lastUpdateTextView = findViewById(R.id.lastUpdateTextView_moneymeterdata);

        _collapsingToolbar = findViewById(R.id.collapsing_toolbar_moneymeterdata);
        _collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        _collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _spinner = findViewById(R.id.spinner_moneymeterdata);
        _graphView = findViewById(R.id.graph_moneymeterdata);

        _context = this;

        _receiverController = new ReceiverController(_context);

        _moneyMeterListService = MoneyMeterListService.getInstance();
        _navigationService = NavigationService.getInstance();

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item, _moneyMeterListService.GetMoneyMeterDescriptionList());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinner.setAdapter(dataAdapter);
        _spinnerEnabled = false;
        _spinner.setSelection(_moneyMeterListService.GetActiveMoneyMeter().GetTypeId(), true);
        _spinnerEnabled = true;
        _spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!_spinnerEnabled) {
                    Logger.getInstance().Warning(TAG, "_spinnerEnabled is disabled!");
                    return;
                }

                _moneyMeterListService.SetActiveMoneyMeter(position);

                MoneyMeter activeMoneyMeter = _moneyMeterListService.GetActiveMoneyMeter();
                SerializableList<MoneyMeterData> moneyMeterDataList = activeMoneyMeter.GetMoneyMeterDataList();
                if (moneyMeterDataList.getSize() > 0) {
                    _moneyMeterListViewAdapter = new MoneyMeterListViewAdapter(_context, moneyMeterDataList);
                    _listView.setAdapter(_moneyMeterListViewAdapter);
                    _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d money meter entries", moneyMeterDataList.getSize()));
                }
                createGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        _lastUpdateTextView.setText(_moneyMeterListService.GetLastUpdate().toString());

        SerializableList<MoneyMeterData> moneyMeterDataList = new SerializableList<>();
        MoneyMeter activeMoneyMeter = _moneyMeterListService.GetActiveMoneyMeter();
        if (activeMoneyMeter != null) {
            moneyMeterDataList = activeMoneyMeter.GetMoneyMeterDataList();

            _spinnerEnabled = false;
            _spinner.setSelection(activeMoneyMeter.GetTypeId(), true);
            _spinnerEnabled = true;

            createGraph();
        }

        if (moneyMeterDataList.getSize() > 0) {
            _moneyMeterListViewAdapter = new MoneyMeterListViewAdapter(_context, moneyMeterDataList);
            _listView.setAdapter(_moneyMeterListViewAdapter);

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d money meter entries", moneyMeterDataList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);

        FloatingActionButton addButton = findViewById(R.id.floating_action_button_add_moneymeterdata);
        addButton.setOnClickListener(view -> {
            int newHighestId = _moneyMeterListService.GetHighestId() + 1;
            int newHighestTypeId = _moneyMeterListService.GetHighestTypeId("", "") + 1;
            MoneyMeterData newMoneyMeterData = new MoneyMeterData(newHighestId, newHighestTypeId, "", "", 0, "", new SerializableDate(), UserService.getInstance().GetUser().GetName());
            Bundle data = new Bundle();
            data.putSerializable(MoneyMeterListService.MoneyMeterDataIntent, newMoneyMeterData);

            NavigationService.NavigationResult navigationResult = _navigationService.NavigateToActivityWithData(_context, MoneyMeterDataEditActivity.class, data);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout_moneymeterdata);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_moneymeter);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_moneymeterdata);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _moneyMeterListService.LoadData();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        _receiverController.RegisterReceiver(_moneyMeterUpdateReceiver, new String[]{MoneyMeterListService.MoneyMeterListDownloadFinishedBroadcast});

        SerializableList<MoneyMeterData> moneyMeterDataList = new SerializableList<>();
        MoneyMeter activeMoneyMeter = _moneyMeterListService.GetActiveMoneyMeter();
        if (activeMoneyMeter != null) {
            moneyMeterDataList = activeMoneyMeter.GetMoneyMeterDataList();

            _spinnerEnabled = false;
            _spinner.setSelection(activeMoneyMeter.GetTypeId(), true);
            _spinnerEnabled = true;

            createGraph();
        }

        if (moneyMeterDataList.getSize() > 0) {
            _moneyMeterListViewAdapter = new MoneyMeterListViewAdapter(_context, moneyMeterDataList);
            _listView.setAdapter(_moneyMeterListViewAdapter);

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d money meter entries", moneyMeterDataList.getSize()));
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout_moneymeterdata);
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
        } else if (id == R.id.nav_schedule) {
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
        } else if (id == R.id.nav_security) {
            navigationResult = _navigationService.NavigateToActivity(_context, SecurityActivity.class);
        } else if (id == R.id.nav_settings) {
            navigationResult = _navigationService.NavigateToActivity(_context, SettingsActivity.class);
        } else if (id == R.id.nav_switch) {
            navigationResult = _navigationService.NavigateToActivity(_context, WirelessSwitchActivity.class);
        } else if (id == R.id.nav_birthday) {
            navigationResult = _navigationService.NavigateToActivity(_context, BirthdayActivity.class);
        } else if (id == R.id.nav_meter) {
            navigationResult = _navigationService.NavigateToActivity(_context, MeterDataActivity.class);
        }

        if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
            Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
            displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_moneymeterdata);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createGraph() {
        _graphView.removeAllSeries();

        SerializableList<MoneyMeterData> moneyMeterDataList = _moneyMeterListService.GetActiveMoneyMeter().GetMoneyMeterDataList();
        int moneyMeterDataListSize = moneyMeterDataList.getSize();
        DataPoint[] dataPoints = new DataPoint[moneyMeterDataListSize];

        Date firstDate = new Date();
        Date lastDate = new Date();

        for (int index = 0; index < moneyMeterDataListSize; index++) {
            SerializableDate saveDate = moneyMeterDataList.getValue(index).GetSaveDate();
            Date date = new Date(saveDate.Year(), saveDate.Month(), saveDate.DayOfMonth());
            dataPoints[index] = new DataPoint(date, moneyMeterDataList.getValue(index).GetAmount());
            if (index == 0) {
                firstDate = date;
            } else if (index == moneyMeterDataListSize - 1) {
                lastDate = date;
            }
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        _graphView.addSeries(series);

        _graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        _graphView.getGridLabelRenderer().setNumHorizontalLabels(3);

        _graphView.getViewport().setMinX(firstDate.getTime());
        _graphView.getViewport().setMaxX(lastDate.getTime());
        _graphView.getViewport().setXAxisBoundsManual(true);

        _graphView.getGridLabelRenderer().setHumanRounding(false);
    }

    private void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(MoneyMeterDataActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }
}
