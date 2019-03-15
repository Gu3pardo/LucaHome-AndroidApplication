import 'package:wireless_control/models/area.model.dart';
import 'package:wireless_control/models/next_cloud_credentials.model.dart';
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

  String currentRoute;

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

    this.currentRoute = "/"});

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

    String currentRoute}) {
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
      currentRoute: currentRoute ?? this.currentRoute,
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
          currentRoute == other.currentRoute;

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
      currentRoute.hashCode;

  @override
  String toString() {
    return 'AppState{nextCloudCredentials: $nextCloudCredentials, isLoadingNextCloudCredentials: $isLoadingNextCloudCredentials, areaList: $areaList, isLoadingArea: $isLoadingArea, selectedArea: $selectedArea, toBeAddedArea: $toBeAddedArea, wirelessSocketListAll: $wirelessSocketListAll, wirelessSocketListArea: $wirelessSocketListArea, isLoadingWirelessSocket: $isLoadingWirelessSocket, selectedWirelessSocket: $selectedWirelessSocket, toBeAddedWirelessSocket: $toBeAddedWirelessSocket, currentRoute: $currentRoute}';
  }
}
