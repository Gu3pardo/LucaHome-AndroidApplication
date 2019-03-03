import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/reducers/area.reducer.dart';
import 'package:lucahome_flutter/reducers/loading.reducer.dart';
import 'package:lucahome_flutter/reducers/next_cloud_credentials.reducer.dart';
import 'package:lucahome_flutter/reducers/wireless_socket.reducer.dart';

AppState appReducer(state, action) {
  return new AppState(
    nextCloudCredentials: nextCloudCredentialsReducer(state.nextCloudCredentials, action),
    isLoadingNextCloudCredentials: loadingNextCloudCredentialsReducer(state.isLoadingNextCloudCredentials, action),
    areaList: areaListReducer(state.areaList, action),
    isLoadingArea: loadingAreaReducer(state.isLoadingArea, action),
    selectedArea: areaReducer(state.selectedArea, action),
    wirelessSocketList: wirelessSocketListReducer(state.wirelessSocketList, action),
    isLoadingWirelessSocket: loadingWirelessSocketReducer(state.isLoadingWirelessSocket, action),
    selectedWirelessSocket: wirelessSocketReducer(state.selectedWirelessSocket, action),
  ); //new
}
