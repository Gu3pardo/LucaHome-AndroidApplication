import 'package:wireless_control/database/entity.db.dart';
import 'package:wireless_control/models/wireless_socket.model.dart';

class WirelessSocketDb extends EntityDb<WirelessSocket> {
  WirelessSocketDb()
      : super(
            "CREATE TABLE wirelessSockets(id INTEGER PRIMARY KEY, name TEXT, code TEXT, area TEXT, state INTEGER, description TEXT, icon TEXT, deletable INTEGER, lastToggled INTEGER, group TEXT)",
            'wireless_socket_database.db',
            'wirelessSockets',
            (map) => WirelessSocket.fromMap(map));
}
