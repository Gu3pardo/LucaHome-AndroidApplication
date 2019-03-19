import 'package:wireless_control/actions/periodic_task.actions.dart';
import 'package:wireless_control/actions/wireless_socket.actions.dart';
import 'package:wireless_control/models/periodic_task.model.dart';
import 'package:redux/redux.dart';

final periodicTaskListReducer = combineReducers<List<PeriodicTask>>([
  new TypedReducer<List<PeriodicTask>, PeriodicTaskLoadSuccessful>(_loadSuccessful),
  new TypedReducer<List<PeriodicTask>, PeriodicTaskLoadFail>(_loadFailed),
  new TypedReducer<List<PeriodicTask>, PeriodicTaskAddSuccessful>(_addSuccessful),
  new TypedReducer<List<PeriodicTask>, PeriodicTaskAddFail>(_addFailed),
  new TypedReducer<List<PeriodicTask>, PeriodicTaskUpdateSuccessful>(_updateSuccessful),
  new TypedReducer<List<PeriodicTask>, PeriodicTaskUpdateFail>(_updateFailed),
  new TypedReducer<List<PeriodicTask>, PeriodicTaskDeleteSuccessful>(_deleteSuccessful),
  new TypedReducer<List<PeriodicTask>, PeriodicTaskDeleteFail>(_deleteFailed),
]);

List<PeriodicTask> _loadSuccessful(List<PeriodicTask> periodicTaskList, action) => List.unmodifiable(List.from(action.list));
List<PeriodicTask> _loadFailed(List<PeriodicTask> periodicTaskList, action) => periodicTaskList;

List<PeriodicTask> _addSuccessful(List<PeriodicTask> periodicTaskList, action) => List.unmodifiable(List.from(periodicTaskList)..add(action.periodicTask));
List<PeriodicTask> _addFailed(List<PeriodicTask> periodicTaskList, action) => periodicTaskList;

List<PeriodicTask> _updateSuccessful(List<PeriodicTask> periodicTaskList, action) {
  var modifiableList = List.from(periodicTaskList);
  var index = modifiableList.indexWhere((periodicTask) => periodicTask.name == action.periodicTask.name);
  modifiableList.removeAt(index);
  modifiableList.insert(index, action.periodicTask);
  return List.unmodifiable(modifiableList);
}
List<PeriodicTask> _updateFailed(List<PeriodicTask> periodicTaskList, action) => periodicTaskList;

List<PeriodicTask> _deleteSuccessful(List<PeriodicTask> periodicTaskList, action) => List.unmodifiable(List.from(periodicTaskList)..remove(action.periodicTask));
List<PeriodicTask> _deleteFailed(List<PeriodicTask> periodicTaskList, action) => periodicTaskList;

final periodicTaskSelectReducer = combineReducers<PeriodicTask>([
  new TypedReducer<PeriodicTask, PeriodicTaskSelectSuccessful>(_selectSuccessful),
  new TypedReducer<PeriodicTask, PeriodicTaskSelectFail>(_selectFailed),
]);

PeriodicTask _selectSuccessful(PeriodicTask periodicTask, action) => action.periodicTask;
PeriodicTask _selectFailed(PeriodicTask periodicTask, action) => periodicTask;

final periodicTaskSelectWirelessSocketReducer = combineReducers<List<PeriodicTask>>([
  new TypedReducer<List<PeriodicTask>, WirelessSocketSelectSuccessful>(_selectWirelessSocketSuccessful),
  new TypedReducer<List<PeriodicTask>, WirelessSocketSelectFail>(_selectWirelessSocketFailed),
]);

List<PeriodicTask> _selectWirelessSocketSuccessful(List<PeriodicTask> periodicTaskList, action) {
  return action.wirelessSocket != null
      ? periodicTaskList.where((PeriodicTask periodicTask) => periodicTask.wirelessSocketId == action.wirelessSocket.id).toList()
      : periodicTaskList;
}
List<PeriodicTask> _selectWirelessSocketFailed(List<PeriodicTask> periodicTaskList, action) => periodicTaskList;

final periodicTaskAddReducer = combineReducers<PeriodicTask>([
  new TypedReducer<PeriodicTask, PeriodicTaskAdd>(_addPeriodicTask),
  new TypedReducer<PeriodicTask, PeriodicTaskAddSuccessful>(_addPeriodicTaskSuccessful),
  new TypedReducer<PeriodicTask, PeriodicTaskAddFail>(_addPeriodicTaskFailed),
]);

PeriodicTask _addPeriodicTask(PeriodicTask periodicTask, action) => action.periodicTask;
PeriodicTask _addPeriodicTaskSuccessful(PeriodicTask periodicTask, action) => null;
PeriodicTask _addPeriodicTaskFailed(PeriodicTask periodicTask, action) => null;
