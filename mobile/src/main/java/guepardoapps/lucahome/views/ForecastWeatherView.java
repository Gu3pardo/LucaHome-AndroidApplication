package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.flaviofaria.kenburnsview.KenBurnsView;

import java.util.List;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.enums.HomeAutomationAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.openweather.common.classes.NotificationContent;
import guepardoapps.library.openweather.common.enums.WeatherCondition;
import guepardoapps.library.openweather.common.model.ForecastModel;

import guepardoapps.library.openweather.common.model.ForecastWeatherModel;
import guepardoapps.library.openweather.customadapter.ForecastListAdapter;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.library.toolset.controller.SharedPrefController;
import guepardoapps.lucahome.R;

public class ForecastWeatherView extends AppCompatActivity {

    private static final String TAG = ForecastWeatherView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private boolean _isInitialized;

    private PullRefreshLayout _pullRefreshLayout;

    private ProgressBar _progressBar;
    private ListView _listView;
    private KenBurnsView _mainBackground;
    private TextView _noDataFallback;

    private Context _context;

    private BroadcastController _broadcastController;
    private NavigationService _navigationService;
    private ReceiverController _receiverController;

    private Runnable _getDataRunnable = new Runnable() {
        public void run() {
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.HOME_AUTOMATION_COMMAND,
                    new String[]{Bundles.HOME_AUTOMATION_ACTION},
                    new Object[]{HomeAutomationAction.GET_WEATHER_FORECAST});
        }
    };

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver onReceive");

            ForecastModel data = (ForecastModel) intent.getSerializableExtra(Bundles.WEATHER_FORECAST);

            if (data != null) {
                _logger.Info(String.format("ForecastModel: %s", data.toString()));

                List<ForecastWeatherModel> list = data.GetList();
                _listView.setAdapter(new ForecastListAdapter(_context, list));

                try {
                    NotificationContent notificationContent = data.TellForecastWeather();
                    int wallpaperId = WeatherCondition.GetByIcon(notificationContent.GetIcon()).GetWallpaper();
                    _mainBackground.setImageResource(wallpaperId);
                } catch (Exception exception) {
                    _logger.Error(exception.getMessage());
                    _mainBackground.setImageResource(R.drawable.wallpaper);
                }

                _noDataFallback.setVisibility(View.GONE);
                _progressBar.setVisibility(View.GONE);
            } else {
                _logger.Warn("ForecastModel is null!");

                _progressBar.setVisibility(View.GONE);
                _noDataFallback.setVisibility(View.VISIBLE);

                _mainBackground.setImageResource(R.drawable.wallpaper);
            }

            _pullRefreshLayout.setRefreshing(false);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_skeleton_nested_list);

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        _context = this;

        _broadcastController = new BroadcastController(_context);
        _navigationService = new NavigationService(_context);
        _receiverController = new ReceiverController(_context);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.skeletonList_collapsing);
        collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(_context, R.color.TextIcon));
        collapsingToolbar.setCollapsedTitleTextColor(android.graphics.Color.argb(0, 0, 0, 0));

        _pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.skeletonList_pullRefreshLayout);
        _pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                _logger.Debug("onRefresh " + TAG);
                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_CURRENT_WEATHER);
                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_FORECAST_WEATHER);
            }
        });

        _listView = (ListView) findViewById(R.id.skeletonList_listView);
        _progressBar = (ProgressBar) findViewById(R.id.skeletonList_progressBarListView);
        _noDataFallback = (TextView) findViewById(R.id.skeletonList_fallBackTextView);

        _mainBackground = (KenBurnsView) findViewById(R.id.skeletonList_backdrop);
        _mainBackground.setImageResource(R.drawable.wallpaper);
        if (!new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME).LoadBooleanValueFromSharedPreferences(SharedPrefConstants.MOVE_IMAGES)) {
            _mainBackground.pause();
        }

        FloatingActionButton buttonAdd = (FloatingActionButton) findViewById(R.id.skeletonList_addButton);
        buttonAdd.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        _logger.Debug("onResume");
        if (!_isInitialized) {
            if (_receiverController != null && _broadcastController != null) {
                _receiverController.RegisterReceiver(_updateReceiver, new String[]{Broadcasts.UPDATE_FORECAST_VIEW});
                _isInitialized = true;
                _getDataRunnable.run();
            }
        }
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
        _receiverController.Dispose();
        _isInitialized = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            _navigationService.NavigateTo(HomeView.class, true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_basic_reload, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.buttonReload) {
            _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_FORECAST_WEATHER);
        }
        return super.onOptionsItemSelected(item);
    }
}
