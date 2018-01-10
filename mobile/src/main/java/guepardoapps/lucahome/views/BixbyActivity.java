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

import guepardoapps.bixby.classes.BixbyPair;
import guepardoapps.bixby.classes.actions.BixbyAction;
import guepardoapps.bixby.services.BixbyPairService;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.adapter.BixbyPairListViewAdapter;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.service.NavigationService;

public class BixbyActivity extends AppCompatBaseActivity {
    /**
     * BroadcastReceiver to receive the event after load of bixby data has finished
     */
    private BroadcastReceiver _bixbyLoadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BixbyPairService.BixbyPairUpdateFinishedContent result = (BixbyPairService.BixbyPairUpdateFinishedContent) intent.getSerializableExtra(BixbyPairService.BixbyPairUpdateFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(BixbyPairService.getInstance().GetLastUpdate().toString());
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

        TAG = BixbyActivity.class.getSimpleName();

        setContentView(R.layout.activity_bixby);

        Toolbar toolbar = findViewById(R.id.toolbar_bixby);
        //setSupportActionBar(toolbar);
        _drawerLayout = findViewById(R.id.drawer_layout_bixby);

        _listView = findViewById(R.id.listView_bixby);
        _progressBar = findViewById(R.id.progressBar_bixby);
        _noDataFallback = findViewById(R.id.fallBackTextView_bixby);
        _lastUpdateTextView = findViewById(R.id.lastUpdateTextView_bixby);

        _searchField = findViewById(R.id.search_bixby);
        _searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                SerializableList<BixbyPair> filteredBixbyPairList = BixbyPairService.getInstance().SearchDataList(charSequence.toString());
                _listView.setAdapter(new BixbyPairListViewAdapter(_context, filteredBixbyPairList));
                _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d bixby entries", filteredBixbyPairList.getSize()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _collapsingToolbar = findViewById(R.id.collapsing_toolbar_bixby);
        _collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        _collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _context = this;

        _receiverController = new ReceiverController(_context);

        _lastUpdateTextView.setText(BixbyPairService.getInstance().GetLastUpdate().toString());

        FloatingActionButton addButton = findViewById(R.id.floating_action_button_add_bixby);
        addButton.setOnClickListener(view -> {
            Bundle data = new Bundle();
            data.putSerializable(
                    BixbyPairService.BIXBY_PAIR_INTENT,
                    new BixbyPair(
                            BixbyPairService.getInstance().GetHighestId() + 1,
                            new BixbyAction(BixbyPairService.getInstance().GetHighestActionId() + 1, BixbyPairService.getInstance().GetHighestId() + 1),
                            new SerializableList<>()));

            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().NavigateToActivityWithData(_context, BixbyEditActivity.class, data);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
            }
        });

        _drawerLayout = findViewById(R.id.drawer_layout_bixby);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_bixby);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_bixby);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.INVISIBLE);
            BixbyPairService.getInstance().LoadData();
        });

        updateList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_bixbyLoadReceiver, new String[]{BixbyPairService.BixbyPairLoadFinishedBroadcast});
        updateList();
    }

    @Override
    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(BixbyActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void updateList() {
        SerializableList<BixbyPair> bixbyPairList = BixbyPairService.getInstance().GetDataList();
        if (bixbyPairList.getSize() > 0) {
            _listView.setAdapter(new BixbyPairListViewAdapter(_context, bixbyPairList));

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d bixby entries", bixbyPairList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);
    }
}
