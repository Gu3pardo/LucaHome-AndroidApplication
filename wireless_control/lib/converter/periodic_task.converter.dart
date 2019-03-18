import 'package:wireless_control/models/periodic_task.model.dart';

List<PeriodicTask> createList(List<dynamic> jsonList) {
  List<PeriodicTask> periodicTaskList = new List();
  for (int index = 0; index < jsonList.length; index++) {
    dynamic entry = jsonList[index];
    PeriodicTask area = new PeriodicTask(
        id: int.parse(entry["id"]),
        name: entry["name"],
        wirelessSocketId: entry["wirelessSocketId"],
        wirelessSocketState: int.parse(entry["wirelessSocketState"]),
        weekday: int.parse(entry["weekday"]),
        hour: int.parse(entry["hour"]),
        minute: int.parse(entry["minute"]),
        periodic: int.parse(entry["periodic"]),
        active: int.parse(entry["active"]));
    periodicTaskList.add(area);
  }
  return periodicTaskList;
}
