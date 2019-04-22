import 'package:wireless_control/enums/weekday.enum.dart';
import 'package:wireless_control/models/periodic_task.model.dart';

String getDateTimeString(PeriodicTask periodicTask) {
  var weekday = Weekday.values[periodicTask.weekday  - 1];
  return "$weekday, ${periodicTask.hour.toString().padLeft(2)}:${periodicTask.minute.toString().padLeft(2)}";
}