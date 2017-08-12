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
import guepardoapps.lucahome.adapter.MenuListViewAdapter;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaMenu;
import guepardoapps.lucahome.common.service.MenuService;
import guepardoapps.lucahome.service.NavigationService;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MenuActivity.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    /**
     * Initiate UI
     */
    private EditText _searchField;
    private ProgressBar _progressBar;
    private ListView _listView;
    private TextView _noDataFallback;
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
     * MenuService manages data for menus
     */
    private MenuService _menuService;

    /**
     * Adapter for the birthday entries of the listView
     */
    private MenuListViewAdapter _menuListViewAdapter;

    /**
     * BroadcastReceiver to receive updates for the menu
     */
    private BroadcastReceiver _menuUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_menuUpdateReceiver");
            MenuService.MenuDownloadFinishedContent result =
                    (MenuService.MenuDownloadFinishedContent) intent.getSerializableExtra(MenuService.MenuDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                if (result.MenuList != null) {
                    if (result.MenuList.getSize() > 0) {
                        _menuListViewAdapter = new MenuListViewAdapter(_context, result.MenuList);
                        _listView.setAdapter(_menuListViewAdapter);

                        _noDataFallback.setVisibility(View.GONE);
                        _listView.setVisibility(View.VISIBLE);
                        _searchField.setVisibility(View.VISIBLE);
                    } else {
                        _noDataFallback.setVisibility(View.VISIBLE);
                        _searchField.setVisibility(View.INVISIBLE);
                    }
                } else {
                    Snacky.builder()
                            .setActivty(MenuActivity.this)
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
                        .setActivty(MenuActivity.this)
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

        setContentView(R.layout.activity_menu);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_menu);
        //setSupportActionBar(toolbar);

        _listView = (ListView) findViewById(R.id.listView_menu);
        _progressBar = (ProgressBar) findViewById(R.id.progressBar_menu);
        _noDataFallback = (TextView) findViewById(R.id.fallBackTextView_menu);

        _searchField = (EditText) findViewById(R.id.search_menu);
        _searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                SerializableList<LucaMenu> filteredMenuList = _menuService.FoundMenus(charSequence.toString());
                _menuListViewAdapter = new MenuListViewAdapter(_context, filteredMenuList);
                _listView.setAdapter(_menuListViewAdapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _context = this;

        _receiverController = new ReceiverController(_context);

        _navigationService = NavigationService.getInstance();
        _menuService = MenuService.getInstance();

        SerializableList<LucaMenu> menuList = _menuService.GetMenuList();
        if (menuList.getSize() > 0) {
            _menuListViewAdapter = new MenuListViewAdapter(_context, menuList);
            _listView.setAdapter(_menuListViewAdapter);

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);
        }
        _progressBar.setVisibility(View.GONE);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_menu);
        collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_menu);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_menu);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.pullRefreshLayout_menu);
        _pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                _logger.Debug("onRefresh " + TAG);

                _listView.setVisibility(View.GONE);
                _progressBar.setVisibility(View.VISIBLE);
                _searchField.setVisibility(View.INVISIBLE);

                _menuService.LoadMenuList();
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

        _receiverController.RegisterReceiver(_menuUpdateReceiver, new String[]{MenuService.MenuDownloadFinishedBroadcast});

        SerializableList<LucaMenu> menuList = _menuService.GetMenuList();
        if (menuList.getSize() > 0) {
            _menuListViewAdapter = new MenuListViewAdapter(_context, menuList);
            _listView.setAdapter(_menuListViewAdapter);

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_menu);
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
        } else if (id == R.id.nav_coins) {
            navigationResult = _navigationService.NavigateToActivity(_context, CoinActivity.class);
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
                    .setActivty(MenuActivity.this)
                    .setText("Failed to navigate! Please contact LucaHome support!")
                    .setDuration(Snacky.LENGTH_INDEFINITE)
                    .setActionText(android.R.string.ok)
                    .error()
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_menu);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
