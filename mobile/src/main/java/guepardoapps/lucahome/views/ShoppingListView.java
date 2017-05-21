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
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
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
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.enums.MainServiceAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.customadapter.ShoppingListAdapter;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;

public class ShoppingListView extends AppCompatActivity {

    private static final String TAG = ShoppingListView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private boolean _isInitialized;
    private SerializableList<ShoppingEntryDto> _shoppingList;

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

    private Class<?>[] _activities = {MenuView.class, null};
    private int[] _images = {R.drawable.main_image_menu, R.drawable.main_image_shopping};
    private static final int _startImageIndex = 1;
    private ImageListener _imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageResource(_images[position]);
        }
    };

    private Runnable _getDataRunnable = new Runnable() {
        public void run() {
            _broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
                    new String[]{Bundles.MAIN_SERVICE_ACTION}, new Object[]{MainServiceAction.GET_SHOPPING_LIST});
        }
    };

    private BroadcastReceiver _updateListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateListReceiver onReceive");

            @SuppressWarnings("unchecked")
            SerializableList<ShoppingEntryDto> list = (SerializableList<ShoppingEntryDto>) intent
                    .getSerializableExtra(guepardoapps.library.lucahome.common.constants.Bundles.SHOPPING_LIST);

            if (list != null) {
                _shoppingList = list;

                ListAdapter listAdapter = new ShoppingListAdapter(_context, _shoppingList, false, null);
                _listView.setAdapter(listAdapter);

                if (list.getSize() > 0) {
                    _noDataFallback.setVisibility(View.GONE);
                } else {
                    _noDataFallback.setVisibility(View.VISIBLE);
                }

                _progressBar.setVisibility(View.GONE);

                _collapsingToolbar.setTitle(String.format(Locale.GERMAN, " %d entries", list.getSize()));
            } else {
                _logger.Warn("shoppingList is null!");

                _progressBar.setVisibility(View.GONE);
                _noDataFallback.setVisibility(View.VISIBLE);

                _collapsingToolbar.setTitle(String.format(Locale.GERMAN, " %d entries", 0));
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
                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_SHOPPING_LIST);
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
                _logger.Info(String.format(Locale.GERMAN,
                        "onPageScrolled at position %d with positionOffset %f and positionOffsetPixels %d",
                        position, positionOffset, positionOffsetPixels));
            }

            @Override
            public void onPageSelected(int position) {
                _logger.Info(String.format(Locale.GERMAN, "onPageSelected at position %d", position));
                Class<?> targetActivity = _activities[position];
                if (targetActivity != null) {
                    _navigationService.NavigateTo(targetActivity, true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                _logger.Info(String.format(Locale.GERMAN, "onPageScrollStateChanged at state %d", state));
            }
        });

        FloatingActionButton buttonAdd = (FloatingActionButton) findViewById(R.id.skeletonList_addButton);
        buttonAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                _logger.Debug("onClick buttonAdd");
                int size = 0;
                if (_shoppingList != null) {
                    size = _shoppingList.getSize();
                }
                _dialogController.ShowAddShoppingEntryDialog((Activity) _context, _getDataRunnable, null, true, false, size);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        _logger.Debug("onResume");
        if (!_isInitialized) {
            if (_receiverController != null && _broadcastController != null) {
                _isInitialized = true;
                _receiverController.RegisterReceiver(_updateListReceiver,
                        new String[]{Broadcasts.UPDATE_SHOPPING_LIST});
                _getDataRunnable.run();
            }
        }
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
        _isInitialized = false;
        _receiverController.Dispose();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        _isInitialized = false;
        _receiverController.Dispose();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shopping_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.buttonShareShoppingList) {
            if (_shoppingList != null) {
                if (_shoppingList.getSize() > 0) {
                    shareShoppingList();
                } else {
                    Toasty.warning(_context, "Nothing to share!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toasty.warning(_context, "Nothing to share!", Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.buttonReload) {
            _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_SHOPPING_LIST);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            _navigationService.NavigateTo(HomeView.class, true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void shareShoppingList() {
        String shareText = "ShoppingList:\n";

        for (int index = 0; index < _shoppingList.getSize(); index++) {
            ShoppingEntryDto entry = _shoppingList.getValue(index);
            shareText += String.valueOf(entry.GetQuantity()) + "x " + entry.GetName() + "\n";
        }

        Intent sendIntent = new Intent();

        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");

        startActivity(sendIntent);
    }
}
