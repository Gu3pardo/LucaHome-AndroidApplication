package guepardoapps.lucahome.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import guepardoapps.library.lucahome.common.constants.Color;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.view.controller.mediamirror.*;

public class MediaMirrorView extends Activity {

	private static final String TAG = MediaMirrorView.class.getSimpleName();
	private LucaHomeLogger _logger;

	private Context _context;
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
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));

		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_context = this;
		_navigationService = new NavigationService(_context);

		_advancedViewController = new AdvancedViewController(_context);
		_rssViewController = new RssViewController(_context);
		_screenViewController = new ScreenViewController(_context);
		_textViewController = new TextViewController(_context);
		_topViewController = new TopViewController(_context);
		_updateViewController = new UpdateViewController(_context);
		_websiteViewController = new WebsiteViewController(_context);

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
		super.onPause();
		_logger.Debug("onPause");

		_advancedViewController.onPause();
		_rssViewController.onPause();
		_screenViewController.onPause();
		_textViewController.onPause();
		_topViewController.onPause();
		_updateViewController.onPause();
		_websiteViewController.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_logger.Debug("onDestroy");

		_advancedViewController.onDestroy();
		_rssViewController.onDestroy();
		_screenViewController.onDestroy();
		_textViewController.onDestroy();
		_topViewController.onDestroy();
		_updateViewController.onDestroy();
		_websiteViewController.onDestroy();
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

	public void SelecteYoutubeId(View view) {
		_logger.Debug("SelecteYoutubeId");
		_topViewController.SelecteYoutubeId();
	}
}
