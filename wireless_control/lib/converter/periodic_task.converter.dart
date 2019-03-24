import 'package:wireless_control/models/periodic_task.model.dart';

List<PeriodicTask> createList(List<dynamic> jsonList) {
  List<PeriodicTask> periodicTaskList = new List();
  for (int index = 0; index < jsonList.length; index++) {
    dynamic entry = jsonList[index];
    periodicTaskList.add(PeriodicTask.fromJson(entry));
  }
  return periodicTaskList;
}
