import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';
import 'package:wireless_control/models/area.model.dart';

// Helpful link: https://flutter.dev/docs/cookbook/persistence/sqlite

class AreaDb {
  final String _tableName = 'areas';

  AreaDb();

  Future<Database> _database() async {
    return openDatabase(
      join(await getDatabasesPath(), 'area_database.db'),
      onCreate: (db, version) {
        return db.execute(
          "CREATE TABLE areas(id INTEGER PRIMARY KEY, name TEXT, filter TEXT, deletable INTEGER)",
        );
      },
      version: 1,
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

  Future<void> insert(Area area) async {
    final Database database = await _database();
    await database.insert(
      _tableName,
      area.toJson(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<List<Area>> read() async {
    final Database database = await _database();
    final List<Map<String, dynamic>> maps = await database.query(_tableName);
    return List.generate(maps.length, (i) {
      return Area.fromJson(maps[i]);
    });
  }

  Future<void> update(Area area) async {
    final Database database = await _database();
    await database.update(
      _tableName,
      area.toJson(),
      where: "id = ?",
      whereArgs: [area.id],
    );
  }
}
