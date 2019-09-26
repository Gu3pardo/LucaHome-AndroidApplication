import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';
import 'package:wireless_control/models/entity.model.dart';

class EntityDb<T extends Entity> {
  String _createScript;

  String _databaseName;

  int _databaseVersion;

  String _tableName;

  Function _fromMap;

  EntityDb(String createScript, String databaseName, String tableName, Function fromMap, {int databaseVersion = 1}) {
    this._createScript = createScript;
    this._databaseName = databaseName;
    this._tableName = tableName;
    this._fromMap = fromMap;
    this._databaseVersion = databaseVersion;
  }

  Future<Database> _database() async {
    return openDatabase(
      join(await getDatabasesPath(), this._databaseName),
      onCreate: (db, version) => db.execute(this._createScript),
      version: this._databaseVersion,
    );
  }

  Future<void> delete(int id) async {
    final Database database = await _database();
    await database.delete(
      _tableName,
      where: "id = ?",
      whereArgs: [id],
    );
  }

  Future<void> insert(T entity) async {
    final Database database = await _database();
    await database.insert(
      _tableName,
      entity.toJson(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<List<T>> read() async {
    final Database database = await _database();
    final List<Map<String, dynamic>> maps = await database.query(_tableName);
    return List.generate(maps.length, (i) =>this._fromMap(maps[i]));
  }

  Future<void> update(T entity) async {
    final Database database = await _database();
    await database.update(
      _tableName,
      entity.toJson(),
      where: "id = ?",
      whereArgs: [entity.id],
    );
  }
}
