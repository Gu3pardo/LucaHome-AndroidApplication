package guepardoapps.lucahome.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Color;
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.enums.MainServiceAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.customadapter.ShoppingListAdapter;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.toastview.ToastView;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.constants.Bundles;

public class ShoppingListView extends Activity {

	private static final String TAG = ShoppingListView.class.getSimpleName();
	private LucaHomeLogger _logger;

	private boolean _isInitialized;
	private SerializableList<ShoppingEntryDto> _shoppingList;

	private ProgressBar _progressBar;
	private ListView _listView;
	private Button _buttonAdd;

	private ListAdapter _listAdapter;

	private Context _context;

	private BroadcastController _broadcastController;
	private LucaDialogController _dialogController;
	private NavigationService _navigationService;
	private ReceiverController _receiverController;

	private Runnable _getDataRunnable = new Runnable() {
		public void run() {
			_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
					new String[] { Bundles.MAIN_SERVICE_ACTION }, new Object[] { MainServiceAction.GET_SHOPPING_LIST });
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

				_listAdapter = new ShoppingListAdapter(_context, _shoppingList);
				_listView.setAdapter(_listAdapter);

				setTitle(String.valueOf(_shoppingList.getSize()) + " entries");
			}

			_progressBar.setVisibility(View.GONE);
			_listView.setVisibility(View.VISIBLE);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_skeleton_list);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));

		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_context = this;

		_broadcastController = new BroadcastController(_context);
		_dialogController = new LucaDialogController(_context);
		_navigationService = new NavigationService(_context);
		_receiverController = new ReceiverController(_context);

		_listView = (ListView) findViewById(R.id.listView);
		_progressBar = (ProgressBar) findViewById(R.id.progressBarListView);

		_buttonAdd = (Button) findViewById(R.id.buttonAddListView);
		_buttonAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_logger.Debug("onClick _buttonAdd");
				int size = 0;
				if (_shoppingList != null) {
					size = _shoppingList.getSize();
				}
				_dialogController.ShowAddShoppingEntryDialog(_getDataRunnable, null, true, false, size);
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
						new String[] { Broadcasts.UPDATE_SHOPPING_LIST });
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
		_receiverController.UnregisterReceiver(_updateListReceiver);
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
					ToastView.warning(_context, "Nothing to share!", Toast.LENGTH_LONG).show();
				}
			} else {
				ToastView.warning(_context, "Nothing to share!", Toast.LENGTH_LONG).show();
			}
		} else if (id == R.id.buttonReload) {
			_broadcastController.SendSimpleBroadcast(
					guepardoapps.library.lucahome.common.constants.Broadcasts.RELOAD_SHOPPING_LIST);
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
