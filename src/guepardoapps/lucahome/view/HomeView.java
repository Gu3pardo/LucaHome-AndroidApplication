package guepardoapps.lucahome.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.view.controller.HomeViewBottomBarController;
import guepardoapps.lucahome.view.controller.HomeViewListButtonController;
import guepardoapps.lucahome.view.controller.HomeViewMapController;
import guepardoapps.lucahome.view.controller.HomeViewWeatherController;

import guepardoapps.lucahomelibrary.common.constants.Color;
import guepardoapps.lucahomelibrary.common.tools.LucaHomeLogger;
import guepardoapps.lucahomelibrary.services.helper.NavigationService;

public class HomeView extends Activity {

	private static final String TAG = HomeView.class.getName();
	private LucaHomeLogger _logger;

	private Context _context;

	private NavigationService _navigationService;

	private HomeViewBottomBarController _bottomBarController;
	private HomeViewListButtonController _listButtonController;
	private HomeViewMapController _mapController;
	private HomeViewWeatherController _weatherController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_home);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));

		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_context = this;

		_navigationService = new NavigationService(_context);

		_bottomBarController = new HomeViewBottomBarController(_context);
		_listButtonController = new HomeViewListButtonController(_context);
		_mapController = new HomeViewMapController(_context);
		_weatherController = new HomeViewWeatherController(_context);

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
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void ShowMap(View view) {
		_logger.Debug("ShowMap");
		_logger.Debug("Selected by view: " + view.getTransitionName());
		_navigationService.NavigateTo(MapView.class, true);
	}
}
