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
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import guepardoapps.library.lucahome.common.dto.ListedMenuDto;
import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.enums.MainServiceAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.customadapter.MenuListAdapter;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;

public class MenuView extends AppCompatActivity {

    private static final String TAG = MenuView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private boolean _isInitialized;

    private SerializableList<MenuDto> _menu;
    private SerializableList<ListedMenuDto> _listedMenu;

    private ProgressBar _progressBar;
    private ListView _listView;

    private ListAdapter _listAdapter;

    private Context _context;

    private BroadcastController _broadcastController;
    private NavigationService _navigationService;
    private ReceiverController _receiverController;

    private Runnable _getDataRunnable = new Runnable() {
        public void run() {
            _broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
                    new String[]{Bundles.MAIN_SERVICE_ACTION}, new Object[]{MainServiceAction.GET_MENU});
        }
    };

    private BroadcastReceiver _updateListedMenuReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateListedMenuReceiver onReceive");

            @SuppressWarnings("unchecked")
            SerializableList<ListedMenuDto> list = (SerializableList<ListedMenuDto>) intent
                    .getSerializableExtra(Bundles.LISTED_MENU);

            if (list != null) {
                _listedMenu = list;

                if (_menu != null) {
                    _listAdapter = new MenuListAdapter(_context, _menu, _listedMenu, false, false);
                    _listView.setAdapter(_listAdapter);

                    _progressBar.setVisibility(View.GONE);
                    _listView.setVisibility(View.VISIBLE);
                } else {
                    _logger.Warn("_menu is currently null!");
                }
            }
        }
    };

    private BroadcastReceiver _updateMenuReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateMenuReceiver onReceive");

            @SuppressWarnings("unchecked")
            SerializableList<MenuDto> list = (SerializableList<MenuDto>) intent.getSerializableExtra(Bundles.MENU);

            if (list != null) {
                _menu = list;

                if (_listedMenu != null) {
                    _listAdapter = new MenuListAdapter(_context, _menu, _listedMenu, false, false);
                    _listView.setAdapter(_listAdapter);

                    _progressBar.setVisibility(View.GONE);
                    _listView.setVisibility(View.VISIBLE);
                } else {
                    _logger.Warn("_listedMenu is currently null!");
                }
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

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.skeletonList_collapsing);
        collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(_context, R.color.TextIcon));
        collapsingToolbar.setCollapsedTitleTextColor(android.graphics.Color.argb(0, 0, 0, 0));
        collapsingToolbar.setTitle("Menu");

        _listView = (ListView) findViewById(R.id.skeletonList_listView);
        _progressBar = (ProgressBar) findViewById(R.id.skeletonList_progressBarListView);

        ImageView mainBackground = (ImageView) findViewById(R.id.skeletonList_backdrop);
        mainBackground.setImageResource(R.drawable.main_image_menu);

        FloatingActionButton buttonAdd = (FloatingActionButton) findViewById(R.id.skeletonList_addButton);
        buttonAdd.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        _logger.Debug("onResume");
        if (!_isInitialized) {
            if (_receiverController != null && _broadcastController != null) {
                _isInitialized = true;
                _receiverController.RegisterReceiver(_updateListedMenuReceiver,
                        new String[]{Broadcasts.UPDATE_LISTED_MENU_VIEW});
                _receiverController.RegisterReceiver(_updateMenuReceiver, new String[]{Broadcasts.UPDATE_MENU_VIEW});
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
            _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_MENU);
            _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_LISTED_MENU);
        }
        return super.onOptionsItemSelected(item);
    }
}
