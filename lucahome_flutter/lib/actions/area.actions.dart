import 'package:flutter/foundation.dart';
import 'package:lucahome_flutter/models/area.model.dart';

class Load {
  Load();
}

class LoadSuccessful {
  final List<Area> areaList;

  LoadSuccessful({@required this.areaList});

  @override
  String toString() {
    return 'Load{areaList: $areaList}';
  }
}

class LoadFail {
  final dynamic error;

  LoadFail(this.error);

  @override
  String toString() {
    return 'Load{There was an error loading: $error}';
  }
}

class Add {
  Add();
}

class AddSuccessful {
  final Area area;

  AddSuccessful({@required this.area});

  @override
  String toString() {
    return 'Add{area: $area}';
  }
}

class AddFail {
  final dynamic error;

  AddFail(this.error);

  @override
  String toString() {
    return 'Add{There was an error adding: $error}';
  }
}

class Update {
  Update();
}

class UpdateSuccessful {
  final Area area;

  UpdateSuccessful({@required this.area});

  @override
  String toString() {
    return 'Update{area: $area}';
  }
}

class UpdateFail {
  final dynamic error;

  UpdateFail(this.error);

  @override
  String toString() {
    return 'Update{There was an error updating: $error}';
  }
}

class Delete {
  Delete();
}

class DeleteSuccessful {
  final Area area;

  DeleteSuccessful({@required this.area});

  @override
  String toString() {
    return 'Delete{area: $area}';
  }
}

class DeleteFail {
  final dynamic error;

  DeleteFail(this.error);

  @override
  String toString() {
    return 'Update{There was an error deleting: $error}';
  }
}
