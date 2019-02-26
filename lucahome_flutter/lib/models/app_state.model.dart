import 'package:lucahome_flutter/models/area.model.dart';
import 'package:lucahome_flutter/models/next_cloud_credentials.model.dart';
import 'package:lucahome_flutter/models/wireless_socket.model.dart';

class AppState {
  bool isLoading;
  NextCloudCredentials nextCloudCredentials;
  List<Area> areaList;
  List<WirelessSocket> wirelessSocketList;

  AppState({
    this.isLoading = false,
    this.nextCloudCredentials,
    this.areaList,
    this.wirelessSocketList,
  });

  factory AppState.loading() => new AppState(isLoading: true);

  AppState copyWith(
      {bool isLoading,
      NextCloudCredentials nextCloudCredentials,
      List<Area> areaList,
      List<WirelessSocket> wirelessSocketList}) {
    return new AppState(
      isLoading: isLoading ?? this.isLoading,
      nextCloudCredentials: nextCloudCredentials ?? this.nextCloudCredentials,
      areaList: areaList ?? this.areaList,
      wirelessSocketList: wirelessSocketList ?? this.wirelessSocketList,
    );
  }

  bool operator ==(Object other) =>
      identical(this, other) ||
      other is AppState &&
          runtimeType == other.runtimeType &&
          isLoading == other.isLoading &&
          nextCloudCredentials == other.nextCloudCredentials &&
          areaList == other.areaList &&
          wirelessSocketList == other.wirelessSocketList;

  @override
  int get hashCode =>
      isLoading.hashCode ^
      nextCloudCredentials.hashCode ^
      areaList.hashCode ^
      wirelessSocketList.hashCode;

  @override
  String toString() {
    return 'AppState{isLoading: $isLoading, nextCloudCredentials: $nextCloudCredentials, areaList: $areaList, wirelessSocketList: $wirelessSocketList}';
  }
}
