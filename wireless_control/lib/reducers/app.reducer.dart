import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/reducers/area.reducer.dart';
import 'package:wireless_control/reducers/loading.reducer.dart';
import 'package:wireless_control/reducers/next_cloud_credentials.reducer.dart';
import 'package:wireless_control/reducers/route.reducer.dart';
import 'package:wireless_control/reducers/wireless_socket.reducer.dart';

AppState appReducer(state, action) {
  return new AppState(
      nextCloudCredentials: nextCloudCredentialsReducer(state.nextCloudCredentials, action),
      isLoadingNextCloudCredentials: loadingNextCloudCredentialsReducer(state.isLoadingNextCloudCredentials, action),
      areaList: areaListReducer(state.areaList, action),
      isLoadingArea: loadingAreaReducer(state.isLoadingArea, action),
      selectedArea: areaReducer(state.selectedArea, action),
      wirelessSocketListAll: wirelessSocketListAllReducer(state.wirelessSocketListAll, action),
      wirelessSocketListArea: wirelessSocketListAreaReducer(state.wirelessSocketListAll, action),
      isLoadingWirelessSocket: loadingWirelessSocketReducer(state.isLoadingWirelessSocket, action),
      selectedWirelessSocket: wirelessSocketReducer(state.selectedWirelessSocket, action),
      currentRoute: routeReducer(state.currentRoute, action)); //new
}
