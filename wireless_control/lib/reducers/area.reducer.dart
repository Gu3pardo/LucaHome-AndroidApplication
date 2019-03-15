import 'package:wireless_control/actions/area.actions.dart';
import 'package:wireless_control/models/area.model.dart';
import 'package:redux/redux.dart';

final areaListReducer = combineReducers<List<Area>>([
  new TypedReducer<List<Area>, AreaLoadSuccessful>(_loadSuccessful),
  new TypedReducer<List<Area>, AreaLoadFail>(_loadFailed),
  new TypedReducer<List<Area>, AreaAddSuccessful>(_addSuccessful),
  new TypedReducer<List<Area>, AreaAddFail>(_addFailed),
  new TypedReducer<List<Area>, AreaUpdateSuccessful>(_updateSuccessful),
  new TypedReducer<List<Area>, AreaUpdateFail>(_updateFailed),
  new TypedReducer<List<Area>, AreaDeleteSuccessful>(_deleteSuccessful),
  new TypedReducer<List<Area>, AreaDeleteFail>(_deleteFailed),
]);

List<Area> _loadSuccessful(List<Area> areaList, action) => List.unmodifiable(List.from(action.list));
List<Area> _loadFailed(List<Area> areaList, action) => areaList;

List<Area> _addSuccessful(List<Area> areaList, action) => List.unmodifiable(List.from(areaList)..add(action.area));
List<Area> _addFailed(List<Area> areaList, action) => areaList;

List<Area> _updateSuccessful(List<Area> areaList, action) {
  var modifiableList = List.from(areaList);
  var index = modifiableList.indexWhere((area) => area.name == action.area.name);
  modifiableList.removeAt(index);
  modifiableList.insert(index, action.area);
  return List.unmodifiable(modifiableList);
}
List<Area> _updateFailed(List<Area> areaList, action) => areaList;

List<Area> _deleteSuccessful(List<Area> areaList, action) => List.unmodifiable(List.from(areaList)..remove(action.area));
List<Area> _deleteFailed(List<Area> areaList, action) => areaList;

final areaSelectReducer = combineReducers<Area>([
  new TypedReducer<Area, AreaSelectSuccessful>(_selectSuccessful),
  new TypedReducer<Area, AreaSelectFail>(_selectFailed),
]);

Area _selectSuccessful(Area area, action) => action.area;
Area _selectFailed(Area area, action) => area;

final areaAddReducer = combineReducers<Area>([
  new TypedReducer<Area, AreaAdd>(_addArea),
  new TypedReducer<Area, AreaAddSuccessful>(_addAreaSuccessful),
  new TypedReducer<Area, AreaAddFail>(_addAreaFailed),
]);

Area _addArea(Area area, action) => action.area;
Area _addAreaSuccessful(Area area, action) => null;
Area _addAreaFailed(Area area, action) => null;
