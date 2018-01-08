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

@SuppressWarnings("deprecation")
public class MeterDataActivity extends AppCompatBaseActivity {
    /**
     * Initiate UI
     */
    private Spinner _spinner;
    private GraphView _graphView;

    private boolean _spinnerEnabled = true;

    /**
     * BroadcastReceiver to receive the event after download of meter data has finished
     */
    private BroadcastReceiver _meterDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MeterListService.MeterListDownloadFinishedContent result = (MeterListService.MeterListDownloadFinishedContent) intent.getSerializableExtra(MeterListService.MeterListDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(MeterListService.getInstance().GetLastUpdate().toString());
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

        TAG = MeterDataActivity.class.getSimpleName();

        setContentView(R.layout.activity_meterdata);

        int typeId = getIntent().getIntExtra(MeterListService.MeterDataIntent, 0);
        Meter meter = MeterListService.getInstance().GetByTypeId(typeId);

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

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item, MeterListService.getInstance().GetMeterIdList());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinner.setAdapter(dataAdapter);
        _spinnerEnabled = false;
        if (meter != null) {
            MeterListService.getInstance().SetActiveMeter(meter.GetMeterId());
        }
        _spinner.setSelection(MeterListService.getInstance().GetActiveMeter().GetTypeId(), true);
        _spinnerEnabled = true;
        _spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!_spinnerEnabled) {
                    Logger.getInstance().Warning(TAG, "_spinnerEnabled is disabled!");
                    return;
                }

                String selectedMeterId = MeterListService.getInstance().GetMeterIdList().get(position);
                MeterListService.getInstance().SetActiveMeter(selectedMeterId);

                Meter activeMeter = MeterListService.getInstance().GetActiveMeter();
                SerializableList<MeterData> meterDataList = activeMeter.GetMeterDataList();
                if (meterDataList.getSize() > 0) {
                    _listView.setAdapter(new MeterListViewAdapter(_context, meterDataList));
                    _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d meter entries", meterDataList.getSize()));
                }
                createGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        _lastUpdateTextView.setText(MeterListService.getInstance().GetLastUpdate().toString());
        updateList();

        FloatingActionButton addButton = findViewById(R.id.floating_action_button_add_meterdata);
        addButton.setOnClickListener(view -> {
            int newHighestId = MeterListService.getInstance().GetHighestId() + 1;
            int newHighestTypeId = MeterListService.getInstance().GetHighestTypeId("") + 1;
            MeterData newMeterData = new MeterData(newHighestId, "", newHighestTypeId, new SerializableDate(), new SerializableTime(), "", "", 0, "");
            Bundle data = new Bundle();
            data.putSerializable(MeterListService.MeterDataIntent, newMeterData);

            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().NavigateToActivityWithData(_context, MeterDataEditActivity.class, data);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
            }
        });

        _drawerLayout = findViewById(R.id.drawer_layout_meterdata);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_meter);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_meterdata);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            MeterListService.getInstance().LoadData();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_meterDownloadReceiver, new String[]{MeterListService.MeterListDownloadFinishedBroadcast});
        updateList();
    }

    @Override
    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(MeterDataActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void updateList() {
        SerializableList<MeterData> meterDataList = new SerializableList<>();
        Meter activeMeter = MeterListService.getInstance().GetActiveMeter();
        if (activeMeter != null) {
            meterDataList = activeMeter.GetMeterDataList();

            _spinnerEnabled = false;
            _spinner.setSelection(activeMeter.GetTypeId(), true);
            _spinnerEnabled = true;

            createGraph();
        }

        if (meterDataList.getSize() > 0) {
            _listView.setAdapter(new MeterListViewAdapter(_context, meterDataList));

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d meter entries", meterDataList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);
    }

    private void createGraph() {
        _graphView.removeAllSeries();

        SerializableList<MeterData> meterDataList = MeterListService.getInstance().GetActiveMeter().GetMeterDataList();
        int meterDataListSize = meterDataList.getSize();
        DataPoint[] dataPoints = new DataPoint[meterDataListSize];

        Date firstDate = new Date();
        Date lastDate = new Date();

        for (int index = 0; index < meterDataListSize; index++) {
            SerializableDate saveDate = meterDataList.getValue(index).GetSaveDate();
            SerializableTime saveTime = meterDataList.getValue(index).GetSaveTime();
            Date date = new Date(saveDate.Year(), saveDate.Month() - 1, saveDate.DayOfMonth(), saveTime.Hour(), saveTime.Minute());
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
}
