package guepardoapps.lucahome.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;

import guepardoapps.library.lucahome.common.constants.Color;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.view.controller.home.BottomBarController;
import guepardoapps.lucahome.view.controller.home.ListButtonController;
import guepardoapps.lucahome.view.controller.home.MapController;
import guepardoapps.lucahome.view.controller.home.WeatherController;

public class HomeView extends Activity {

	private static final String TAG = HomeView.class.getSimpleName();
	private LucaHomeLogger _logger;

	private Context _context;

	private BottomBarController _bottomBarController;
	private ListButtonController _listButtonController;
	private MapController _mapController;
	private WeatherController _weatherController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_home);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));

		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_context = this;

		_bottomBarController = new BottomBarController(_context);
		_listButtonController = new ListButtonController(_context);
		_mapController = new MapController(_context);
		_weatherController = new WeatherController(_context);

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
		super.onPause();
		_logger.Debug("onPause");

		_bottomBarController.onPause();
		_listButtonController.onPause();
		_mapController.onPause();
		_weatherController.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_logger.Debug("onDestroy");

		_bottomBarController.onDestroy();
		_listButtonController.onDestroy();
		_mapController.onDestroy();
		_weatherController.onDestroy();
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
