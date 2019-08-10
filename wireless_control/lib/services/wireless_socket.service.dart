import 'package:wireless_control/database/wireless_socket.db.dart';
import 'package:wireless_control/models/wireless_socket.model.dart';
import 'package:wireless_control/utils/iterable.util.dart';

class WirelessSocketService {
  WirelessSocketService();

  void syncDatabase(List<WirelessSocket> wirelessSocketList) async {
    final db = WirelessSocketDb();
    final existingList = await db.read();
    wirelessSocketList.forEach((wirelessSocket) => {
          if (firstOrNullWhere(existingList, (x) => x.id == wirelessSocket.id) != null)
            db.update(wirelessSocket)
          else
            db.insert(wirelessSocket)
        });
    existingList.forEach((wirelessSocket) => {
          if (firstOrNullWhere(wirelessSocketList, (x) => x.id == wirelessSocket.id) == null)
            db.delete(wirelessSocket.id)
    });
  }

  void add(WirelessSocket wirelessSocket) async {
    final db = WirelessSocketDb();
    db.insert(wirelessSocket);
  }

  void update(WirelessSocket wirelessSocket) async {
    final db = WirelessSocketDb();
    db.update(wirelessSocket);
  }

  void delete(WirelessSocket wirelessSocket) async {
    final db = WirelessSocketDb();
    db.delete(wirelessSocket.id);
  }
}
