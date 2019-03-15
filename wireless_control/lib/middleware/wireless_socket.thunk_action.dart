import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:redux/redux.dart';
import 'package:redux_thunk/redux_thunk.dart';
import 'package:wireless_control/actions/wireless_socket.actions.dart';
import 'package:wireless_control/constants/nextcloud.constants.dart';
import 'package:wireless_control/converter/wireless_socket.converter.dart';
import 'package:wireless_control/models/api_response.model.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/wireless_socket.model.dart';
import 'package:wireless_control/models/next_cloud_credentials.model.dart';

ThunkAction<AppState> loadWirelessSockets(NextCloudCredentials nextCloudCredentials) {
  return (Store<AppState> store) async {
    store.dispatch(new WirelessSocketLoad());

    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.get(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + 'wireless_socket',
        headers: {'authorization': authorization});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new WirelessSocketLoadFail("Invalid URL"));
        break;

    // 405 For invalid URL
      case 405:
        store.dispatch(new WirelessSocketLoadFail("Method not allowed"));
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new WirelessSocketLoadFail("Invalid Credentials"));
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success") {
          try {
            store.dispatch(new WirelessSocketLoadSuccessful(list: createList(apiResponseModel.data)));
          } catch(exception) {
            store.dispatch(new WirelessSocketLoadFail(exception));
          }
        } else {
          store.dispatch(new WirelessSocketLoadFail(apiResponseModel.message));
        }
        break;

      default:
        store.dispatch(new WirelessSocketLoadFail("Unknown error: ${response.reasonPhrase}"));
        break;
    }
  };
}

ThunkAction<AppState> addWirelessSocket(NextCloudCredentials nextCloudCredentials, WirelessSocket wirelessSocket) {
  return (Store<AppState> store) async {
    store.dispatch(new WirelessSocketAddOnServer());

    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.post(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + "wireless_socket",
        body: jsonEncode(wirelessSocket.toAddJson()),
        headers: {'authorization': authorization, 'Accept': 'application/json', 'Content-Type': 'application/json'});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new WirelessSocketAddFail("Invalid URL"));
        break;

    // 405 For invalid URL
      case 405:
        store.dispatch(new WirelessSocketAddFail("Method not allowed"));
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new WirelessSocketAddFail("Invalid Credentials"));
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success" && apiResponseModel.data >= 0) {
          wirelessSocket.id = apiResponseModel.data;
          store.dispatch(new WirelessSocketAddSuccessful(wirelessSocket: wirelessSocket));
        } else {
          store.dispatch(new WirelessSocketAddFail(apiResponseModel.message));
        }
        break;

      default:
        store.dispatch(new WirelessSocketAddFail("Unknown error: ${response.reasonPhrase}"));
        break;
    }
  };
}

ThunkAction<AppState> updateWirelessSocket(NextCloudCredentials nextCloudCredentials, WirelessSocket wirelessSocket) {
  return (Store<AppState> store) async {
    store.dispatch(new WirelessSocketUpdate());

    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.put(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + "wireless_socket/" + wirelessSocket.id.toString(),
        body: jsonEncode(wirelessSocket),
        headers: {'authorization': authorization, 'Accept': 'application/json', 'Content-Type': 'application/json'});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new WirelessSocketUpdateFail("Invalid URL"));
        break;

    // 405 For invalid URL
      case 405:
        store.dispatch(new WirelessSocketUpdateFail("Method not allowed"));
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new WirelessSocketUpdateFail("Invalid Credentials"));
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success" && apiResponseModel.data == 0) {
          store.dispatch(new WirelessSocketUpdateSuccessful(wirelessSocket: wirelessSocket));
        } else {
          store.dispatch(new WirelessSocketUpdateFail(apiResponseModel.message));
        }
        break;

      default:
        store.dispatch(new WirelessSocketUpdateFail("Unknown error: ${response.reasonPhrase}"));
        break;
    }
  };
}

ThunkAction<AppState> deleteWirelessSocket(NextCloudCredentials nextCloudCredentials, WirelessSocket wirelessSocket) {
  return (Store<AppState> store) async {
    store.dispatch(new WirelessSocketDelete());

    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.delete(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + "wireless_socket/${wirelessSocket.id}",
        headers: {'authorization': authorization});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new WirelessSocketDeleteFail("Invalid URL"));
        break;

    // 405 For invalid URL
      case 405:
        store.dispatch(new WirelessSocketDeleteFail("Method not allowed"));
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new WirelessSocketDeleteFail("Invalid Credentials"));
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success" && apiResponseModel.data == 0) {
          store.dispatch(new WirelessSocketDeleteSuccessful(wirelessSocket: wirelessSocket));
        } else {
          store.dispatch(new WirelessSocketDeleteFail(apiResponseModel.message));
        }
        break;

      default:
        store.dispatch(new WirelessSocketDeleteFail("Unknown error: ${response.reasonPhrase}"));
        break;
    }
  };
}
