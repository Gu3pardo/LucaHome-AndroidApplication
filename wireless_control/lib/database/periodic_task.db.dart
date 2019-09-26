import 'package:wireless_control/database/entity.db.dart';
import 'package:wireless_control/models/periodic_task.model.dart';

class PeriodicTaskDb extends EntityDb<PeriodicTask> {
  PeriodicTaskDb()
      : super(
            "CREATE TABLE periodic_tasks(id INTEGER PRIMARY KEY, name TEXT, wirelessSocketId INTEGER, wirelessSocketState INTEGER, weekday INTEGER, hour INTEGER, minute INTEGER, periodic INTEGER, active INTEGER)",
            'periodic_task_database.db',
            'periodic_tasks',
            (map) => PeriodicTask.fromMap(map));
}
