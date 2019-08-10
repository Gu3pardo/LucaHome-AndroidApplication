import 'package:wireless_control/database/periodic_task.db.dart';
import 'package:wireless_control/models/periodic_task.model.dart';
import 'package:wireless_control/utils/iterable.util.dart';

class PeriodicTaskService {
  PeriodicTaskService();

  void syncDatabase(List<PeriodicTask> periodicTaskList) async {
    final db = PeriodicTaskDb();
    final existingList = await db.read();
    periodicTaskList.forEach((periodicTask) => {
          if (firstOrNullWhere(existingList, (x) => x.id == periodicTask.id) != null)
            db.update(periodicTask)
          else
            db.insert(periodicTask)
        });
    existingList.forEach((periodicTask) => {
          if (firstOrNullWhere(periodicTaskList, (x) => x.id == periodicTask.id) == null)
            db.delete(periodicTask.id)
    });
  }

  void add(PeriodicTask periodicTask) async {
    final db = PeriodicTaskDb();
    db.insert(periodicTask);
  }

  void update(PeriodicTask periodicTask) async {
    final db = PeriodicTaskDb();
    db.update(periodicTask);
  }

  void delete(PeriodicTask periodicTask) async {
    final db = PeriodicTaskDb();
    db.delete(periodicTask.id);
  }
}
