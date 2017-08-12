package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
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

public class CoinActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = CoinActivity.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    /**
     * Initiate UI
     */
    private EditText _searchField;
    private ProgressBar _progressBar;
    private ListView _listView;
    private TextView _noDataFallback;
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
     * CoinService manages data for coins
     */
    private CoinService _coinService;

    /**
     * Adapter for the coin entries of the listView
     */
    private CoinListViewAdapter _coinListViewAdapter;

    /**
     * BroadcastReceiver to receive updates for the birthdays
     */
    private BroadcastReceiver _coinUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_coinUpdateReceiver");
            CoinService.CoinDownloadFinishedContent result =
                    (CoinService.CoinDownloadFinishedContent) intent.getSerializableExtra(CoinService.CoinDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                if (result.CoinList != null) {
                    if (result.CoinList.getSize() > 0) {
                        _coinListViewAdapter = new CoinListViewAdapter(_context, result.CoinList);
                        _listView.setAdapter(_coinListViewAdapter);

                        _noDataFallback.setVisibility(View.GONE);
                        _listView.setVisibility(View.VISIBLE);
                        _searchField.setVisibility(View.VISIBLE);

                        _collapsingToolbar.setTitle(_coinService.AllCoinsValue());
                    } else {
                        _collapsingToolbar.setTitle("0�");
                        _noDataFallback.setVisibility(View.VISIBLE);
                        _searchField.setVisibility(View.INVISIBLE);
                    }
                } else {
                    Snacky.builder()
                            .setActivty(CoinActivity.this)
                            .setText(Tools.DecompressByteArrayToString(result.Response))
                            .setDuration(Snacky.LENGTH_INDEFINITE)
                            .setActionText(android.R.string.ok)
                            .error()
                            .show();
                    _noDataFallback.setVisibility(View.VISIBLE);
                    _searchField.setVisibility(View.INVISIBLE);
                }
            } else {
                Snacky.builder()
                        .setActivty(CoinActivity.this)
                        .setText(Tools.DecompressByteArrayToString(result.Response))
                        .setDuration(Snacky.LENGTH_INDEFINITE)
                        .setActionText(android.R.string.ok)
                        .error()
                        .show();
                _noDataFallback.setVisibility(View.VISIBLE);
                _searchField.setVisibility(View.INVISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _logger = new Logger(TAG);
        _logger.Debug("onCreate");

        setContentView(R.layout.activity_coin);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_coin);
        //setSupportActionBar(toolbar);

        _listView = (ListView) findViewById(R.id.listView_coin);
        _progressBar = (ProgressBar) findViewById(R.id.progressBar_coin);
        _noDataFallback = (TextView) findViewById(R.id.fallBackTextView_coin);

        _searchField = (EditText) findViewById(R.id.search_coin);
        _searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                SerializableList<Coin> filteredCoinList = _coinService.FoundCoins(charSequence.toString());
                _coinListViewAdapter = new CoinListViewAdapter(_context, filteredCoinList);
                _listView.setAdapter(_coinListViewAdapter);
                _collapsingToolbar.setTitle(_coinService.FilteredCoinsValue());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_coin);
        _collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        _collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _context = this;

        _receiverController = new ReceiverController(_context);

        _navigationService = NavigationService.getInstance();
        _coinService = CoinService.getInstance();

        SerializableList<Coin> coinList = _coinService.GetCoinList();
        if (coinList.getSize() > 0) {
            _coinListViewAdapter = new CoinListViewAdapter(_context, coinList);
            _listView.setAdapter(_coinListViewAdapter);

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(_coinService.AllCoinsValue());
        }
        _progressBar.setVisibility(View.GONE);

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.floating_action_button_add_coin);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle data = new Bundle();
                data.putSerializable(CoinService.CoinIntent, new CoinDto(-1, "", "", 0, CoinDto.Action.Add));

                NavigationService.NavigationResult navigationResult = _navigationService.NavigateToActivityWithData(_context, CoinEditActivity.class, data);
                if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                    _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));

                    Snacky.builder()
                            .setActivty(CoinActivity.this)
                            .setText("Failed to navigate! Please contact LucaHome support!")
                            .setDuration(Snacky.LENGTH_INDEFINITE)
                            .setActionText(android.R.string.ok)
                            .error()
                            .show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_coin);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_coin);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.pullRefreshLayout_coin);
        _pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                _logger.Debug("onRefresh " + TAG);

                _listView.setVisibility(View.GONE);
                _progressBar.setVisibility(View.VISIBLE);
                _searchField.setVisibility(View.INVISIBLE);

                _coinService.LoadCoinConversionList();
            }
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

        _receiverController.RegisterReceiver(_coinUpdateReceiver, new String[]{CoinService.CoinDownloadFinishedBroadcast});

        SerializableList<Coin> coinList = _coinService.GetCoinList();
        if (coinList.getSize() > 0) {
            _coinListViewAdapter = new CoinListViewAdapter(_context, coinList);
            _listView.setAdapter(_coinListViewAdapter);

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(_coinService.AllCoinsValue());
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_coin);
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
            navigationResult = _navigationService.NavigateToActivity(_context, MediaMirrorActivity.class);
        } else if (id == R.id.nav_library) {
            navigationResult = _navigationService.NavigateToActivity(_context, LibraryActivity.class);
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

            Snacky.builder()
                    .setActivty(CoinActivity.this)
                    .setText("Failed to navigate! Please contact LucaHome support!")
                    .setDuration(Snacky.LENGTH_INDEFINITE)
                    .setActionText(android.R.string.ok)
                    .error()
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_coin);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
