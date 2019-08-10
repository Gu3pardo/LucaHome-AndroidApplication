import 'package:wireless_control/database/area.db.dart';
import 'package:wireless_control/models/area.model.dart';
import 'package:wireless_control/utils/iterable.util.dart';

class AreaService {
  AreaService();

  void syncDatabase(List<Area> areaList) async {
    final db = AreaDb();
    final existingList = await db.read();
    areaList.forEach((area) => {
          if (firstOrNullWhere(existingList, (x) => x.id == area.id) != null)
            db.update(area)
          else
            db.insert(area)
        });
    existingList.forEach((area) => {
          if (firstOrNullWhere(areaList, (x) => x.id == area.id) == null)
            db.delete(area.id)
        });
  }

  void add(Area area) async {
    final db = AreaDb();
    db.insert(area);
  }

  void update(Area area) async {
    final db = AreaDb();
    db.update(area);
  }

  void delete(Area area) async {
    final db = AreaDb();
    db.delete(area.id);
  }
}
