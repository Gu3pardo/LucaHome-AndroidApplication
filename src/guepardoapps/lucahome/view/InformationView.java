package guepardoapps.lucahome.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.Constants;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.customadapter.*;
import guepardoapps.lucahome.dto.*;
import guepardoapps.lucahome.services.helper.NavigationService;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class InformationView extends Activity {

	private static final String TAG = InformationView.class.getName();
	private LucaHomeLogger _logger;

	private boolean _isInitialized;

	private ProgressBar _progressBarAbove;
	private ListView _listViewAbove;

	private ProgressBar _progressBarBelow;
	private ListView _listViewBelow;

	private ListAdapter _listAdapterAbove;
	private ListAdapter _listAdapterBelow;

	private Context _context;

	private BroadcastController _broadcastController;
	private NavigationService _navigationService;
	private ReceiverController _receiverController;

	private Runnable _getDataRunnable = new Runnable() {
		public void run() {
			_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_MAIN_SERVICE_COMMAND,
					Constants.BUNDLE_MAIN_SERVICE_ACTION, MainServiceAction.GET_INFORMATIONS);
			_logger.Debug("Called for Informations!");
			_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_MAIN_SERVICE_COMMAND,
					Constants.BUNDLE_MAIN_SERVICE_ACTION, MainServiceAction.GET_CHANGES);
			_logger.Debug("Called for Changes!");
		}
	};

	private BroadcastReceiver _updateChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateChangeReceiver onReceive");

			@SuppressWarnings("unchecked")
			SerializableList<ChangeDto> list = (SerializableList<ChangeDto>) intent
					.getSerializableExtra(Constants.BUNDLE_CHANGE_LIST);

			if (list != null) {
				_listAdapterBelow = new ChangeListAdapter(_context, list);
				_listViewBelow.setAdapter(_listAdapterBelow);

				_listViewBelow.setVisibility(View.VISIBLE);
			} else {
				_logger.Warn("list is null!");
			}
			
			_progressBarBelow.setVisibility(View.GONE);
		}
	};

	private BroadcastReceiver _updateInformationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateInformationReceiver onReceive");

			InformationDto entry = (InformationDto) intent.getSerializableExtra(Constants.BUNDLE_INFORMATION_SINGLE);

			if (entry != null) {
				_listAdapterAbove = new InformationListAdapter(_context, entry);
				_listViewAbove.setAdapter(_listAdapterAbove);

				_listViewAbove.setVisibility(View.VISIBLE);
			} else {
				_logger.Warn("InformationDto is null!");
			}
			
			_progressBarAbove.setVisibility(View.GONE);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_skeleton_list_double);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Constants.ACTION_BAR_COLOR));

		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_context = this;

		_broadcastController = new BroadcastController(_context);
		_navigationService = new NavigationService(_context);
		_receiverController = new ReceiverController(_context);

		_listViewAbove = (ListView) findViewById(R.id.listViewAbove);
		_progressBarAbove = (ProgressBar) findViewById(R.id.progressBarListViewAbove);

		_listViewBelow = (ListView) findViewById(R.id.listViewBelow);
		_progressBarBelow = (ProgressBar) findViewById(R.id.progressBarListViewBelow);
	}

	@Override
	public void onResume() {
		super.onResume();
		_logger.Debug("onResume");
		if (!_isInitialized) {
			if (_receiverController != null && _broadcastController != null) {
				_receiverController.RegisterReceiver(_updateChangeReceiver,
						new String[] { Constants.BROADCAST_UPDATE_CHANGE });
				_receiverController.RegisterReceiver(_updateInformationReceiver,
						new String[] { Constants.BROADCAST_UPDATE_INFORMATION });
				_isInitialized = true;
				_getDataRunnable.run();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		_logger.Debug("onPause");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_updateChangeReceiver);
		_receiverController.UnregisterReceiver(_updateInformationReceiver);
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
