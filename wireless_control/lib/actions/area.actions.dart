import 'package:flutter/foundation.dart';
import 'package:wireless_control/models/area.model.dart';

class AreaLoad {
  AreaLoad();
}

class AreaLoadSuccessful {
  final List<Area> list;

  AreaLoadSuccessful({@required this.list});

  @override
  String toString() {
    return 'AreaLoadSuccessful{areaList: $list}';
  }
}

class AreaLoadFail {
  final dynamic error;

  AreaLoadFail(this.error);

  @override
  String toString() {
    return 'AreaLoadFail{There was an error loading: $error}';
  }
}

class AreaSelect {
  AreaSelect();
}

class AreaSelectSuccessful {
  final Area area;

  AreaSelectSuccessful({@required this.area});

  @override
  String toString() {
    return 'AreaSelectSuccessful{area: $area}';
  }
}

class AreaSelectFail {
  final dynamic error;

  AreaSelectFail(this.error);

  @override
  String toString() {
    return 'AreaSelectFail{There was an error selecting: $error}';
  }
}

class AreaAdd {
  final Area area;

  AreaAdd({@required this.area});

  @override
  String toString() {
    return 'AreaAdd{area: $area}';
  }
}

class AreaAddOnServer {
  AreaAddOnServer();
}

class AreaAddSuccessful {
  final Area area;

  AreaAddSuccessful({@required this.area});

  @override
  String toString() {
    return 'AreaAddSuccessful{area: $area}';
  }
}

class AreaAddFail {
  final dynamic error;

  AreaAddFail(this.error);

  @override
  String toString() {
    return 'AreaAddFail{There was an error adding: $error}';
  }
}

class AreaUpdateOnServer {
  AreaUpdateOnServer();
}

class AreaUpdateSuccessful {
  final Area area;

  AreaUpdateSuccessful({@required this.area});

  @override
  String toString() {
    return 'AreaUpdateSuccessful{area: $area}';
  }
}

class AreaUpdateFail {
  final dynamic error;

  AreaUpdateFail(this.error);

  @override
  String toString() {
    return 'AreaUpdateFail{There was an error updating: $error}';
  }
}

class AreaDeleteOnServer {
  AreaDeleteOnServer();
}

class AreaDeleteSuccessful {
  final Area area;

  AreaDeleteSuccessful({@required this.area});

  @override
  String toString() {
    return 'AreaDeleteSuccessful{area: $area}';
  }
}

class AreaDeleteFail {
  final dynamic error;

  AreaDeleteFail(this.error);

  @override
  String toString() {
    return 'AreaDeleteFail{There was an error deleting: $error}';
  }
}
