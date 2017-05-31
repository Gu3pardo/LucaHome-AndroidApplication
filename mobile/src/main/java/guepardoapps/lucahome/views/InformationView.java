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
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.InformationDto;
import guepardoapps.library.lucahome.common.enums.HomeAutomationAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.customadapter.InformationListAdapter;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;

public class InformationView extends AppCompatActivity {

    private static final String TAG = InformationView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private boolean _isInitialized;

    private CollapsingToolbarLayout _collapsingToolbar;

    private ProgressBar _progressBar;
    private ListView _listView;
    private TextView _noDataFallback;

    private Context _context;

    private BroadcastController _broadcastController;
    private NavigationService _navigationService;
    private ReceiverController _receiverController;

    private Runnable _getDataRunnable = new Runnable() {
        public void run() {
            _broadcastController.SendSerializableBroadcast(
                    Broadcasts.HOME_AUTOMATION_COMMAND,
                    Bundles.HOME_AUTOMATION_ACTION,
                    HomeAutomationAction.GET_INFORMATION_LIST);
            _logger.Debug("Called for information!");
        }
    };

    private BroadcastReceiver _updateInformationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateInformationReceiver onReceive");

            InformationDto informationDto = (InformationDto) intent.getSerializableExtra(Bundles.INFORMATION_SINGLE);
            if (informationDto != null) {
                _listView.setAdapter(new InformationListAdapter(_context, informationDto));

                _progressBar.setVisibility(View.GONE);
                _listView.setVisibility(View.VISIBLE);
                _noDataFallback.setVisibility(View.GONE);

                _collapsingToolbar.setTitle("Information");
            } else {
                _logger.Warn("InformationDto is null!");
                _progressBar.setVisibility(View.GONE);
                _listView.setVisibility(View.GONE);
                _noDataFallback.setVisibility(View.VISIBLE);
            }
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

        _collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.skeletonList_collapsing);
        _collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(_context, R.color.TextIcon));
        _collapsingToolbar.setCollapsedTitleTextColor(android.graphics.Color.argb(0, 0, 0, 0));

        PullRefreshLayout pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.skeletonList_pullRefreshLayout);
        pullRefreshLayout.setEnabled(false);

        _listView = (ListView) findViewById(R.id.skeletonList_listView);
        _progressBar = (ProgressBar) findViewById(R.id.skeletonList_progressBarListView);
        _noDataFallback = (TextView) findViewById(R.id.skeletonList_fallBackTextView);

        ImageView mainBackground = (ImageView) findViewById(R.id.skeletonList_backdrop);
        mainBackground.setImageResource(R.drawable.main_image_informations);

        FloatingActionButton buttonAdd = (FloatingActionButton) findViewById(R.id.skeletonList_addButton);
        buttonAdd.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        _logger.Debug("onResume");
        if (!_isInitialized) {
            if (_receiverController != null && _broadcastController != null) {
                _receiverController.RegisterReceiver(_updateInformationReceiver,
                        new String[]{Broadcasts.UPDATE_INFORMATION});
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
}
