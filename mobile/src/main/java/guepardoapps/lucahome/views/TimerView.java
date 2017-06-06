package guepardoapps.lucahome.views;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.TimerDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.enums.HomeAutomationAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.customadapter.TimerListAdapter;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;

public class TimerView extends AppCompatActivity {

    private static final String TAG = TimerView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private boolean _isInitialized;
    private SerializableList<WirelessSocketDto> _socketList;

    private CollapsingToolbarLayout _collapsingToolbar;
    private PullRefreshLayout _pullRefreshLayout;

    private ProgressBar _progressBar;
    private ListView _listView;
    private TextView _noDataFallback;

    private Context _context;

    private BroadcastController _broadcastController;
    private LucaDialogController _dialogController;
    private NavigationService _navigationService;
    private ReceiverController _receiverController;

    private Class<?>[] _activities = {SocketView.class, ScheduleView.class, null};
    private int[] _images = {R.drawable.main_image_sockets, R.drawable.main_image_schedule, R.drawable.main_image_timer};
    private static final int _startImageIndex = 2;
    private ImageListener _imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageResource(_images[position]);
        }
    };

    private Runnable _getDataRunnable = new Runnable() {
        public void run() {
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.HOME_AUTOMATION_COMMAND,
                    new String[]{Bundles.HOME_AUTOMATION_ACTION},
                    new Object[]{HomeAutomationAction.GET_TIMER_LIST});
        }
    };

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver onReceive");

            SerializableList<TimerDto> list = (SerializableList<TimerDto>) intent
                    .getSerializableExtra(Bundles.TIMER_LIST);
            _socketList = (SerializableList<WirelessSocketDto>) intent.getSerializableExtra(Bundles.SOCKET_LIST);

            if (list != null) {
                _listView.setAdapter(new TimerListAdapter(_context, list, _socketList, false));

                _progressBar.setVisibility(View.GONE);

                if (list.getSize() > 0) {
                    _noDataFallback.setVisibility(View.GONE);
                } else {
                    _noDataFallback.setVisibility(View.VISIBLE);
                }

                _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d timer", list.getSize()));
            }

            _pullRefreshLayout.setRefreshing(false);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_skeleton_nested_list_carousel);

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        _context = this;

        _broadcastController = new BroadcastController(_context);
        _dialogController = new LucaDialogController(_context);
        _navigationService = new NavigationService(_context);
        _receiverController = new ReceiverController(_context);

        _collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.skeletonList_collapsing);
        _collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(_context, R.color.TextIcon));
        _collapsingToolbar.setCollapsedTitleTextColor(android.graphics.Color.argb(0, 0, 0, 0));

        _pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.skeletonList_pullRefreshLayout);
        _pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                _logger.Debug("onRefresh " + TAG);
                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_TIMER);
            }
        });

        _listView = (ListView) findViewById(R.id.skeletonList_listView);
        _progressBar = (ProgressBar) findViewById(R.id.skeletonList_progressBarListView);
        _noDataFallback = (TextView) findViewById(R.id.skeletonList_fallBackTextView);

        CarouselView carouselView = (CarouselView) findViewById(R.id.skeletonList_carouselView);
        carouselView.setPageCount(_images.length);
        carouselView.setCurrentItem(_startImageIndex);
        carouselView.setImageListener(_imageListener);
        carouselView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                _logger.Info(String.format(Locale.getDefault(),
                        "onPageScrolled at position %d with positionOffset %f and positionOffsetPixels %d",
                        position, positionOffset, positionOffsetPixels));
            }

            @Override
            public void onPageSelected(int position) {
                _logger.Info(String.format(Locale.getDefault(), "onPageSelected at position %d", position));
                Class<?> targetActivity = _activities[position];
                if (targetActivity != null) {
                    _navigationService.NavigateTo(targetActivity, true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                _logger.Info(String.format(Locale.getDefault(), "onPageScrollStateChanged at state %d", state));
            }
        });

        FloatingActionButton buttonAdd = (FloatingActionButton) findViewById(R.id.skeletonList_addButton);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("onClick _buttonAdd");
                if (_socketList != null) {
                    _dialogController.ShowAddTimerDialog((Activity) _context, _getDataRunnable, _socketList, null, true);
                } else {
                    _logger.Warn("SocketList is null!");
                    Toasty.error(_context, "SocketList is null!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        _logger.Debug("onResume");
        if (!_isInitialized) {
            if (_receiverController != null && _broadcastController != null) {
                if (_dialogController == null) {
                    _dialogController = new LucaDialogController(_context);
                }
                _receiverController.RegisterReceiver(_updateReceiver, new String[]{Broadcasts.UPDATE_TIMER});
                _isInitialized = true;
                _getDataRunnable.run();
            }
        }
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
        if (_dialogController != null) {
            _dialogController.Dispose();
            _dialogController = null;
        }
        _receiverController.Dispose();
        _isInitialized = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        if (_dialogController != null) {
            _dialogController.Dispose();
            _dialogController = null;
        }
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
            _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_TIMER);
        }
        return super.onOptionsItemSelected(item);
    }
}
