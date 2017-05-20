package guepardoapps.lucahome.views;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import guepardoapps.library.lucahome.common.constants.Color;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.services.helper.NavigationService;
import guepardoapps.library.toolset.controller.BroadcastController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.views.controller.mediamirror.*;

public class MediaMirrorView extends Activity {

    private static final String TAG = MediaMirrorView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private BroadcastController _broadcastController;
    private NavigationService _navigationService;

    private AdvancedViewController _advancedViewController;
    private RssViewController _rssViewController;
    private ScreenViewController _screenViewController;
    private TextViewController _textViewController;
    private TopViewController _topViewController;
    private UpdateViewController _updateViewController;
    private WebsiteViewController _websiteViewController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_mediamirror);

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));
        } else {
            _logger.Error("ActionBar is null!");
        }

        _broadcastController = new BroadcastController(this);
        _navigationService = new NavigationService(this);

        _advancedViewController = new AdvancedViewController(this);
        _rssViewController = new RssViewController(this);
        _screenViewController = new ScreenViewController(this);
        _textViewController = new TextViewController(this);
        _topViewController = new TopViewController(this);
        _updateViewController = new UpdateViewController(this);
        _websiteViewController = new WebsiteViewController(this);

        _advancedViewController.onCreate();
        _rssViewController.onCreate();
        _screenViewController.onCreate();
        _textViewController.onCreate();
        _topViewController.onCreate();
        _updateViewController.onCreate();
        _websiteViewController.onCreate();
    }

    @Override
    public void onResume() {
        super.onResume();
        _logger.Debug("onResume");

        _advancedViewController.onResume();
        _rssViewController.onResume();
        _screenViewController.onResume();
        _textViewController.onResume();
        _topViewController.onResume();
        _updateViewController.onResume();
        _websiteViewController.onResume();
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");

        _advancedViewController.onPause();
        _rssViewController.onPause();
        _screenViewController.onPause();
        _textViewController.onPause();
        _topViewController.onPause();
        _updateViewController.onPause();
        _websiteViewController.onPause();

        super.onPause();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");

        _advancedViewController.onDestroy();
        _rssViewController.onDestroy();
        _screenViewController.onDestroy();
        _textViewController.onDestroy();
        _topViewController.onDestroy();
        _updateViewController.onDestroy();
        _websiteViewController.onDestroy();

        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        _logger.Debug(String.format("onKeyDown: keyCode: %s | event: %s", keyCode, event));

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
            _broadcastController
                    .SendSimpleBroadcast(guepardoapps.library.lucahome.common.constants.Broadcasts.RELOAD_MEDIAMIRROR);
        }
        return super.onOptionsItemSelected(item);
    }

    public void SelectedYoutubeId(View view) {
        _logger.Debug("SelectedYoutubeId");
        _topViewController.SelectedYoutubeId();
    }
}
