import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/reducers/area.reducer.dart';
import 'package:lucahome_flutter/reducers/nextcloud_credentials.reducer.dart';
import 'package:lucahome_flutter/reducers/wireless_socket.reducer.dart';

AppState appReducer(state, action) {
  return new AppState(
    isLoading: false,
    nextCloudCredentials: nextCloudCredentialsReducer(state.nextCloudCredentials, action),
    areaList: areaReducer(state.areaList, action),
    wirelessSocketList: wirelessSocketReducer(state.wirelessSocketList, action),
  ); //new
}
