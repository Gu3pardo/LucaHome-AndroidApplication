import 'package:wireless_control/models/area.model.dart';
import 'package:wireless_control/models/next_cloud_credentials.model.dart';
import 'package:wireless_control/models/wireless_socket.model.dart';

class AppState {
  NextCloudCredentials nextCloudCredentials;
  bool isLoadingNextCloudCredentials;

  List<Area> areaList;
  bool isLoadingArea;
  Area selectedArea;

  List<WirelessSocket> wirelessSocketListAll;
  List<WirelessSocket> wirelessSocketListArea;
  bool isLoadingWirelessSocket;
  WirelessSocket selectedWirelessSocket;

  String currentRoute;

  AppState(
      {this.nextCloudCredentials,
      this.isLoadingNextCloudCredentials = false,
      this.areaList,
      this.isLoadingArea = false,
      this.selectedArea,
      this.wirelessSocketListAll,
      this.wirelessSocketListArea,
      this.isLoadingWirelessSocket = false,
      this.selectedWirelessSocket,
      this.currentRoute = "/"});

  AppState copyWith(
      {NextCloudCredentials nextCloudCredentials,
      bool isLoadingNextCloudCredentials,
      List<Area> areaList,
      bool isLoadingArea,
      Area selectedArea,
      List<WirelessSocket> wirelessSocketListAll,
      List<WirelessSocket> wirelessSocketListArea,
      bool isLoadingWirelessSocket,
      WirelessSocket selectedWirelessSocket,
      String currentRoute}) {
    return new AppState(
      nextCloudCredentials: nextCloudCredentials ?? this.nextCloudCredentials,
      isLoadingNextCloudCredentials: isLoadingNextCloudCredentials ?? this.isLoadingNextCloudCredentials,
      areaList: areaList ?? this.areaList,
      isLoadingArea: isLoadingArea ?? this.isLoadingArea,
      selectedArea: selectedArea ?? this.selectedArea,
      wirelessSocketListAll: wirelessSocketListAll ?? this.wirelessSocketListAll,
      wirelessSocketListArea: wirelessSocketListArea ?? this.wirelessSocketListArea,
      isLoadingWirelessSocket: isLoadingWirelessSocket ?? this.isLoadingWirelessSocket,
      selectedWirelessSocket: selectedWirelessSocket ?? this.selectedWirelessSocket,
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
          wirelessSocketListAll == other.wirelessSocketListAll &&
          wirelessSocketListArea == other.wirelessSocketListArea &&
          isLoadingWirelessSocket == other.isLoadingWirelessSocket &&
          selectedWirelessSocket == other.selectedWirelessSocket &&
          currentRoute == other.currentRoute;

  @override
  int get hashCode =>
      nextCloudCredentials.hashCode ^
      isLoadingNextCloudCredentials.hashCode ^
      areaList.hashCode ^
      isLoadingArea.hashCode ^
      selectedArea.hashCode ^
      wirelessSocketListAll.hashCode ^
      wirelessSocketListArea.hashCode ^
      isLoadingWirelessSocket.hashCode ^
      selectedWirelessSocket.hashCode ^
      currentRoute.hashCode;

  @override
  String toString() {
    return 'AppState{nextCloudCredentials: $nextCloudCredentials, isLoadingNextCloudCredentials: $isLoadingNextCloudCredentials, areaList: $areaList, isLoadingArea: $isLoadingArea, selectedArea: $selectedArea, wirelessSocketListAll: $wirelessSocketListAll, wirelessSocketListArea: $wirelessSocketListArea, isLoadingWirelessSocket: $isLoadingWirelessSocket, selectedWirelessSocket: $selectedWirelessSocket, currentRoute: $currentRoute}';
  }
}
