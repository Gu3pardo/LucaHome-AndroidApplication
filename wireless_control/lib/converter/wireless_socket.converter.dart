import 'package:wireless_control/models/wireless_socket.model.dart';

List<WirelessSocket> createList(List<dynamic> jsonList) {
  List<WirelessSocket> wirelessSocketList = new List();
  for (int index = 0; index < jsonList.length; index++) {
    dynamic entry = jsonList[index];
    wirelessSocketList.add(WirelessSocket.fromJson(entry));
  }
  return wirelessSocketList;
}
