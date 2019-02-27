import 'package:flutter/foundation.dart';
import 'package:lucahome_flutter/models/next_cloud_credentials.model.dart';

class NextCloudCredentialsLogIn {
  NextCloudCredentialsLogIn();
}

class NextCloudCredentialsLogInSuccessful {
  final NextCloudCredentials user;

  NextCloudCredentialsLogInSuccessful({@required this.user});

  @override
  String toString() {
    return 'NextCloudCredentialsLogInSuccessful{user: $user}';
  }
}

class NextCloudCredentialsLogInFail {
  final dynamic error;

  NextCloudCredentialsLogInFail(this.error);

  @override
  String toString() {
    return 'NextCloudCredentialsLogInFail{There was an error logging in: $error}';
  }
}

class NextCloudCredentialsLogOut {}

class NextCloudCredentialsLogOutSuccessful {
  NextCloudCredentialsLogOutSuccessful();

  @override
  String toString() {
    return 'NextCloudCredentialsLogOutSuccessful{user: null}';
  }
}

class NextCloudCredentialsLogOutFail {
  final dynamic error;

  NextCloudCredentialsLogOutFail(this.error);

  String toString() {
    return 'NextCloudCredentialsLogOutFail{There was an error logging out: $error}';
  }
}
