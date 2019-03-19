import 'package:wireless_control/enums/app_theme.enum.dart';
import 'package:wireless_control/models/area.model.dart';
import 'package:wireless_control/models/next_cloud_credentials.model.dart';
import 'package:wireless_control/models/periodic_task.model.dart';
import 'package:wireless_control/models/wireless_socket.model.dart';

class AppState {
  NextCloudCredentials nextCloudCredentials;
  bool isLoadingNextCloudCredentials;

  List<Area> areaList;
  bool isLoadingArea;
  Area selectedArea;
  Area toBeAddedArea;

  List<WirelessSocket> wirelessSocketListAll;
  List<WirelessSocket> wirelessSocketListArea;
  bool isLoadingWirelessSocket;
  WirelessSocket selectedWirelessSocket;
  WirelessSocket toBeAddedWirelessSocket;

  List<PeriodicTask> periodicTaskList;
  List<PeriodicTask> periodicTaskListWirelessSocket;
  bool isLoadingPeriodicTask;
  PeriodicTask selectedPeriodicTask;
  PeriodicTask toBeAddedPeriodicTask;

  String currentRoute;

  AppTheme theme;

  AppState({
    this.nextCloudCredentials,
    this.isLoadingNextCloudCredentials = false,

    this.areaList,
    this.isLoadingArea = false,
    this.selectedArea,
    this.toBeAddedArea,

    this.wirelessSocketListAll,
    this.wirelessSocketListArea,
    this.isLoadingWirelessSocket = false,
    this.selectedWirelessSocket,
    this.toBeAddedWirelessSocket,

    this.periodicTaskList,
    this.periodicTaskListWirelessSocket,
    this.isLoadingPeriodicTask = false,
    this.selectedPeriodicTask,
    this.toBeAddedPeriodicTask,

    this.currentRoute = "/",
    this.theme = AppTheme.Light});

  AppState copyWith({
    NextCloudCredentials nextCloudCredentials,
    bool isLoadingNextCloudCredentials,

    List<Area> areaList,
    bool isLoadingArea,
    Area selectedArea,
    Area toBeAddedArea,

    List<WirelessSocket> wirelessSocketListAll,
    List<WirelessSocket> wirelessSocketListArea,
    bool isLoadingWirelessSocket,
    WirelessSocket selectedWirelessSocket,
    WirelessSocket toBeAddedWirelessSocket,

    List<PeriodicTask> periodicTaskList,
    List<PeriodicTask> periodicTaskListWirelessSocket,
    bool isLoadingPeriodicTask,
    PeriodicTask selectedPeriodicTask,
    PeriodicTask toBeAddedPeriodicTask,

    String currentRoute,
    AppTheme theme}) {
    return new AppState(
      nextCloudCredentials: nextCloudCredentials ?? this.nextCloudCredentials,
      isLoadingNextCloudCredentials: isLoadingNextCloudCredentials ?? this.isLoadingNextCloudCredentials,
      areaList: areaList ?? this.areaList,
      isLoadingArea: isLoadingArea ?? this.isLoadingArea,
      selectedArea: selectedArea ?? this.selectedArea,
      toBeAddedArea: toBeAddedArea ?? this.toBeAddedArea,
      wirelessSocketListAll: wirelessSocketListAll ?? this.wirelessSocketListAll,
      wirelessSocketListArea: wirelessSocketListArea ?? this.wirelessSocketListArea,
      isLoadingWirelessSocket: isLoadingWirelessSocket ?? this.isLoadingWirelessSocket,
      selectedWirelessSocket: selectedWirelessSocket ?? this.selectedWirelessSocket,
      toBeAddedWirelessSocket: toBeAddedWirelessSocket ?? this.toBeAddedWirelessSocket,
      periodicTaskList: periodicTaskList ?? this.periodicTaskList,
      periodicTaskListWirelessSocket: periodicTaskListWirelessSocket ?? this.periodicTaskListWirelessSocket,
      isLoadingPeriodicTask: isLoadingPeriodicTask ?? this.isLoadingPeriodicTask,
      selectedPeriodicTask: selectedPeriodicTask ?? this.selectedPeriodicTask,
      toBeAddedPeriodicTask: toBeAddedPeriodicTask ?? this.toBeAddedPeriodicTask,
      currentRoute: currentRoute ?? this.currentRoute,
      theme: theme ?? this.theme,
    );
  }

  bool operator ==(Object other) =>
      identical(this, other) ||
      other is AppState &&
          runtimeType == other.runtimeType &&
          nextCloudCredentials == other.nextCloudCredentials &&
          isLoadingNextCloudCredentials == other.isLoadingNextCloudCredentials &&
          areaList == other.areaList &&
          isLoadingArea == other.isLoadingArea &&
          selectedArea == other.selectedArea &&
          toBeAddedArea == other.toBeAddedArea &&
          wirelessSocketListAll == other.wirelessSocketListAll &&
          wirelessSocketListArea == other.wirelessSocketListArea &&
          isLoadingWirelessSocket == other.isLoadingWirelessSocket &&
          selectedWirelessSocket == other.selectedWirelessSocket &&
          toBeAddedWirelessSocket == other.toBeAddedWirelessSocket &&
          periodicTaskList == other.periodicTaskList &&
          periodicTaskListWirelessSocket == other.periodicTaskListWirelessSocket &&
          isLoadingPeriodicTask == other.isLoadingPeriodicTask &&
          selectedPeriodicTask == other.selectedPeriodicTask &&
          toBeAddedPeriodicTask == other.toBeAddedPeriodicTask &&
          currentRoute == other.currentRoute &&
          theme == other.theme;

  @override
  int get hashCode =>
      nextCloudCredentials.hashCode ^
      isLoadingNextCloudCredentials.hashCode ^
      areaList.hashCode ^
      isLoadingArea.hashCode ^
      selectedArea.hashCode ^
      toBeAddedArea.hashCode ^
      wirelessSocketListAll.hashCode ^
      wirelessSocketListArea.hashCode ^
      isLoadingWirelessSocket.hashCode ^
      selectedWirelessSocket.hashCode ^
      toBeAddedWirelessSocket.hashCode ^
      periodicTaskList.hashCode ^
      periodicTaskListWirelessSocket.hashCode ^
      isLoadingPeriodicTask.hashCode ^
      selectedPeriodicTask.hashCode ^
      toBeAddedPeriodicTask.hashCode ^
      currentRoute.hashCode ^
      theme.hashCode;

  @override
  String toString() {
    return 'AppState{nextCloudCredentials: $nextCloudCredentials, isLoadingNextCloudCredentials: $isLoadingNextCloudCredentials, areaList: $areaList, isLoadingArea: $isLoadingArea, selectedArea: $selectedArea, toBeAddedArea: $toBeAddedArea, wirelessSocketListAll: $wirelessSocketListAll, wirelessSocketListArea: $wirelessSocketListArea, isLoadingWirelessSocket: $isLoadingWirelessSocket, selectedWirelessSocket: $selectedWirelessSocket, toBeAddedWirelessSocket: $toBeAddedWirelessSocket, periodicTaskList: $periodicTaskList, periodicTaskListWirelessSocket: $periodicTaskListWirelessSocket, isLoadingPeriodicTask: $isLoadingPeriodicTask, selectedPeriodicTask: $selectedPeriodicTask, toBeAddedPeriodicTask: $toBeAddedPeriodicTask, currentRoute: $currentRoute, theme: $theme}';
  }
}
