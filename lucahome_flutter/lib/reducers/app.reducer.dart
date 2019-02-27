import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/reducers/area.reducer.dart';
import 'package:lucahome_flutter/reducers/next_cloud_credentials.reducer.dart';
import 'package:lucahome_flutter/reducers/wireless_socket.reducer.dart';

AppState appReducer(state, action) {
  return new AppState(
    isLoading: false,
    nextCloudCredentials: nextCloudCredentialsReducer(state.nextCloudCredentials, action),
    areaList: areaListReducer(state.areaList, action),
    selectedArea: areaReducer(state.selectedArea, action),
    wirelessSocketList: wirelessSocketListReducer(state.wirelessSocketList, action),
    selectedWirelessSocket: wirelessSocketReducer(state.selectedWirelessSocket, action),
  ); //new
}
