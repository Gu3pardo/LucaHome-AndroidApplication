import 'package:wireless_control/actions/area.actions.dart';
import 'package:wireless_control/actions/wireless_socket.actions.dart';
import 'package:wireless_control/models/wireless_socket.model.dart';
import 'package:redux/redux.dart';

final wirelessSocketListAllReducer = combineReducers<List<WirelessSocket>>([
  new TypedReducer<List<WirelessSocket>, WirelessSocketLoadSuccessful>(_loadSuccessful),
  new TypedReducer<List<WirelessSocket>, WirelessSocketLoadFail>(_loadFailed),
  new TypedReducer<List<WirelessSocket>, WirelessSocketAddSuccessful>(_addSuccessful),
  new TypedReducer<List<WirelessSocket>, WirelessSocketAddFail>(_addFailed),
  new TypedReducer<List<WirelessSocket>, WirelessSocketUpdateSuccessful>(_updateSuccessful),
  new TypedReducer<List<WirelessSocket>, WirelessSocketUpdateFail>(_updateFailed),
  new TypedReducer<List<WirelessSocket>, WirelessSocketDeleteSuccessful>(_deleteSuccessful),
  new TypedReducer<List<WirelessSocket>, WirelessSocketDeleteFail>(_deleteFailed),
]);

List<WirelessSocket> _loadSuccessful(List<WirelessSocket> wirelessSocketList, action) => List.unmodifiable(List.from(action.list));
List<WirelessSocket> _loadFailed(List<WirelessSocket> wirelessSocketList, action) => wirelessSocketList;

List<WirelessSocket> _addSuccessful(List<WirelessSocket> wirelessSocketList, action) => List.unmodifiable(List.from(wirelessSocketList)..add(action.wirelessSocket));
List<WirelessSocket> _addFailed(List<WirelessSocket> wirelessSocketList, action) => wirelessSocketList;

List<WirelessSocket> _updateSuccessful(List<WirelessSocket> wirelessSocketList, action) {
  var modifiableList = List.from(wirelessSocketList);
  var index = modifiableList.indexWhere((wirelessSocket) => wirelessSocket.name == action.wirelessSocket.name);
  modifiableList.removeAt(index);
  modifiableList.insert(index, action.wirelessSocket);
  return List.unmodifiable(modifiableList);
}
List<WirelessSocket> _updateFailed(List<WirelessSocket> wirelessSocketList, action) => wirelessSocketList;

List<WirelessSocket> _deleteSuccessful(List<WirelessSocket> wirelessSocketList, action) => List.unmodifiable(List.from(wirelessSocketList)..remove(action.wirelessSocket));
List<WirelessSocket> _deleteFailed(List<WirelessSocket> wirelessSocketList, action) => wirelessSocketList;

final wirelessSocketSelectReducer = combineReducers<WirelessSocket>([
  new TypedReducer<WirelessSocket, WirelessSocketSelectSuccessful>(_selectSuccessful),
  new TypedReducer<WirelessSocket, WirelessSocketSelectFail>(_selectFailed),
]);

WirelessSocket _selectSuccessful(WirelessSocket wirelessSocket, action) => action.wirelessSocket;
WirelessSocket _selectFailed(WirelessSocket wirelessSocket, action) => wirelessSocket;

final wirelessSocketSelectAreaReducer = combineReducers<List<WirelessSocket>>([
  new TypedReducer<List<WirelessSocket>, AreaSelectSuccessful>(_selectAreaSuccessful),
  new TypedReducer<List<WirelessSocket>, AreaSelect>(_selectAreaFailed),
]);

List<WirelessSocket> _selectAreaSuccessful(List<WirelessSocket> wirelessSocketList, action) =>
    action.area.filter == '' ? wirelessSocketList : wirelessSocketList.where((WirelessSocket wirelessSocket) => wirelessSocket.area == action.area.filter).toList();
List<WirelessSocket> _selectAreaFailed(List<WirelessSocket> wirelessSocketList, action) => wirelessSocketList;

final wirelessSocketAddReducer = combineReducers<WirelessSocket>([
  new TypedReducer<WirelessSocket, WirelessSocketAdd>(_addWirelessSocket),
  new TypedReducer<WirelessSocket, WirelessSocketAddSuccessful>(_addWirelessSocketSuccessful),
  new TypedReducer<WirelessSocket, WirelessSocketAddFail>(_addWirelessSocketFailed),
]);

WirelessSocket _addWirelessSocket(WirelessSocket wirelessSocket, action) => action.wirelessSocket;
WirelessSocket _addWirelessSocketSuccessful(WirelessSocket wirelessSocket, action) => null;
WirelessSocket _addWirelessSocketFailed(WirelessSocket wirelessSocket, action) => null;
