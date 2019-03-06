import 'package:wireless_control/actions/area.actions.dart';
import 'package:wireless_control/actions/next_cloud_credentials.actions.dart';
import 'package:wireless_control/actions/wireless_socket.actions.dart';
import 'package:redux/redux.dart';

bool _loading(bool loading, action) => true;

bool _finished(bool loading, action) => false;

final loadingAreaReducer = combineReducers<bool>([
  new TypedReducer<bool, AreaLoad>(_loading),
  new TypedReducer<bool, AreaLoadSuccessful>(_finished),
  new TypedReducer<bool, AreaLoadFail>(_finished),
  new TypedReducer<bool, AreaAdd>(_loading),
  new TypedReducer<bool, AreaAddSuccessful>(_finished),
  new TypedReducer<bool, AreaAddFail>(_finished),
  new TypedReducer<bool, AreaUpdate>(_loading),
  new TypedReducer<bool, AreaUpdateSuccessful>(_finished),
  new TypedReducer<bool, AreaUpdateFail>(_finished),
  new TypedReducer<bool, AreaDelete>(_loading),
  new TypedReducer<bool, AreaDeleteSuccessful>(_finished),
  new TypedReducer<bool, AreaDeleteFail>(_finished),
]);

final loadingNextCloudCredentialsReducer = combineReducers<bool>([
  new TypedReducer<bool, NextCloudCredentialsLogIn>(_loading),
  new TypedReducer<bool, NextCloudCredentialsLogInSuccessful>(_finished),
  new TypedReducer<bool, NextCloudCredentialsLogInFail>(_finished),
  new TypedReducer<bool, NextCloudCredentialsLogOut>(_loading),
  new TypedReducer<bool, NextCloudCredentialsLogOutSuccessful>(_finished),
  new TypedReducer<bool, NextCloudCredentialsLogOutFail>(_finished),
]);

final loadingWirelessSocketReducer = combineReducers<bool>([
  new TypedReducer<bool, WirelessSocketLoad>(_loading),
  new TypedReducer<bool, WirelessSocketLoadSuccessful>(_finished),
  new TypedReducer<bool, WirelessSocketLoadFail>(_finished),
  new TypedReducer<bool, WirelessSocketAdd>(_loading),
  new TypedReducer<bool, WirelessSocketAddSuccessful>(_finished),
  new TypedReducer<bool, WirelessSocketAddFail>(_finished),
  new TypedReducer<bool, WirelessSocketUpdate>(_loading),
  new TypedReducer<bool, WirelessSocketUpdateSuccessful>(_finished),
  new TypedReducer<bool, WirelessSocketUpdateFail>(_finished),
  new TypedReducer<bool, WirelessSocketDelete>(_loading),
  new TypedReducer<bool, WirelessSocketDeleteSuccessful>(_finished),
  new TypedReducer<bool, WirelessSocketDeleteFail>(_finished),
]);
