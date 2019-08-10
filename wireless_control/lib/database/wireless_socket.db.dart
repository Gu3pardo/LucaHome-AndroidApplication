import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';
import 'package:wireless_control/models/wireless_socket.model.dart';

// Helpful link: https://flutter.dev/docs/cookbook/persistence/sqlite

class WirelessSocketDb {
  final String _tableName = 'wirelessSockets';

  WirelessSocketDb();

  Future<Database> _database() async {
    return openDatabase(
      join(await getDatabasesPath(), 'wireless_socket_database.db'),
      onCreate: (db, version) {
        return db.execute(
          "CREATE TABLE wirelessSockets(id INTEGER PRIMARY KEY, name TEXT, code TEXT, area TEXT, state INTEGER, description TEXT, icon TEXT, deletable INTEGER)",
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

  Future<void> insert(WirelessSocket wirelessSocket) async {
    final Database database = await _database();
    await database.insert(
      _tableName,
      wirelessSocket.toJson(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<List<WirelessSocket>> read() async {
    final Database database = await _database();
    final List<Map<String, dynamic>> maps = await database.query(_tableName);
    return List.generate(maps.length, (i) {
      return WirelessSocket.fromJson(maps[i]);
    });
  }

  Future<void> update(WirelessSocket wirelessSocket) async {
    final Database database = await _database();
    await database.update(
      _tableName,
      wirelessSocket.toJson(),
      where: "id = ?",
      whereArgs: [wirelessSocket.id],
    );
  }
}
