import 'package:flutter/foundation.dart';
import 'package:wireless_control/models/wireless_socket.model.dart';

class WirelessSocketLoad {
  WirelessSocketLoad();
}

class WirelessSocketLoadSuccessful {
  final List<WirelessSocket> list;

  WirelessSocketLoadSuccessful({@required this.list});

  @override
  String toString() {
    return 'WirelessSocketLoadSuccessful{list: $list}';
  }
}

class WirelessSocketLoadFail {
  final dynamic error;

  WirelessSocketLoadFail(this.error);

  @override
  String toString() {
    return 'WirelessSocketLoadFail{There was an error loading: $error}';
  }
}

class WirelessSocketSelect {
  WirelessSocketSelect();
}

class WirelessSocketSelectSuccessful {
  final WirelessSocket wirelessSocket;

  WirelessSocketSelectSuccessful({@required this.wirelessSocket});

  @override
  String toString() {
    return 'WirelessSocketSelectSuccessful{wirelessSocket: $wirelessSocket}';
  }
}

class WirelessSocketSelectFail {
  final dynamic error;

  WirelessSocketSelectFail(this.error);

  @override
  String toString() {
    return 'WirelessSocketSelectFail{There was an error selecting: $error}';
  }
}

class WirelessSocketAddOnServer {
  WirelessSocketAddOnServer();
}

class WirelessSocketAdd {
  final WirelessSocket wirelessSocket;

  WirelessSocketAdd({@required this.wirelessSocket});

  @override
  String toString() {
    return 'WirelessSocketAdd{wirelessSocket: $wirelessSocket}';
  }
}

class WirelessSocketAddSuccessful {
  final WirelessSocket wirelessSocket;

  WirelessSocketAddSuccessful({@required this.wirelessSocket});

  @override
  String toString() {
    return 'WirelessSocketAddSuccessful{wirelessSocket: $wirelessSocket}';
  }
}

class WirelessSocketAddFail {
  final dynamic error;

  WirelessSocketAddFail(this.error);

  @override
  String toString() {
    return 'WirelessSocketAddFail{There was an error adding: $error}';
  }
}

class WirelessSocketUpdateOnServer {
  WirelessSocketUpdateOnServer();
}

class WirelessSocketUpdateSuccessful {
  final WirelessSocket wirelessSocket;

  WirelessSocketUpdateSuccessful({@required this.wirelessSocket});

  @override
  String toString() {
    return 'WirelessSocketUpdateSuccessful{wirelessSocket: $wirelessSocket}';
  }
}

class WirelessSocketUpdateFail {
  final dynamic error;

  WirelessSocketUpdateFail(this.error);

  @override
  String toString() {
    return 'WirelessSocketUpdateFail{There was an error updating: $error}';
  }
}

class WirelessSocketDeleteOnServer {
  WirelessSocketDeleteOnServer();
}

class WirelessSocketDeleteSuccessful {
  final WirelessSocket wirelessSocket;

  WirelessSocketDeleteSuccessful({@required this.wirelessSocket});

  @override
  String toString() {
    return 'WirelessSocketDeleteSuccessful{wirelessSocket: $wirelessSocket}';
  }
}

class WirelessSocketDeleteFail {
  final dynamic error;

  WirelessSocketDeleteFail(this.error);

  @override
  String toString() {
    return 'WirelessSocketDeleteFail{There was an error deleting: $error}';
  }
}
