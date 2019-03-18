import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/reducers/area.reducer.dart';
import 'package:wireless_control/reducers/loading.reducer.dart';
import 'package:wireless_control/reducers/next_cloud_credentials.reducer.dart';
import 'package:wireless_control/reducers/periodic_task.reducer.dart';
import 'package:wireless_control/reducers/route.reducer.dart';
import 'package:wireless_control/reducers/theme.reducer.dart';
import 'package:wireless_control/reducers/wireless_socket.reducer.dart';

AppState appReducer(state, action) {
  return new AppState(
      nextCloudCredentials: nextCloudCredentialsReducer(state.nextCloudCredentials, action),
      isLoadingNextCloudCredentials: loadingNextCloudCredentialsReducer(state.isLoadingNextCloudCredentials, action),

      areaList: areaListReducer(state.areaList, action),
      isLoadingArea: loadingAreaReducer(state.isLoadingArea, action),
      selectedArea: areaSelectReducer(state.selectedArea, action),
      toBeAddedArea: areaAddReducer(state.toBeAddedArea, action),

      wirelessSocketListAll: wirelessSocketListAllReducer(state.wirelessSocketListAll, action),
      wirelessSocketListArea: wirelessSocketSelectAreaReducer(state.wirelessSocketListAll, action),
      isLoadingWirelessSocket: loadingWirelessSocketReducer(state.isLoadingWirelessSocket, action),
      selectedWirelessSocket: wirelessSocketSelectReducer(state.selectedWirelessSocket, action),
      toBeAddedWirelessSocket: wirelessSocketAddReducer(state.toBeAddedWirelessSocket, action),

      periodicTaskList: periodicTaskListReducer(state.periodicTaskList, action),
      isLoadingPeriodicTask: loadingPeriodicTaskReducer(state.isLoadingPeriodicTask, action),
      selectedPeriodicTask: periodicTaskSelectReducer(state.selectedPeriodicTask, action),
      toBeAddedPeriodicTask: periodicTaskAddReducer(state.toBeAddedPeriodicTask, action),

      currentRoute: routeReducer(state.currentRoute, action),
      theme: themeReducer(state.theme, action)
  ); //new
}
