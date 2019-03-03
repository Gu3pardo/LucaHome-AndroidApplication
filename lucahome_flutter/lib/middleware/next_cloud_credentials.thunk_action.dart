import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:lucahome_flutter/actions/next_cloud_credentials.actions.dart';
import 'package:lucahome_flutter/constants/nextcloud.constants.dart';
import 'package:lucahome_flutter/middleware/area.thunk_action.dart';
import 'package:lucahome_flutter/middleware//wireless_socket.thunk_action.dart';
import 'package:lucahome_flutter/models/api_response.model.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/models/next_cloud_credentials.model.dart';
import 'package:lucahome_flutter/utils/shared_pref.utils.dart';
import 'package:redux/redux.dart';
import 'package:redux_thunk/redux_thunk.dart';

ThunkAction<AppState> logIn(NextCloudCredentials nextCloudCredentials) {
  return (Store<AppState> store) async {
    store.dispatch(new NextCloudCredentialsLogIn());

    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.get(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + 'area',
        headers: {'authorization': authorization});

    switch (response.statusCode) {
      // 404 For invalid URL
      case 404:
        saveNextCloudCredentials(NextCloudCredentials(baseUrl:"", userName:"", passPhrase:""));
        store.dispatch(new NextCloudCredentialsLogInFail("Invalid URL"));
        break;

      // 401 For invalid userName with message: CORS requires basic auth
      // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        saveNextCloudCredentials(NextCloudCredentials(baseUrl:nextCloudCredentials.baseUrl, userName:"", passPhrase:""));
        store.dispatch(new NextCloudCredentialsLogInFail("Invalid Credentials"));
        break;

      // Valid
      case 200:
        saveNextCloudCredentials(nextCloudCredentials);
        var apiResponseModel = ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success") {
          store.dispatch(loadAreas(nextCloudCredentials));
          store.dispatch(loadWirelessSockets(nextCloudCredentials));
          store.dispatch(new NextCloudCredentialsLogInSuccessful(user: nextCloudCredentials));
        } else {
          store.dispatch(new NextCloudCredentialsLogInFail(apiResponseModel.message));
        }
        break;

      default:
        store.dispatch(new NextCloudCredentialsLogInFail("Unknown error"));
        break;
    }
  };
}
