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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

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

public class WirelessSwitchActivity extends AppCompatBaseActivity {
    /**
     * BroadcastReceiver to receive updates
     */
    private BroadcastReceiver _wirelessSwitchUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WirelessSwitchService.WirelessSwitchDownloadFinishedContent result = (WirelessSwitchService.WirelessSwitchDownloadFinishedContent) intent.getSerializableExtra(WirelessSwitchService.WirelessSwitchDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(WirelessSwitchService.getInstance().GetLastUpdate().toString());
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

        TAG = WirelessSwitchActivity.class.getSimpleName();

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
                SerializableList<WirelessSwitch> filteredWirelessSwitchList = WirelessSwitchService.getInstance().SearchDataList(charSequence.toString());
                _listView.setAdapter(new SwitchListViewAdapter(_context, filteredWirelessSwitchList));
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

        _lastUpdateTextView.setText(WirelessSwitchService.getInstance().GetLastUpdate().toString());
        updateList();

        FloatingActionButton addButton = findViewById(R.id.floating_action_button_add_wireless_switch);
        addButton.setOnClickListener(view -> {
            Bundle data = new Bundle();
            data.putSerializable(WirelessSwitchService.WirelessSwitchIntent, new WirelessSwitchDto(-1, "", "", -1, '1', WirelessSwitchDto.Action.Add));

            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().NavigateToActivityWithData(_context, WirelessSwitchEditActivity.class, data);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
            }
        });

        _drawerLayout = findViewById(R.id.drawer_layout_switches);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_switch);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_switches);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            WirelessSwitchService.getInstance().LoadData();
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
        updateList();
    }

    @Override
    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(WirelessSwitchActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void updateList() {
        SerializableList<WirelessSwitch> wirelessSwitchList = WirelessSwitchService.getInstance().GetDataList();
        if (wirelessSwitchList.getSize() > 0) {
            _listView.setAdapter(new SwitchListViewAdapter(_context, wirelessSwitchList));

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d switches", wirelessSwitchList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);
    }
}
