import 'dart:convert' as convert;
import 'package:http/http.dart' as http;
import 'package:lucahome_flutter/actions/wireless_socket.actions.dart';
import 'package:lucahome_flutter/constants/nextcloud.constants.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:redux/redux.dart';
import 'package:redux_thunk/redux_thunk.dart';

ThunkAction<AppState> loadWirelessSockets = (Store<AppState> store) async {
  var response = await http.get(NextCloudConstants.baseUrl + "wireless_socket");
  if (response.statusCode == 200) {
    try {
      var jsonResponse = convert.jsonDecode(response.body);
      var data = jsonResponse.data;

      if (data == false) {
        store.dispatch(new WirelessSocketLoadFail(jsonResponse.message));
      } else {
        store.dispatch(new WirelessSocketLoadSuccessful(list: data));
      }
    } catch (exception) {
      store.dispatch(new WirelessSocketLoadFail(exception));
    }
  } else {
    store.dispatch(new WirelessSocketLoadFail(response.statusCode));
  }
};

ThunkAction<AppState> addWirelessSocket = (Store<AppState> store) async {
  var wirelessSocket = store.state.selectedWirelessSocket;
  var response = await http.post(NextCloudConstants.baseUrl + "wireless_socket",
      body: convert.jsonEncode(wirelessSocket));
  if (response.statusCode == 200) {
    try {
      var jsonResponse = convert.jsonDecode(response.body);
      var data = jsonResponse.data;

      if (data == false) {
        store.dispatch(new WirelessSocketAddFail(jsonResponse.message));
      } else {
        if (data >= 0) {
          wirelessSocket.id = data;
          store.dispatch(
              new WirelessSocketAddSuccessful(wirelessSocket: wirelessSocket));
        } else {
          store.dispatch(new WirelessSocketAddFail(data));
        }
      }
    } catch (exception) {
      store.dispatch(new WirelessSocketAddFail(exception));
    }
  } else {
    store.dispatch(new WirelessSocketAddFail(response.statusCode));
  }
};

ThunkAction<AppState> updateWirelessSocket = (Store<AppState> store) async {
  var wirelessSocket = store.state.selectedWirelessSocket;
  var response = await http.put(NextCloudConstants.baseUrl + "wireless_socket",
      body: convert.jsonEncode(wirelessSocket));
  if (response.statusCode == 200) {
    try {
      var jsonResponse = convert.jsonDecode(response.body);
      var data = jsonResponse.data;

      if (data == false) {
        store.dispatch(new WirelessSocketUpdateFail(jsonResponse.message));
      } else {
        if (data == 0) {
          store.dispatch(new WirelessSocketUpdateSuccessful(
              wirelessSocket: wirelessSocket));
        } else {
          store.dispatch(new WirelessSocketUpdateFail(data));
        }
      }
    } catch (exception) {
      store.dispatch(new WirelessSocketUpdateFail(exception));
    }
  } else {
    store.dispatch(new WirelessSocketUpdateFail(response.statusCode));
  }
};

ThunkAction<AppState> deleteWirelessSocket = (Store<AppState> store) async {
  var wirelessSocket = store.state.selectedWirelessSocket;
  var response = await http.delete(
      NextCloudConstants.baseUrl + "wireless_socket/$wirelessSocket.id");
  if (response.statusCode == 200) {
    try {
      var jsonResponse = convert.jsonDecode(response.body);
      var data = jsonResponse.data;

      if (data == false) {
        store.dispatch(new WirelessSocketDeleteFail(jsonResponse.message));
      } else {
        if (data == 0) {
          store.dispatch(new WirelessSocketDeleteSuccessful(
              wirelessSocket: wirelessSocket));
        } else {
          store.dispatch(new WirelessSocketDeleteFail(data));
        }
      }
    } catch (exception) {
      store.dispatch(new WirelessSocketDeleteFail(exception));
    }
  } else {
    store.dispatch(new WirelessSocketDeleteFail(response.statusCode));
  }
};
