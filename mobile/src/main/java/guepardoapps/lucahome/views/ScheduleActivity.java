package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;

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
import guepardoapps.lucahome.common.enums.Weekday;
import guepardoapps.lucahome.common.service.ScheduleService;
import guepardoapps.lucahome.service.NavigationService;

public class ScheduleActivity extends AppCompatBaseActivity {
    /**
     * BroadcastReceiver to receive updates
     */
    private BroadcastReceiver _scheduleUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ScheduleService.ScheduleDownloadFinishedContent result = (ScheduleService.ScheduleDownloadFinishedContent) intent.getSerializableExtra(ScheduleService.ScheduleDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(ScheduleService.getInstance().GetLastUpdate().toString());
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

        TAG = ScheduleActivity.class.getSimpleName();

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
                SerializableList<Schedule> filteredScheduleList = ScheduleService.getInstance().SearchDataList(charSequence.toString());
                _listView.setAdapter(new ScheduleListViewAdapter(_context, filteredScheduleList));
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

        _lastUpdateTextView.setText(ScheduleService.getInstance().GetLastUpdate().toString());
        updateList();

        FloatingActionButton addButton = findViewById(R.id.floating_action_button_add_schedule);
        addButton.setOnClickListener(view -> {
            Bundle data = new Bundle();
            data.putSerializable(ScheduleService.ScheduleIntent, new ScheduleDto(-1, "", null, null, Weekday.NULL, new SerializableTime(), SocketAction.Activate, ScheduleDto.Action.Add));

            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().NavigateToActivityWithData(_context, ScheduleEditActivity.class, data);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
            }
        });

        _drawerLayout = findViewById(R.id.drawer_layout_schedule);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_schedules);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_schedule);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.INVISIBLE);
            ScheduleService.getInstance().LoadData();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_scheduleUpdateReceiver, new String[]{ScheduleService.ScheduleDownloadFinishedBroadcast});
        updateList();
    }

    @Override
    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(ScheduleActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void updateList() {
        SerializableList<Schedule> scheduleList = ScheduleService.getInstance().GetDataList();
        if (scheduleList.getSize() > 0) {
            _listView.setAdapter(new ScheduleListViewAdapter(_context, scheduleList));

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d schedules", scheduleList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);
    }
}
