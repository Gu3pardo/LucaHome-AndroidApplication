package guepardoapps.lucahome.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.Constants;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.classes.*;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.customadapter.*;
import guepardoapps.lucahome.dto.*;
import guepardoapps.lucahome.services.helper.DialogService;
import guepardoapps.lucahome.services.helper.NavigationService;
import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class ScheduleView extends Activity {

	private static final String TAG = ScheduleView.class.getName();
	private LucaHomeLogger _logger;

	private boolean _isInitialized;
	private SerializableList<WirelessSocketDto> _socketList;

	private ProgressBar _progressBar;
	private ListView _listView;
	private Button _buttonAdd;

	private ListAdapter _listAdapter;

	private Context _context;

	private BroadcastController _broadcastController;
	private DialogService _dialogService;
	private NavigationService _navigationService;
	private ReceiverController _receiverController;

	private Runnable _getDataRunnable = new Runnable() {
		public void run() {
			_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_MAIN_SERVICE_COMMAND,
					new String[] { Constants.BUNDLE_MAIN_SERVICE_ACTION },
					new Object[] { MainServiceAction.GET_SCHEDULES });
		}
	};

	private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateReceiver onReceive");

			SerializableList<ScheduleDto> list = (SerializableList<ScheduleDto>) intent
					.getSerializableExtra(Constants.BUNDLE_SCHEDULE_LIST);
			_socketList = (SerializableList<WirelessSocketDto>) intent
					.getSerializableExtra(Constants.BUNDLE_SOCKET_LIST);

			if (list != null) {
				_listAdapter = new ScheduleListAdapter(_context, list, _socketList);
				_listView.setAdapter(_listAdapter);

				_progressBar.setVisibility(View.GONE);
				_listView.setVisibility(View.VISIBLE);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_skeleton_list);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Constants.ACTION_BAR_COLOR));

		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_context = this;

		_broadcastController = new BroadcastController(_context);
		_dialogService = new DialogService(_context);
		_navigationService = new NavigationService(_context);
		_receiverController = new ReceiverController(_context);

		_listView = (ListView) findViewById(R.id.listView);
		_progressBar = (ProgressBar) findViewById(R.id.progressBarListView);

		_buttonAdd = (Button) findViewById(R.id.buttonAddListView);
		_buttonAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_logger.Debug("onClick _buttonAdd");
				if (_socketList != null) {
					_dialogService.ShowAddScheduleDialog(_getDataRunnable, _socketList, null, true);
				} else {
					_logger.Warn("SocketList is null!");
					Toast.makeText(_context, "SocketList is null!", Toast.LENGTH_LONG).show();
				}
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
				_receiverController.RegisterReceiver(_updateReceiver,
						new String[] { Constants.BROADCAST_UPDATE_SCHEDULE });
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
		_receiverController.UnregisterReceiver(_updateReceiver);
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
