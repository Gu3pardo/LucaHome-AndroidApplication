import 'package:wireless_control/actions/route.actions.dart';
import 'package:redux/redux.dart';

String _routeChange(String route, action) => action.route;

final routeReducer = combineReducers<String>([
  new TypedReducer<String, RouteChange>(_routeChange),
]);
