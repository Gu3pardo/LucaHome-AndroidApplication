package guepardoapps.lucahome.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;

import de.mateware.snacky.Snacky;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.service.NavigationService;

public abstract class AppCompatBaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected static String TAG = AppCompatBaseActivity.class.getSimpleName();

    /**
     * Initiate UI
     */
    protected DrawerLayout _drawerLayout;
    protected EditText _searchField;
    protected ProgressBar _progressBar;
    protected ListView _listView;
    protected TextView _noDataFallback;
    protected TextView _lastUpdateTextView;
    protected CollapsingToolbarLayout _collapsingToolbar;
    protected PullRefreshLayout _pullRefreshLayout;
    protected ImageView _mainImageView;
    protected FloatingActionButton _addButton;
    protected FloatingActionButton _shareButton;

    protected Context _context;

    /**
     * ReceiverController to register and unregister broadcasts
     */
    protected ReceiverController _receiverController;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        if (_drawerLayout.isDrawerOpen(GravityCompat.START)) {
            _drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            NavigationService.getInstance().GoBack(_context);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        NavigationService.NavigationResult navigationResult = NavigationService.NavigationResult.NULL;

        if (id == R.id.nav_socket) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, WirelessSocketActivity.class);
        } else if (id == R.id.nav_schedule) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, ScheduleActivity.class);
        } else if (id == R.id.nav_timer) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, TimerActivity.class);
        } else if (id == R.id.nav_movie) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, MovieActivity.class);
        } else if (id == R.id.nav_mediamirror) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, MediaServerActivity.class);
        } else if (id == R.id.nav_coins) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, CoinActivity.class);
        } else if (id == R.id.nav_menu) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, MenuActivity.class);
        } else if (id == R.id.nav_shopping) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, ShoppingListActivity.class);
        } else if (id == R.id.nav_forecast_weather) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, ForecastWeatherActivity.class);
        } else if (id == R.id.nav_security) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, SecurityActivity.class);
        } else if (id == R.id.nav_settings) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, SettingsActivity.class);
        } else if (id == R.id.nav_switch) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, WirelessSwitchActivity.class);
        } else if (id == R.id.nav_meter) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, MeterDataActivity.class);
        } else if (id == R.id.nav_money) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, MoneyMeterDataActivity.class);
        } else if (id == R.id.nav_birthday) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, MoneyMeterDataActivity.class);
        } else if (id == R.id.nav_puckjs) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, PuckJsActivity.class);
        } else if (id == R.id.nav_temperature) {
            navigationResult = NavigationService.getInstance().NavigateToActivity(_context, TemperatureActivity.class);
        }

        if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
            Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
            displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
        }

        _drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(AppCompatBaseActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }
}
