package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;

import java.util.Locale;

import guepardoapps.library.openweather.service.OpenWeatherService;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.adapter.MainListViewAdapter;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.builders.MainListViewBuilder;
import guepardoapps.lucahome.builders.MapContentViewBuilder;
import guepardoapps.lucahome.classes.MainListViewItem;
import guepardoapps.lucahome.data.service.MapContentService;
import guepardoapps.lucahome.data.service.TemperatureService;
import guepardoapps.lucahome.service.MainService;
import guepardoapps.lucahome.service.NavigationService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Logger _logger;

    /**
     * Initiate UI
     */
    private ProgressBar _progressBar;
    private ListView _listView;

    /**
     * Builder for the entries of the listView and the map
     */
    private MainListViewBuilder _mainListViewBuilder;
    private MapContentViewBuilder _mapContentViewBuilder;

    /**
     * Adapter for the cards of the listView
     */
    private MainListViewAdapter _mainListViewAdapter;

    /**
     * MapContentService manages data for mapContent
     */
    private MapContentService _mapContentService;

    /**
     * ReceiverController to register and unregister from broadcasts of the UserService
     */
    private ReceiverController _receiverController;

    /**
     * NavigationService manages navigation between activities
     */
    private NavigationService _navigationService;

    /**
     * Services to handle data
     */
    private OpenWeatherService _openWeatherService;
    private TemperatureService _temperatureService;

    /**
     * Binder for MainService
     */
    private MainService _mainServiceBinder;

    /**
     * ServiceConnection for MainServiceBinder
     */
    private ServiceConnection _mainServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            _mainServiceBinder = ((MainService.MainServiceBinder) binder).getService();
            _logger.Debug("onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName className) {
            _logger.Debug("onServiceDisconnected");
            _mainServiceBinder = null;
        }
    };

    /**
     * BroadcastReceiver to receive progress and success state of initial app download
     */
    private BroadcastReceiver _mainServiceDownloadProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_mainServiceDownloadProgress");
            MainService.MainServiceDownloadCountContent progress = (MainService.MainServiceDownloadCountContent) intent.getSerializableExtra(MainService.MainServiceDownloadCountBundle);
            if (progress != null) {
                if (progress.DownloadFinished) {
                    _mainListViewBuilder.UpdateItemDescription(MainListViewItem.Type.Weather, String.format(
                            Locale.getDefault(),
                            "Current temperature: %.2f degree Celsius\nCurrent condition: %s",
                            _openWeatherService.CurrentWeather().GetTemperature(), _openWeatherService.CurrentWeather().GetDescription()));
                    _mainListViewBuilder.UpdateItemImageResource(MainListViewItem.Type.Weather, _openWeatherService.ForecastWeather().GetWallpaper());

                    _listView.setVisibility(View.VISIBLE);
                    _progressBar.setVisibility(View.GONE);

                    _mapContentViewBuilder.CreateMapContentViewList(_mapContentService.GetMapContentList());
                    _mapContentViewBuilder.AddViewsToMap();
                }
            }
        }
    };

    /**
     * BroadcastReceiver to receive updates for the mapContent
     */
    private BroadcastReceiver _mapContentUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_mapContentUpdateReceiver");
            _mapContentViewBuilder.CreateMapContentViewList(_mapContentService.GetMapContentList());
            _mapContentViewBuilder.AddViewsToMap();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _logger = new Logger(TAG);
        _logger.Debug("onCreate");

        setContentView(R.layout.activity_main);

        _mapContentService = MapContentService.getInstance();
        _navigationService = NavigationService.getInstance();
        _openWeatherService = OpenWeatherService.getInstance();
        _temperatureService = TemperatureService.getInstance();

        _receiverController = new ReceiverController(this);

        _listView = (ListView) findViewById(R.id.skeletonList_listView_main);
        _progressBar = (ProgressBar) findViewById(R.id.skeletonList_progressBarListView_main);
        TextView noDataFallback = (TextView) findViewById(R.id.skeletonList_fallBackTextView_main);

        _mainListViewBuilder = new MainListViewBuilder(this);
        _mapContentViewBuilder = new MapContentViewBuilder(this);

        _mainListViewAdapter = new MainListViewAdapter(this, _mainListViewBuilder.GetList());
        _listView.setAdapter(_mainListViewAdapter);

        _listView.setVisibility(View.VISIBLE);
        _progressBar.setVisibility(View.GONE);
        noDataFallback.setVisibility(View.GONE);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.skeletonList_collapsing_main);
        collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        PullRefreshLayout pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.skeletonList_pullRefreshLayout_main);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                _logger.Debug("onRefresh " + TAG);

                _listView.setVisibility(View.GONE);
                _progressBar.setVisibility(View.VISIBLE);

                _mainServiceBinder.StartDownloadAll("pullRefreshLayout setOnRefreshListener");
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

        _navigationService.ClearGoBackList();
        _receiverController.RegisterReceiver(_mainServiceDownloadProgressReceiver, new String[]{MainService.MainServiceDownloadCountBroadcast});
        _receiverController.RegisterReceiver(_mapContentUpdateReceiver, new String[]{MapContentService.MapContentDownloadFinishedBroadcast});

        if (_mainServiceBinder == null) {
            _logger.Debug("Not bound to service! Binding now...");
            bindService(new Intent(this, MainService.class), _mainServiceConnection, Context.BIND_AUTO_CREATE);
        }

        _mapContentViewBuilder.Initialize();
        _mapContentViewBuilder.CreateMapContentViewList(_mapContentService.GetMapContentList());
        _mapContentViewBuilder.AddViewsToMap();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _logger.Debug("onPause");

        _receiverController.Dispose();

        if (_mainServiceBinder != null) {
            _logger.Debug("Unbinding from server");
            try {
                unbindService(_mainServiceConnection);
            } catch (Exception exception) {
                _logger.Error(exception.getMessage());
            } finally {
                _mainServiceBinder = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        _logger.Debug(String.format("onKeyDown: keyCode: %s | event: %s", keyCode, event));

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            _navigationService.ClearCurrentActivity();
            _navigationService.ClearGoBackList();
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
