package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import java.util.List;

import de.mateware.snacky.Snacky;

import guepardoapps.library.openweather.customadapter.ForecastListAdapter;
import guepardoapps.library.openweather.models.ForecastModel;
import guepardoapps.library.openweather.models.ForecastPartModel;
import guepardoapps.library.openweather.service.OpenWeatherService;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.ReceiverController;

public class ForecastWeatherActivity extends AppCompatBaseActivity {
    /**
     * BroadcastReceiver to receive the event after download of weather has finished
     */
    private BroadcastReceiver _forecastDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            OpenWeatherService.ForecastWeatherDownloadFinishedContent result =
                    (OpenWeatherService.ForecastWeatherDownloadFinishedContent) intent.getSerializableExtra(OpenWeatherService.ForecastWeatherDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(OpenWeatherService.getInstance().GetLastUpdate().toString());
                updateList();
            } else {
                displayErrorSnackBar(result.Response);
                _noDataFallback.setVisibility(View.VISIBLE);
                _searchField.setVisibility(View.INVISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = ForecastWeatherActivity.class.getSimpleName();

        setContentView(R.layout.activity_forecast_weather);

        Toolbar toolbar = findViewById(R.id.toolbar_forecast_weather);
        //setSupportActionBar(toolbar);

        _listView = findViewById(R.id.listView_forecast_weather);
        _progressBar = findViewById(R.id.progressBar_forecast_weather);
        _noDataFallback = findViewById(R.id.fallBackTextView_forecast_weather);
        _lastUpdateTextView = findViewById(R.id.lastUpdateTextView_forecast_weather);
        _mainImageView = findViewById(R.id.kenBurnsView_forecast_weather);

        _searchField = findViewById(R.id.search_forecast_weather);
        _searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                ForecastModel foundForecastModel = OpenWeatherService.getInstance().FoundForecastItem(charSequence.toString());
                _listView.setAdapter(new ForecastListAdapter(_context, foundForecastModel.GetList()));
                _mainImageView.setImageResource(foundForecastModel.GetWallpaper());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _context = this;

        _receiverController = new ReceiverController(_context);

        _lastUpdateTextView.setText(OpenWeatherService.getInstance().GetLastUpdate().toString());

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar_forecast_weather);
        collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _drawerLayout = findViewById(R.id.drawer_layout_forecast_weather);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_forecast_weather);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_forecast_weather);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.INVISIBLE);
            OpenWeatherService.getInstance().LoadForecastWeather();
        });

        updateList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_forecastDownloadReceiver, new String[]{OpenWeatherService.ForecastWeatherDownloadFinishedBroadcast});
    }

    @Override
    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(ForecastWeatherActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void updateList() {
        List<ForecastPartModel> forecastList = OpenWeatherService.getInstance().ForecastWeather().GetList();
        if (forecastList.size() > 0) {
            _listView.setAdapter(new ForecastListAdapter(_context, forecastList));

            _mainImageView.setImageResource(OpenWeatherService.getInstance().ForecastWeather().GetWallpaper());

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);
        }
        _progressBar.setVisibility(View.GONE);
    }
}
