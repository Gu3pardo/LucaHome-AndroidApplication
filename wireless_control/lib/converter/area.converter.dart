import 'package:wireless_control/models/area.model.dart';

List<Area> createList(List<dynamic> jsonList) {
  List<Area> areaList = new List();
  for (int index = 0; index < jsonList.length; index++) {
    dynamic entry = jsonList[index];
    areaList.add(Area.fromJson(entry));
  }
  return areaList;
}
