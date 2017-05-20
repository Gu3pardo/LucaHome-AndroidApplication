package guepardoapps.library.lucahome.controller;

import android.content.Context;

import guepardoapps.library.lucahome.common.dto.ActionDto;
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.database.DatabaseActionStore;
import guepardoapps.library.lucahome.database.DatabaseShoppingList;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class DatabaseController {

	private static final DatabaseController DATABASE_CONTROLLER_SINGLETON = new DatabaseController();

	private static final String TAG = DatabaseController.class.getSimpleName();
	private LucaHomeLogger _logger;

	private Context _context;
	private boolean _isInitialized;

	private static DatabaseActionStore _databaseActionStore;
	private static DatabaseShoppingList _databaseShoppingList;

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
		_databaseShoppingList = new DatabaseShoppingList(_context);

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
		_databaseShoppingList = new DatabaseShoppingList(_context);

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

		removeActionNameIfExists(newEntry.GetName());

		_databaseActionStore.Open();
		_databaseActionStore.CreateEntry(newEntry);
		_databaseActionStore.Close();

		return true;
	}

	public boolean DeleteAction(ActionDto deleteEntry) {
		if (!_isInitialized) {
			_logger.Error("Not initialized!");
			return false;
		}
		_databaseActionStore.Open();
		_databaseActionStore.Delete(deleteEntry);
		_databaseActionStore.Close();

		return true;
	}

	public boolean DeleteActionByName(String deleteActionName) {
		if (!_isInitialized) {
			_logger.Error("Not initialized!");
			return false;
		}

		return removeActionNameIfExists(deleteActionName);
	}

	public void ClearDatabaseActions() {
		if (!_isInitialized) {
			_logger.Error("Not initialized!");
			return;
		}

		SerializableList<ActionDto> actions = GetActions();
		for (int index = 0; index < actions.getSize(); index++) {
			DeleteAction(actions.getValue(index));
		}
	}

	public SerializableList<ShoppingEntryDto> GetShoppingList() {
		if (!_isInitialized) {
			_logger.Error("Not initialized!");
			return null;
		}
		_databaseShoppingList.Open();
		SerializableList<ShoppingEntryDto> list = _databaseShoppingList.GetShoppingList();
		_databaseShoppingList.Close();
		return list;
	}

	public boolean SaveShoppintEntry(ShoppingEntryDto newEntry) {
		if (!_isInitialized) {
			_logger.Error("Not initialized!");
			return false;
		}

		_databaseShoppingList.Open();
		_databaseShoppingList.CreateEntry(newEntry);
		_databaseShoppingList.Close();

		return true;
	}

	public void DeleteShoppingEntry(ShoppingEntryDto deleteEntry) {
		if (!_isInitialized) {
			_logger.Error("Not initialized!");
			return;
		}
		_databaseShoppingList.Open();
		_databaseShoppingList.Delete(deleteEntry);
		_databaseShoppingList.Close();
	}

	public void ClearDatabaseShoppingList() {
		if (!_isInitialized) {
			_logger.Error("Not initialized!");
			return;
		}

		SerializableList<ShoppingEntryDto> entries = GetShoppingList();
		for (int index = 0; index < entries.getSize(); index++) {
			DeleteShoppingEntry(entries.getValue(index));
		}
	}

	private boolean removeActionNameIfExists(String actionName) {
		SerializableList<ActionDto> savedActions = GetActions();

		if (savedActions != null) {
			for (int index = 0; index < savedActions.getSize(); index++) {
				ActionDto entry = savedActions.getValue(index);
				if (entry != null) {
					if (entry.GetName().contains(actionName)) {
						_logger.Info(String.format("Name %s already exists! Removing", entry.GetName()));
						return DeleteAction(entry);
					}
				}
			}
		}

		return false;
	}
}
