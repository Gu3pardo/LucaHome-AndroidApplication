import 'package:flutter/foundation.dart';
import 'package:wireless_control/models/periodic_task.model.dart';

class PeriodicTaskLoad {
  PeriodicTaskLoad();
}

class PeriodicTaskLoadSuccessful {
  final List<PeriodicTask> list;

  PeriodicTaskLoadSuccessful({@required this.list});

  @override
  String toString() {
    return 'PeriodicTaskLoadSuccessful{PeriodicTaskList: $list}';
  }
}

class PeriodicTaskLoadFail {
  final dynamic error;

  PeriodicTaskLoadFail(this.error);

  @override
  String toString() {
    return 'PeriodicTaskLoadFail{There was an error loading: $error}';
  }
}

class PeriodicTaskSelect {
  PeriodicTaskSelect();
}

class PeriodicTaskSelectSuccessful {
  final PeriodicTask periodicTask;

  PeriodicTaskSelectSuccessful({@required this.periodicTask});

  @override
  String toString() {
    return 'PeriodicTaskSelectSuccessful{PeriodicTask: $periodicTask}';
  }
}

class PeriodicTaskSelectFail {
  final dynamic error;

  PeriodicTaskSelectFail(this.error);

  @override
  String toString() {
    return 'PeriodicTaskSelectFail{There was an error selecting: $error}';
  }
}

class PeriodicTaskAdd {
  final PeriodicTask periodicTask;

  PeriodicTaskAdd({@required this.periodicTask});

  @override
  String toString() {
    return 'PeriodicTaskAdd{PeriodicTask: $periodicTask}';
  }
}

class PeriodicTaskAddOnServer {
  PeriodicTaskAddOnServer();
}

class PeriodicTaskAddSuccessful {
  final PeriodicTask periodicTask;

  PeriodicTaskAddSuccessful({@required this.periodicTask});

  @override
  String toString() {
    return 'PeriodicTaskAddSuccessful{PeriodicTask: $periodicTask}';
  }
}

class PeriodicTaskAddFail {
  final dynamic error;

  PeriodicTaskAddFail(this.error);

  @override
  String toString() {
    return 'PeriodicTaskAddFail{There was an error adding: $error}';
  }
}

class PeriodicTaskUpdateOnServer {
  PeriodicTaskUpdateOnServer();
}

class PeriodicTaskUpdateSuccessful {
  final PeriodicTask periodicTask;

  PeriodicTaskUpdateSuccessful({@required this.periodicTask});

  @override
  String toString() {
    return 'PeriodicTaskUpdateSuccessful{PeriodicTask: $PeriodicTask}';
  }
}

class PeriodicTaskUpdateFail {
  final dynamic error;

  PeriodicTaskUpdateFail(this.error);

  @override
  String toString() {
    return 'PeriodicTaskUpdateFail{There was an error updating: $error}';
  }
}

class PeriodicTaskDeleteOnServer {
  PeriodicTaskDeleteOnServer();
}

class PeriodicTaskDeleteSuccessful {
  final PeriodicTask periodicTask;

  PeriodicTaskDeleteSuccessful({@required this.periodicTask});

  @override
  String toString() {
    return 'PeriodicTaskDeleteSuccessful{PeriodicTask: $periodicTask}';
  }
}

class PeriodicTaskDeleteFail {
  final dynamic error;

  PeriodicTaskDeleteFail(this.error);

  @override
  String toString() {
    return 'PeriodicTaskDeleteFail{There was an error deleting: $error}';
  }
}
