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
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;

import guepardoapps.library.openweather.service.OpenWeatherService;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.adapter.MainListViewAdapter;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.builders.MainListViewBuilder;
import guepardoapps.lucahome.builders.MapContentViewBuilder;
import guepardoapps.lucahome.classes.MainListViewItem;
import guepardoapps.lucahome.common.service.MapContentService;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.lucahome.service.MainService;
import guepardoapps.lucahome.service.NavigationService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Initiate UI
     */
    private ProgressBar _progressBar;
    private ListView _listView;
    private PullRefreshLayout _pullRefreshLayout;

    /**
     * Builder for the entries of the listView and the map
     */
    private MainListViewBuilder _mainListViewBuilder;
    private MapContentViewBuilder _mapContentViewBuilder;

    /**
     * ReceiverController to register and unregister from broadcasts of the UserService
     */
    private ReceiverController _receiverController;

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
        }

        public void onServiceDisconnected(ComponentName className) {
            _mainServiceBinder = null;
        }
    };

    /**
     * BroadcastReceiver to receive progress and success state of initial app download
     */
    private BroadcastReceiver _mainServiceDownloadProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainService.MainServiceDownloadCountContent progress = (MainService.MainServiceDownloadCountContent) intent.getSerializableExtra(MainService.MainServiceDownloadCountBundle);
            if (progress != null) {
                if (progress.DownloadFinished) {
                    updateWeatherCard();

                    _listView.setVisibility(View.VISIBLE);
                    _progressBar.setVisibility(View.GONE);
                    _pullRefreshLayout.setRefreshing(false);

                    _listView.setAdapter(new MainListViewAdapter(MainActivity.this, _mainListViewBuilder.GetList()));

                    _mapContentViewBuilder.CreateMapContentViewList(MapContentService.getInstance().GetDataList());
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
            _mapContentViewBuilder.CreateMapContentViewList(MapContentService.getInstance().GetDataList());
            _mapContentViewBuilder.AddViewsToMap();
        }
    };

    /**
     * BroadcastReceiver to receive updates for the current or forecast weather
     */
    private BroadcastReceiver _openWeatherUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateWeatherCard();
            updateMapContent();
        }
    };

    /**
     * BroadcastReceiver to receive updates for the wirelessSockets
     */
    private BroadcastReceiver _wirelessSocketUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MapContentService.getInstance().LoadData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        _receiverController = new ReceiverController(this);

        _listView = findViewById(R.id.skeletonList_listView_main);
        _progressBar = findViewById(R.id.skeletonList_progressBarListView_main);
        TextView noDataFallback = findViewById(R.id.skeletonList_fallBackTextView_main);

        _mainListViewBuilder = new MainListViewBuilder(this);
        _mapContentViewBuilder = new MapContentViewBuilder(this);

        _listView.setAdapter(new MainListViewAdapter(this, _mainListViewBuilder.GetList()));

        _listView.setVisibility(View.VISIBLE);
        _progressBar.setVisibility(View.GONE);
        noDataFallback.setVisibility(View.GONE);

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.skeletonList_collapsing_main);
        collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        FloatingActionButton imageButtonSettings = findViewById(R.id.floating_action_button_settings);
        imageButtonSettings.setOnClickListener(view -> NavigationService.getInstance().NavigateToActivity(MainActivity.this, SettingsActivity.class));

        FloatingActionButton imageButtonAbout = findViewById(R.id.floating_action_button_about);
        imageButtonAbout.setOnClickListener(view -> new LibsBuilder()
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .start(MainActivity.this));

        _listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Check if bottom has been reached
                if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount > 0) {
                    imageButtonSettings.setVisibility(View.GONE);
                    imageButtonAbout.setVisibility(View.GONE);
                } else {
                    imageButtonSettings.setVisibility(View.VISIBLE);
                    imageButtonAbout.setVisibility(View.VISIBLE);
                }
            }
        });

        _pullRefreshLayout = findViewById(R.id.skeletonList_pullRefreshLayout_main);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _mainServiceBinder.StartDownloadAll("pullRefreshLayout setOnRefreshListener");
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        NavigationService.getInstance().ClearGoBackList();

        _receiverController.RegisterReceiver(_mainServiceDownloadProgressReceiver, new String[]{MainService.MainServiceDownloadCountBroadcast});
        _receiverController.RegisterReceiver(_mapContentUpdateReceiver, new String[]{MapContentService.MapContentDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_openWeatherUpdateReceiver, new String[]{OpenWeatherService.CurrentWeatherDownloadFinishedBroadcast, OpenWeatherService.ForecastWeatherDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSocketUpdateReceiver, new String[]{WirelessSocketService.WirelessSocketDownloadFinishedBroadcast});

        if (_mainServiceBinder == null) {
            bindService(new Intent(this, MainService.class), _mainServiceConnection, Context.BIND_AUTO_CREATE);
        }

        _mapContentViewBuilder.Initialize();
        _mapContentViewBuilder.CreateMapContentViewList(MapContentService.getInstance().GetDataList());
        _mapContentViewBuilder.AddViewsToMap();
    }

    @Override
    protected void onPause() {
        super.onPause();

        _receiverController.Dispose();

        if (_mainServiceBinder != null) {
            try {
                unbindService(_mainServiceConnection);
            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.getMessage());
            } finally {
                _mainServiceBinder = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _receiverController.Dispose();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            NavigationService.getInstance().ClearCurrentActivity();
            NavigationService.getInstance().ClearGoBackList();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateMapContent() {
        _mapContentViewBuilder.CreateMapContentViewList(MapContentService.getInstance().GetDataList());
        _mapContentViewBuilder.AddViewsToMap();
    }

    private void updateWeatherCard() {
        _mainListViewBuilder.UpdateItemDescription(MainListViewItem.Type.Weather, String.format(
                Locale.getDefault(),
                "Current temperature: %.2f degree Celsius\nCurrent condition: %s",
                OpenWeatherService.getInstance().CurrentWeather().GetTemperature(), OpenWeatherService.getInstance().CurrentWeather().GetDescription()));
        _mainListViewBuilder.UpdateItemImageResource(MainListViewItem.Type.Weather, OpenWeatherService.getInstance().CurrentWeather().GetCondition().GetWallpaper());
    }
}
