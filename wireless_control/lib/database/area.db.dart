import 'package:wireless_control/database/entity.db.dart';
import 'package:wireless_control/models/area.model.dart';

class AreaDb extends EntityDb<Area> {
  AreaDb()
      : super(
            "CREATE TABLE areas(id INTEGER PRIMARY KEY, name TEXT, filter TEXT, deletable INTEGER)",
            'area_database.db',
            'areas',
            (map) => Area.fromMap(map));
}
