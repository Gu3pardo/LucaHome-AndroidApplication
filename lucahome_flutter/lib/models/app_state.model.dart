import 'package:lucahome_flutter/models/area.model.dart';
import 'package:lucahome_flutter/models/next_cloud_credentials.model.dart';
import 'package:lucahome_flutter/models/wireless_socket.model.dart';

class AppState {
  NextCloudCredentials nextCloudCredentials;
  bool isLoadingNextCloudCredentials;

  List<Area> areaList;
  bool isLoadingArea;
  Area selectedArea;

  List<WirelessSocket> wirelessSocketList;
  bool isLoadingWirelessSocket;
  WirelessSocket selectedWirelessSocket;

  String currentRoute;

  AppState(
      {this.nextCloudCredentials,
      this.isLoadingNextCloudCredentials = false,
      this.areaList,
      this.isLoadingArea = false,
      this.selectedArea,
      this.wirelessSocketList,
      this.isLoadingWirelessSocket = false,
      this.selectedWirelessSocket,
      this.currentRoute = "/"});

  AppState copyWith(
      {NextCloudCredentials nextCloudCredentials,
      bool isLoadingNextCloudCredentials,
      List<Area> areaList,
      bool isLoadingArea,
      Area selectedArea,
      List<WirelessSocket> wirelessSocketList,
      bool isLoadingWirelessSocket,
      WirelessSocket selectedWirelessSocket,
      String currentRoute}) {
    return new AppState(
      nextCloudCredentials: nextCloudCredentials ?? this.nextCloudCredentials,
      isLoadingNextCloudCredentials: isLoadingNextCloudCredentials ?? this.isLoadingNextCloudCredentials,
      areaList: areaList ?? this.areaList,
      isLoadingArea: isLoadingArea ?? this.isLoadingArea,
      selectedArea: selectedArea ?? this.selectedArea,
      wirelessSocketList: wirelessSocketList ?? this.wirelessSocketList,
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
          wirelessSocketList == other.wirelessSocketList &&
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
      wirelessSocketList.hashCode ^
      isLoadingWirelessSocket.hashCode ^
      selectedWirelessSocket.hashCode ^
      currentRoute.hashCode;

  @override
  String toString() {
    return 'AppState{nextCloudCredentials: $nextCloudCredentials, isLoadingNextCloudCredentials: $isLoadingNextCloudCredentials, areaList: $areaList, isLoadingArea: $isLoadingArea, selectedArea: $selectedArea, wirelessSocketList: $wirelessSocketList, isLoadingWirelessSocket: $isLoadingWirelessSocket, selectedWirelessSocket: $selectedWirelessSocket, currentRoute: $currentRoute}';
  }
}
