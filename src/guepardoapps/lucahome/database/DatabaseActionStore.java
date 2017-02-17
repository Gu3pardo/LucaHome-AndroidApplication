package guepardoapps.lucahome.database;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.common.dto.ActionDto;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;

public class DatabaseActionStore {

	private static final String TAG = DatabaseActionStore.class.getName();
	@SuppressWarnings("unused")
	private LucaHomeLogger _logger;

	public static final String KEY_ROWID = "_id";
	public static final String KEY_SOCKET_NAME = "_socket";
	public static final String KEY_ACTION = "_action";

	private static final String DATABASE_NAME = "DatabaseActionStoreDb";
	private static final String DATABASE_TABLE = "DatabaseActionStoreTable";
	private static final int DATABASE_VERSION = 1;

	private DatabaseHelper _databaseHelper;
	private final Context _context;
	private SQLiteDatabase _database;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			database.execSQL(
					" CREATE TABLE " + DATABASE_TABLE + " ( " 
							+ KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
							+ KEY_SOCKET_NAME + " TEXT NOT NULL, " 
							+ KEY_ACTION + " TEXT NOT NULL); ");
		}

		@Override
		public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
			database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(database);
		}

		public void Remove(Context context) {
			context.deleteDatabase(DATABASE_NAME);
		}
	}

	public DatabaseActionStore(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;
	}

	public DatabaseActionStore Open() throws SQLException {
		_databaseHelper = new DatabaseHelper(_context);
		_database = _databaseHelper.getWritableDatabase();
		return this;
	}

	public void Close() {
		_databaseHelper.close();
	}

	public long CreateEntry(ActionDto newEntry) {
		ContentValues contentValues = new ContentValues();

		contentValues.put(KEY_SOCKET_NAME, newEntry.GetSocket());
		contentValues.put(KEY_ACTION, newEntry.GetAction());

		return _database.insert(DATABASE_TABLE, null, contentValues);
	}

	public SerializableList<ActionDto> GetStoredActions() {
		String[] columns = new String[] { 
				KEY_ROWID, 
				KEY_SOCKET_NAME, 
				KEY_ACTION };

		Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
		SerializableList<ActionDto> result = new SerializableList<ActionDto>();

		int idIndex = cursor.getColumnIndex(KEY_ROWID);
		int socketIndex = cursor.getColumnIndex(KEY_SOCKET_NAME);
		int actionIndex = cursor.getColumnIndex(KEY_ACTION);

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int id = cursor.getInt(idIndex);
			String socket = cursor.getString(socketIndex);
			String action = cursor.getString(actionIndex);

			ActionDto entry = new ActionDto(id, socket, action);
			result.addValue(entry);
		}

		return result;
	}

	public void Delete(ActionDto deleteEntry) throws SQLException {
		_database.delete(DATABASE_TABLE, KEY_ROWID + "=" + deleteEntry.GetId(), null);
	}

	public void Remove() {
		_databaseHelper.Remove(_context);
	}
}
