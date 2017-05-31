package guepardoapps.lucahome.views.controller.socket;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.Locale;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.enums.HomeAutomationAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.customadapter.SocketListAdapter;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.views.HomeView;
import guepardoapps.lucahome.views.ScheduleView;
import guepardoapps.lucahome.views.TimerView;

public class SocketViewController {

    private static final String TAG = SocketViewController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private boolean _isInitialized;

    private CollapsingToolbarLayout _collapsingToolbar;
    private PullRefreshLayout _pullRefreshLayout;

    private ProgressBar _progressBar;
    private ListView _listView;
    private TextView _noDataFallback;

    private Context _context;
    private SerializableList<WirelessSocketDto> _socketList;

    private BroadcastController _broadcastController;
    private LucaDialogController _dialogController;
    private NavigationService _navigationService;
    private ReceiverController _receiverController;

    private static final int _startImageIndex = 0;
    private int[] _images = {R.drawable.main_image_sockets, R.drawable.main_image_schedule, R.drawable.main_image_timer};
    private Class<?>[] _activities = {null, ScheduleView.class, TimerView.class};
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
                    new Object[]{HomeAutomationAction.GET_SOCKET_LIST});
        }
    };

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver onReceive");

            @SuppressWarnings("unchecked")
            SerializableList<WirelessSocketDto> list = (SerializableList<WirelessSocketDto>) intent
                    .getSerializableExtra(Bundles.SOCKET_LIST);

            if (list != null) {
                _socketList = list;
                _listView.setAdapter(new SocketListAdapter(_context, _socketList, false, null));

                _progressBar.setVisibility(View.GONE);

                if (list.getSize() > 0) {
                    _noDataFallback.setVisibility(View.GONE);
                } else {
                    _noDataFallback.setVisibility(View.VISIBLE);
                }

                _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d sockets", list.getSize()));
            }

            _pullRefreshLayout.setRefreshing(false);
        }
    };

    public SocketViewController(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;

        _socketList = new SerializableList<>();

        _broadcastController = new BroadcastController(_context);
        _dialogController = new LucaDialogController(_context);
        _navigationService = new NavigationService(_context);
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");

        _collapsingToolbar = (CollapsingToolbarLayout) ((Activity) _context).findViewById(R.id.skeletonList_collapsing);
        _collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(_context, R.color.TextIcon));
        _collapsingToolbar.setCollapsedTitleTextColor(Color.argb(0, 0, 0, 0));

        _pullRefreshLayout = (PullRefreshLayout) ((Activity) _context).findViewById(R.id.skeletonList_pullRefreshLayout);
        _pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                _logger.Debug("onRefresh ");
                ReloadSockets();
            }
        });

        _listView = (ListView) ((Activity) _context).findViewById(R.id.skeletonList_listView);
        _progressBar = (ProgressBar) ((Activity) _context).findViewById(R.id.skeletonList_progressBarListView);
        _noDataFallback = (TextView) ((Activity) _context).findViewById(R.id.skeletonList_fallBackTextView);

        CarouselView carouselView = (CarouselView) ((Activity) _context).findViewById(R.id.skeletonList_carouselView);
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

        FloatingActionButton buttonAdd = (FloatingActionButton) ((Activity) _context).findViewById(R.id.skeletonList_addButton);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("onClick buttonAdd");
                _dialogController.ShowAddSocketDialog(_socketList.getSize(), (Activity) _context, _getDataRunnable, null, true);
            }
        });
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            if (_receiverController != null && _broadcastController != null) {
                if (_dialogController == null) {
                    _dialogController = new LucaDialogController(_context);
                }
                _receiverController.RegisterReceiver(_updateReceiver, new String[]{Broadcasts.UPDATE_SOCKET_LIST});
                ReloadSockets();
                _isInitialized = true;
            }
        }
    }

    public void onPause() {
        _logger.Debug("onPause");
        dispose();
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        dispose();
    }

    public boolean NavigateToHome() {
        _logger.Debug("NavigateToHome");
        if (!_isInitialized) {
            _logger.Warn("Not initialized!");
            return false;
        }
        _navigationService.NavigateTo(HomeView.class, true);
        return true;
    }

    public boolean ReloadSockets() {
        _logger.Debug("ReloadSockets");
        if (!_isInitialized) {
            _logger.Warn("Not initialized!");
            return false;
        }
        _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_SOCKETS);
        return true;
    }

    private void dispose() {
        _logger.Debug("dispose");

        if (!_isInitialized) {
            _logger.Error("Already disposed!");
            return;
        }

        _dialogController.Dispose();
        _dialogController = null;

        _receiverController.Dispose();
        _receiverController = null;

        _isInitialized = false;
    }
}
