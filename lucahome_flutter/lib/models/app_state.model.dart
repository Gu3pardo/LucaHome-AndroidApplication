import 'package:lucahome_flutter/models/area.model.dart';
import 'package:lucahome_flutter/models/next_cloud_credentials.model.dart';
import 'package:lucahome_flutter/models/wireless_socket.model.dart';

class AppState {
  bool isLoading;
  NextCloudCredentials nextCloudCredentials;
  List<Area> areaList;
  Area selectedArea;
  List<WirelessSocket> wirelessSocketList;
  WirelessSocket selectedWirelessSocket;

  AppState({
    this.isLoading = false,
    this.nextCloudCredentials,
    this.areaList,
    this.selectedArea,
    this.wirelessSocketList,
    this.selectedWirelessSocket,
  });

  factory AppState.loading() => new AppState(isLoading: true);

  AppState copyWith(
      {bool isLoading,
      NextCloudCredentials nextCloudCredentials,
      List<Area> areaList,
      Area selectedArea,
      List<WirelessSocket> wirelessSocketList,
      WirelessSocket selectedWirelessSocket}) {
    return new AppState(
      isLoading: isLoading ?? this.isLoading,
      nextCloudCredentials: nextCloudCredentials ?? this.nextCloudCredentials,
      areaList: areaList ?? this.areaList,
      selectedArea: selectedArea ?? this.selectedArea,
      wirelessSocketList: wirelessSocketList ?? this.wirelessSocketList,
      selectedWirelessSocket: selectedWirelessSocket ?? this.selectedWirelessSocket,
    );
  }

  bool operator ==(Object other) =>
      identical(this, other) ||
      other is AppState &&
          runtimeType == other.runtimeType &&
          isLoading == other.isLoading &&
          nextCloudCredentials == other.nextCloudCredentials &&
          areaList == other.areaList &&
          selectedArea == other.selectedArea &&
          wirelessSocketList == other.wirelessSocketList &&
          selectedWirelessSocket == other.selectedWirelessSocket;

  @override
  int get hashCode =>
      isLoading.hashCode ^
      nextCloudCredentials.hashCode ^
      areaList.hashCode ^
      selectedArea.hashCode ^
      wirelessSocketList.hashCode ^
      selectedWirelessSocket.hashCode;

  @override
  String toString() {
    return 'AppState{isLoading: $isLoading, nextCloudCredentials: $nextCloudCredentials, areaList: $areaList, selectedArea: $selectedArea, wirelessSocketList: $wirelessSocketList, selectedWirelessSocket: $selectedWirelessSocket}';
  }
}
