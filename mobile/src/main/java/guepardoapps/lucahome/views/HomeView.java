package guepardoapps.lucahome.views;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.views.controller.home.BottomBarController;
import guepardoapps.lucahome.views.controller.home.ListButtonController;
import guepardoapps.lucahome.views.controller.home.MapController;
import guepardoapps.lucahome.views.controller.home.WeatherController;

public class HomeView extends Activity {

    private static final String TAG = HomeView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private BottomBarController _bottomBarController;
    private ListButtonController _listButtonController;
    private MapController _mapController;
    private WeatherController _weatherController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_home);
        // TODO getActionBar().setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        _bottomBarController = new BottomBarController(this);
        _listButtonController = new ListButtonController(this);
        _mapController = new MapController(this);
        _weatherController = new WeatherController(this);

        _bottomBarController.onCreate();
        _listButtonController.onCreate();
        _mapController.onCreate();
        _weatherController.onCreate();
    }

    @Override
    public void onResume() {
        super.onResume();
        _logger.Debug("onResume");

        _bottomBarController.onResume();
        _listButtonController.onResume();
        _mapController.onResume();
        _weatherController.onResume();
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");

        _bottomBarController.onPause();
        _listButtonController.onPause();
        _mapController.onPause();
        _weatherController.onPause();

        super.onPause();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");

        _bottomBarController.onDestroy();
        _listButtonController.onDestroy();
        _mapController.onDestroy();
        _weatherController.onDestroy();

        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        _logger.Debug(String.format("onKeyDown: keyCode: %s | event: %s", keyCode, event));

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
