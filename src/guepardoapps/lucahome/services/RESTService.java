package guepardoapps.lucahome.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.constants.ServerActions;
import guepardoapps.lucahome.common.controller.DatabaseController;
import guepardoapps.lucahome.common.dto.ActionDto;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.DialogController;
import guepardoapps.toolset.controller.NetworkController;

public class RESTService extends Service {

	private static final String TAG = RESTService.class.getName();
	private LucaHomeLogger _logger;

	private String[] _actions;

	private Context _context;
	private DatabaseController _databaseController;
	private NetworkController _networkController;

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}

		if (_context == null) {
			_context = this;
		}
		if (_networkController == null) {
			if (_context != null) {
				_networkController = new NetworkController(_context,
						new DialogController(_context, ContextCompat.getColor(_context, R.color.TextIcon),
								ContextCompat.getColor(_context, R.color.Background)));
			}
		}

		Bundle bundle = intent.getExtras();
		if (bundle == null) {
			_logger.Warn("Bundle is null!");
			stopSelf();
			return -1;
		}

		String action = bundle.getString(Bundles.ACTION);
		if (action == null) {
			_logger.Warn("Action is null!");
			stopSelf();
			return -1;
		}
		_logger.Debug("Action: " + action);

		String name = bundle.getString(Bundles.NAME);

		if (_databaseController == null) {
			_databaseController = DatabaseController.getInstance();
			_databaseController.onCreate(_context);
		}

		if (!_networkController.IsNetworkAvailable()) {
			_logger.Warn("No network available!");
			if (action.contains(ServerActions.SET_SOCKET)) {
				storeAction(name, action);
			}
			return 0;
		}

		if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
			_logger.Warn("No LucaHome network! ...");
			if (action.contains(ServerActions.SET_SOCKET)) {
				storeAction(name, action);
			}
			return 0;
		}

		String user = bundle.getString(Bundles.USER);
		String password = bundle.getString(Bundles.PASSPHRASE);
		if (user == null) {
			_logger.Warn("No user!");
			return 101;
		}
		if (password == null) {
			_logger.Warn("No password!");
			return 102;
		}

		if (Constants.SERVER_URLs.length == 0) {
			_logger.Error("You did not enter server ips!");
			Toasty.error(_context, "You did not enter server ips!", Toast.LENGTH_LONG).show();
			return 103;
		}

		String url = Constants.SERVER_URLs[0] + Constants.ACTION_PATH + user + "&password=" + password + "&action="
				+ action;
		_logger.Debug("Url: " + url);
		_actions = new String[] { url };

		String broadcast = bundle.getString(Bundles.BROADCAST);
		LucaObject lucaObject = (LucaObject) bundle.getSerializable(Bundles.LUCA_OBJECT);

		SendActionTask task = new SendActionTask();
		task.setValues(name, broadcast, lucaObject, _actions.length);
		task.execute(_actions);

		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private void storeAction(String socketName, String action) {
		_logger.Debug("Store action: " + action);

		int id = -1;
		String socket = socketName;
		String socketAction = "-1";

		if (action.contains(Constants.STATE_ON)) {
			socketAction = Constants.STATE_ON;
		} else if (action.contains(Constants.STATE_OFF)) {
			socketAction = Constants.STATE_OFF;
		} else {
			_logger.Error("Failed to get action for socket to store: " + action);
			Toasty.error(_context, "Couldnot save action!", Toast.LENGTH_SHORT).show();
			return;
		}

		ActionDto storeAction = new ActionDto(id, socket, socketAction);
		if (_databaseController.SaveAction(storeAction)) {
			Toasty.success(_context, "Saved action!", Toast.LENGTH_SHORT).show();
		} else {
			Toasty.error(_context, "Couldnot save action!", Toast.LENGTH_SHORT).show();
		}
	}

	private class SendActionTask extends AsyncTask<String, Void, String> {
		private static final String TAG = "RESTService";

		private LucaHomeLogger _logger;

		private String _name;
		private String _broadcast;
		private LucaObject _lucaObject;

		private String[] _answer;

		public void setValues(String name, String broadcast, LucaObject lucaObject, int answerSize) {
			_logger = new LucaHomeLogger(TAG);

			_name = name;
			_broadcast = broadcast;
			_lucaObject = lucaObject;
			_answer = new String[answerSize];
		}

		@Override
		protected String doInBackground(String... actions) {
			String response = "";
			int answerIndex = 0;
			boolean downloadSuccess = false;

			for (String action : actions) {
				try {
					_logger.Debug("action: " + action);
					response = "";

					URL url = new URL(action);
					URLConnection connection = url.openConnection();
					InputStream inputStream = connection.getInputStream();

					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

					String line;
					while ((line = reader.readLine()) != null) {
						response += line;
					}

					downloadSuccess = true;
					_answer[answerIndex] = response;
					_logger.Debug(response);

				} catch (IOException e) {
					downloadSuccess = false;
					_logger.Error(e.getMessage());
				} finally {
					answerIndex++;
				}
			}

			return String.valueOf(downloadSuccess);
		}

		@Override
		protected void onPostExecute(String result) {
			_logger.Debug("downloadSuccess: " + result);

			if (result.contains(String.valueOf(false))) {
				_logger.Error("Failed to download!");
				stopSelf();
				return;
			}

			// Hack for deactivating all sockets
			if (_name.contains("SHOW_NOTIFICATION_SOCKET")) {
				BroadcastController broadcastController = new BroadcastController(RESTService.this);
				broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
						new String[] { Bundles.MAIN_SERVICE_ACTION },
						new Object[] { MainServiceAction.DOWLOAD_SOCKETS });
			}
			// End hack
			else {
				if (_broadcast != null && _broadcast != "") {
					Intent broadcastIntent = new Intent(_broadcast);
					Bundle broadcastData = new Bundle();
					broadcastData.putStringArray(_name, _answer);
					broadcastData.putSerializable(Bundles.LUCA_OBJECT, _lucaObject);
					broadcastIntent.putExtras(broadcastData);
					sendBroadcast(broadcastIntent);
				}
			}

			stopSelf();
		}
	}
}
