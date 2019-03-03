import 'package:flutter/foundation.dart';

class RouteChange {
  final String route;

  RouteChange({@required this.route});

  @override
  String toString() {
    return 'RouteChange{route: $route}';
  }
}
