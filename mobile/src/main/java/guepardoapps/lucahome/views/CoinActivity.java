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
import guepardoapps.lucahome.adapter.CoinListViewAdapter;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.Coin;
import guepardoapps.lucahome.common.dto.CoinDto;
import guepardoapps.lucahome.common.service.CoinService;
import guepardoapps.lucahome.service.NavigationService;

public class CoinActivity extends AppCompatBaseActivity {
    /**
     * BroadcastReceiver to receive updates
     */
    private BroadcastReceiver _coinUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CoinService.CoinDownloadFinishedContent result = (CoinService.CoinDownloadFinishedContent) intent.getSerializableExtra(CoinService.CoinDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(CoinService.getInstance().GetLastUpdate().toString());
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

        TAG = CoinActivity.class.getSimpleName();

        setContentView(R.layout.activity_coin);

        Toolbar toolbar = findViewById(R.id.toolbar_coin);
        //setSupportActionBar(toolbar);
        _drawerLayout = findViewById(R.id.drawer_layout_coin);

        _listView = findViewById(R.id.listView_coin);
        _progressBar = findViewById(R.id.progressBar_coin);
        _noDataFallback = findViewById(R.id.fallBackTextView_coin);
        _lastUpdateTextView = findViewById(R.id.lastUpdateTextView_coin);

        _searchField = findViewById(R.id.search_coin);
        _searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                SerializableList<Coin> filteredCoinList = CoinService.getInstance().SearchDataList(charSequence.toString());
                _listView.setAdapter(new CoinListViewAdapter(_context, filteredCoinList));
                _collapsingToolbar.setTitle(CoinService.getInstance().FilteredCoinsValue());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _collapsingToolbar = findViewById(R.id.collapsing_toolbar_coin);
        _collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        _collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _context = this;

        _receiverController = new ReceiverController(_context);

        _lastUpdateTextView.setText(CoinService.getInstance().GetLastUpdate().toString());

        FloatingActionButton addButton = findViewById(R.id.floating_action_button_add_coin);
        addButton.setOnClickListener(view -> {
            Bundle data = new Bundle();
            data.putSerializable(CoinService.CoinIntent, new CoinDto(-1, "", "", 0, CoinDto.Action.Add));

            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().NavigateToActivityWithData(_context, CoinEditActivity.class, data);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
            }
        });

        _drawerLayout = findViewById(R.id.drawer_layout_coin);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_coin);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_coin);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.INVISIBLE);
            CoinService.getInstance().LoadCoinConversionList();
        });

        updateList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_coinUpdateReceiver, new String[]{CoinService.CoinDownloadFinishedBroadcast});
        updateList();
    }

    @Override
    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(CoinActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void updateList() {
        SerializableList<Coin> coinList = CoinService.getInstance().GetDataList();
        if (coinList.getSize() > 0) {
            _listView.setAdapter(new CoinListViewAdapter(_context, coinList));

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(CoinService.getInstance().AllCoinsValue());
        }
        _progressBar.setVisibility(View.GONE);
    }
}
