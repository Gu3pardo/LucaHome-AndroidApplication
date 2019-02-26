import 'package:flutter/foundation.dart';
import 'package:lucahome_flutter/models/next_cloud_credentials.model.dart';

class LogIn {
  LogIn();
}

class LogInSuccessful {
  final NextCloudCredentials user;

  LogInSuccessful({@required this.user});

  @override
  String toString() {
    return 'LogIn{user: $user}';
  }
}

class LogInFail {
  final dynamic error;

  LogInFail(this.error);

  @override
  String toString() {
    return 'LogIn{There was an error logging in: $error}';
  }
}

class LogOut {}

class LogOutSuccessful {
  LogOutSuccessful();

  @override
  String toString() {
    return 'LogOut{user: null}';
  }
}

class LogOutFail {
  final dynamic error;

  LogOutFail(this.error);

  String toString() {
    return '{There was an error logging out: $error}';
  }
}
