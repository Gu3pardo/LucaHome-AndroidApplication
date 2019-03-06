import 'package:wireless_control/models/area.model.dart';

List<Area> createList(List<dynamic> jsonList) {
  List<Area> areaList = new List();
  for (int index = 0; index < jsonList.length; index++) {
    dynamic entry = jsonList[index];
    Area area = new Area(
        id: int.parse(entry["id"]),
        name: entry["name"],
        filter: entry["filter"],
        deletable: int.parse(entry["deletable"]));
    areaList.add(area);
  }
  return areaList;
}
