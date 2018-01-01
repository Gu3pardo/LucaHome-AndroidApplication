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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

public class MoneyMeterDataActivity extends AppCompatBaseActivity {
    /**
     * Initiate UI
     */
    private Spinner _spinner;
    private GraphView _graphView;

    private boolean _spinnerEnabled = true;

    /**
     * BroadcastReceiver to receive updates
     */
    private BroadcastReceiver _moneyMeterUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MoneyMeterListService.MoneyMeterListDownloadFinishedContent result = (MoneyMeterListService.MoneyMeterListDownloadFinishedContent) intent.getSerializableExtra(MoneyMeterListService.MoneyMeterListDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(MoneyMeterListService.getInstance().GetLastUpdate().toString());
                updateList();
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

        TAG = MoneyMeterDataActivity.class.getSimpleName();

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

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item, MoneyMeterListService.getInstance().GetMoneyMeterDescriptionList());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinner.setAdapter(dataAdapter);
        _spinnerEnabled = false;
        _spinner.setSelection(MoneyMeterListService.getInstance().GetActiveMoneyMeter().GetTypeId(), true);
        _spinnerEnabled = true;
        _spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!_spinnerEnabled) {
                    Logger.getInstance().Warning(TAG, "_spinnerEnabled is disabled!");
                    return;
                }

                MoneyMeterListService.getInstance().SetActiveMoneyMeter(position);

                MoneyMeter activeMoneyMeter = MoneyMeterListService.getInstance().GetActiveMoneyMeter();
                SerializableList<MoneyMeterData> moneyMeterDataList = activeMoneyMeter.GetMoneyMeterDataList();
                if (moneyMeterDataList.getSize() > 0) {
                    _listView.setAdapter(new MoneyMeterListViewAdapter(_context, moneyMeterDataList));
                    _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d money meter entries", moneyMeterDataList.getSize()));
                }
                createGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        _lastUpdateTextView.setText(MoneyMeterListService.getInstance().GetLastUpdate().toString());
        updateList();

        FloatingActionButton addButton = findViewById(R.id.floating_action_button_add_moneymeterdata);
        addButton.setOnClickListener(view -> {
            int newHighestId = MoneyMeterListService.getInstance().GetHighestId() + 1;
            int newHighestTypeId = MoneyMeterListService.getInstance().GetHighestTypeId("", "") + 1;
            MoneyMeterData newMoneyMeterData = new MoneyMeterData(newHighestId, newHighestTypeId, "", "", 0, "", new SerializableDate(), UserService.getInstance().GetUser().GetName());
            Bundle data = new Bundle();
            data.putSerializable(MoneyMeterListService.MoneyMeterDataIntent, newMoneyMeterData);

            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().NavigateToActivityWithData(_context, MoneyMeterDataEditActivity.class, data);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
            }
        });

        _drawerLayout = findViewById(R.id.drawer_layout_moneymeterdata);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_moneymeter);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_moneymeterdata);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            MoneyMeterListService.getInstance().LoadData();
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
        updateList();
    }

    @Override
    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(MoneyMeterDataActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void updateList() {
        SerializableList<MoneyMeterData> moneyMeterDataList = new SerializableList<>();
        MoneyMeter activeMoneyMeter = MoneyMeterListService.getInstance().GetActiveMoneyMeter();
        if (activeMoneyMeter != null) {
            moneyMeterDataList = activeMoneyMeter.GetMoneyMeterDataList();

            _spinnerEnabled = false;
            _spinner.setSelection(activeMoneyMeter.GetTypeId(), true);
            _spinnerEnabled = true;

            createGraph();
        }

        if (moneyMeterDataList.getSize() > 0) {
            _listView.setAdapter(new MoneyMeterListViewAdapter(_context, moneyMeterDataList));

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d money meter entries", moneyMeterDataList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);
    }

    private void createGraph() {
        _graphView.removeAllSeries();

        SerializableList<MoneyMeterData> moneyMeterDataList = MoneyMeterListService.getInstance().GetActiveMoneyMeter().GetMoneyMeterDataList();
        int moneyMeterDataListSize = moneyMeterDataList.getSize();
        DataPoint[] dataPoints = new DataPoint[moneyMeterDataListSize];

        Date firstDate = new Date();
        Date lastDate = new Date();

        for (int index = 0; index < moneyMeterDataListSize; index++) {
            SerializableDate saveDate = moneyMeterDataList.getValue(index).GetSaveDate();
            Date date = new Date(saveDate.Year(), saveDate.Month() - 1, saveDate.DayOfMonth());
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
}
