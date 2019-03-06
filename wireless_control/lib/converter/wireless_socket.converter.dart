import 'package:wireless_control/models/wireless_socket.model.dart';

List<WirelessSocket> createList(List<dynamic> jsonList) {
  List<WirelessSocket> wirelessSocketList = new List();
  for (int index = 0; index < jsonList.length; index++) {
    dynamic entry = jsonList[index];
    WirelessSocket wirelessSocket = new WirelessSocket(
        id: int.parse(entry["id"]),
        name: entry["name"],
        code: entry["code"],
        area: entry["area"],
        state: int.parse(entry["state"]),
        description: entry["description"],
        icon: entry["icon"],
        deletable: int.parse(entry["deletable"]));
    wirelessSocketList.add(wirelessSocket);
  }
  return wirelessSocketList;
}
