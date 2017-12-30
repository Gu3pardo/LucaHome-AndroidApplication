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
import guepardoapps.lucahome.adapter.MeterListViewAdapter;
import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.Meter;
import guepardoapps.lucahome.common.classes.MeterData;
import guepardoapps.lucahome.common.service.MeterListService;
import guepardoapps.lucahome.service.NavigationService;

public class MeterDataActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MeterDataActivity.class.getSimpleName();

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
     * MeterListService manages data for meter data
     */
    private MeterListService _meterListService;

    /**
     * NavigationService manages navigation between activities
     */
    private NavigationService _navigationService;

    /**
     * Adapter for the meter list entries of the listView
     */
    private MeterListViewAdapter _meterListViewAdapter;

    /**
     * BroadcastReceiver to receive updates for the meter data
     */
    private BroadcastReceiver _meterUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MeterListService.MeterListDownloadFinishedContent result =
                    (MeterListService.MeterListDownloadFinishedContent) intent.getSerializableExtra(MeterListService.MeterListDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(_meterListService.GetLastUpdate().toString());

                SerializableList<MeterData> meterDataList = new SerializableList<>();
                Meter activeMeter = _meterListService.GetActiveMeter();
                if (activeMeter != null) {
                    meterDataList = activeMeter.GetMeterDataList();

                    _spinnerEnabled = false;
                    _spinner.setSelection(activeMeter.GetTypeId(), true);
                    _spinnerEnabled = true;

                    createGraph();
                }

                _meterListViewAdapter = new MeterListViewAdapter(_context, meterDataList);
                _listView.setAdapter(_meterListViewAdapter);

                _noDataFallback.setVisibility(View.GONE);
                _listView.setVisibility(View.VISIBLE);

                _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d meter entries", meterDataList.getSize()));
            }

            displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
            _noDataFallback.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_meterdata);

        Toolbar toolbar = findViewById(R.id.toolbar_meterdata);
        //setSupportActionBar(toolbar);

        _listView = findViewById(R.id.listView_meterdata);
        _progressBar = findViewById(R.id.progressBar_meterdata);
        _noDataFallback = findViewById(R.id.fallBackTextView_meterdata);
        _lastUpdateTextView = findViewById(R.id.lastUpdateTextView_meterdata);

        _collapsingToolbar = findViewById(R.id.collapsing_toolbar_meterdata);
        _collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        _collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _spinner = findViewById(R.id.spinner_meterdata);
        _graphView = findViewById(R.id.graph_meterdata);

        _context = this;

        _receiverController = new ReceiverController(_context);

        _meterListService = MeterListService.getInstance();
        _navigationService = NavigationService.getInstance();

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item, _meterListService.GetMeterIdList());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinner.setAdapter(dataAdapter);
        _spinnerEnabled = false;
        _spinner.setSelection(_meterListService.GetActiveMeter().GetTypeId(), true);
        _spinnerEnabled = true;
        _spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!_spinnerEnabled) {
                    Logger.getInstance().Warning(TAG, "_spinnerEnabled is disabled!");
                    return;
                }

                String selectedMeterId = _meterListService.GetMeterIdList().get(position);
                _meterListService.SetActiveMeter(selectedMeterId);

                Meter activeMeter = _meterListService.GetActiveMeter();
                SerializableList<MeterData> meterDataList = activeMeter.GetMeterDataList();
                if (meterDataList.getSize() > 0) {
                    _meterListViewAdapter = new MeterListViewAdapter(_context, meterDataList);
                    _listView.setAdapter(_meterListViewAdapter);
                    _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d meter entries", meterDataList.getSize()));
                }
                createGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        _lastUpdateTextView.setText(_meterListService.GetLastUpdate().toString());

        SerializableList<MeterData> meterDataList = new SerializableList<>();
        Meter activeMeter = _meterListService.GetActiveMeter();
        if (activeMeter != null) {
            meterDataList = activeMeter.GetMeterDataList();

            _spinnerEnabled = false;
            _spinner.setSelection(activeMeter.GetTypeId(), true);
            _spinnerEnabled = true;

            createGraph();
        }

        if (meterDataList.getSize() > 0) {
            _meterListViewAdapter = new MeterListViewAdapter(_context, meterDataList);
            _listView.setAdapter(_meterListViewAdapter);

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d meter entries", meterDataList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);

        FloatingActionButton addButton = findViewById(R.id.floating_action_button_add_meterdata);
        addButton.setOnClickListener(view -> {
            int newHighestId = _meterListService.GetHighestId() + 1;
            int newHighestTypeId = _meterListService.GetHighestTypeId("") + 1;
            MeterData newMeterData = new MeterData(newHighestId, "", newHighestTypeId, new SerializableDate(), new SerializableTime(), "", "", 0, "");
            Bundle data = new Bundle();
            data.putSerializable(MeterListService.MeterDataIntent, newMeterData);

            NavigationService.NavigationResult navigationResult = _navigationService.NavigateToActivityWithData(_context, MeterDataEditActivity.class, data);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout_meterdata);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_meter);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_meterdata);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _meterListService.LoadData();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        _receiverController.RegisterReceiver(_meterUpdateReceiver, new String[]{MeterListService.MeterListDownloadFinishedBroadcast});

        SerializableList<MeterData> meterDataList = new SerializableList<>();
        Meter activeMeter = _meterListService.GetActiveMeter();
        if (activeMeter != null) {
            meterDataList = activeMeter.GetMeterDataList();

            _spinnerEnabled = false;
            _spinner.setSelection(activeMeter.GetTypeId(), true);
            _spinnerEnabled = true;

            createGraph();
        }

        if (meterDataList.getSize() > 0) {
            _meterListViewAdapter = new MeterListViewAdapter(_context, meterDataList);
            _listView.setAdapter(_meterListViewAdapter);

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d meter entries", meterDataList.getSize()));
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout_meterdata);
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
        } else if (id == R.id.nav_money) {
            navigationResult = _navigationService.NavigateToActivity(_context, MoneyMeterDataActivity.class);
        }

        if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
            Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
            displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_meterdata);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createGraph() {
        _graphView.removeAllSeries();

        SerializableList<MeterData> meterDataList = _meterListService.GetActiveMeter().GetMeterDataList();
        int meterDataListSize = meterDataList.getSize();
        DataPoint[] dataPoints = new DataPoint[meterDataListSize];

        Date firstDate = new Date();
        Date lastDate = new Date();

        for (int index = 0; index < meterDataListSize; index++) {
            SerializableDate saveDate = meterDataList.getValue(index).GetSaveDate();
            SerializableTime saveTime = meterDataList.getValue(index).GetSaveTime();
            Date date = new Date(saveDate.Year(), saveDate.Month(), saveDate.DayOfMonth(), saveTime.Hour(), saveTime.Minute());
            dataPoints[index] = new DataPoint(date, meterDataList.getValue(index).GetValue());
            if (index == 0) {
                firstDate = date;
            } else if (index == meterDataListSize - 1) {
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
                .setActivty(MeterDataActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }
}
