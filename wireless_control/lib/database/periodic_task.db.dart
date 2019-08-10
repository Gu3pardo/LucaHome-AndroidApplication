import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';
import 'package:wireless_control/models/periodic_task.model.dart';

// Helpful link: https://flutter.dev/docs/cookbook/persistence/sqlite

class PeriodicTaskDb {
  final String _tableName = 'periodic_tasks';

  PeriodicTaskDb();

  Future<Database> _database() async {
    return openDatabase(
      join(await getDatabasesPath(), 'periodic_task_database.db'),
      onCreate: (db, version) {
        return db.execute(
          "CREATE TABLE periodic_tasks(id INTEGER PRIMARY KEY, name TEXT, wirelessSocketId INTEGER, wirelessSocketState INTEGER, weekday INTEGER, hour INTEGER, minute INTEGER, periodic INTEGER, active INTEGER)",
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

  Future<void> insert(PeriodicTask periodicTask) async {
    final Database database = await _database();
    await database.insert(
      _tableName,
      periodicTask.toJson(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<List<PeriodicTask>> read() async {
    final Database database = await _database();
    final List<Map<String, dynamic>> maps = await database.query(_tableName);
    return List.generate(maps.length, (i) {
      return PeriodicTask.fromJson(maps[i]);
    });
  }

  Future<void> update(PeriodicTask periodicTask) async {
    final Database database = await _database();
    await database.update(
      _tableName,
      periodicTask.toJson(),
      where: "id = ?",
      whereArgs: [periodicTask.id],
    );
  }
}
