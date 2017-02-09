package guepardoapps.lucahome.common.controller;

import android.content.Context;

import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.common.dto.ActionDto;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.database.DatabaseActionStore;

public class DatabaseController {

	private static final DatabaseController DATABASE_CONTROLLER_SINGLETON = new DatabaseController();

	private static final String TAG = DatabaseController.class.getName();
	private LucaHomeLogger _logger;

	private Context _context;
	private boolean _isInitialized;

	private static DatabaseActionStore _databaseActionStore;

	private DatabaseController() {
		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("DatabaseController created...");
	}

	public static DatabaseController getInstance() {
		return DATABASE_CONTROLLER_SINGLETON;
	}

	public void onCreate(Context context) {
		_logger.Debug("onCreate");
		if (_isInitialized) {
			_logger.Warn("Already initialized!");
			return;
		}

		_context = context;

		_databaseActionStore = new DatabaseActionStore(_context);

		_isInitialized = true;
	}

	public void onPause() {
		_logger.Debug("onPause");
		_isInitialized = false;
	}

	public void onResume(Context context) {
		_logger.Debug("onResume");
		if (_isInitialized) {
			_logger.Warn("Already initialized!");
			return;
		}

		_context = context;

		_databaseActionStore = new DatabaseActionStore(_context);

		_isInitialized = true;
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_isInitialized = false;
	}

	public SerializableList<ActionDto> GetActions() {
		if (!_isInitialized) {
			_logger.Error("Not initialized!");
			return null;
		}
		_databaseActionStore.Open();
		SerializableList<ActionDto> list = _databaseActionStore.GetStoredActions();
		_databaseActionStore.Close();
		return list;
	}

	public boolean SaveAction(ActionDto newEntry) {
		if (!_isInitialized) {
			_logger.Error("Not initialized!");
			return false;
		}
		_databaseActionStore.Open();
		_databaseActionStore.CreateEntry(newEntry);
		_databaseActionStore.Close();

		return true;
	}

	public void DeleteAction(ActionDto deleteEntry) {
		if (!_isInitialized) {
			_logger.Error("Not initialized!");
			return;
		}
		_databaseActionStore.Open();
		_databaseActionStore.Delete(deleteEntry);
		_databaseActionStore.Close();
	}

	public void ClearDatabaseActions() {
		if (!_isInitialized) {
			_logger.Error("Not initialized!");
			return;
		}
		_databaseActionStore.Open();
		_databaseActionStore.Close();
		_databaseActionStore.Remove();
	}
}
