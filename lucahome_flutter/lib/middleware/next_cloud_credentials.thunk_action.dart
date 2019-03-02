import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:lucahome_flutter/actions/next_cloud_credentials.actions.dart';
import 'package:lucahome_flutter/constants/nextcloud.constants.dart';
import 'package:lucahome_flutter/models/api_response.model.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/models/next_cloud_credentials.model.dart';
import 'package:redux/redux.dart';
import 'package:redux_thunk/redux_thunk.dart';

ThunkAction<AppState> logIn(NextCloudCredentials nextCloudCredentials) {
  return (Store<AppState> store) async {
    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.get(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + 'area',
        headers: {'authorization': authorization});

    switch (response.statusCode) {
      // 404 For invalid URL
      case 404:
        store.dispatch(new NextCloudCredentialsLogInFail("Invalid URL"));
        break;

      // 401 For invalid userName with message: CORS requires basic auth
      // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new NextCloudCredentialsLogInFail("Invalid Credentials"));
        break;

      // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success") {
          store.dispatch(new NextCloudCredentialsLogInSuccessful(user: nextCloudCredentials));
        } else {
          store.dispatch(new NextCloudCredentialsLogInFail(apiResponseModel.status));
        }
        break;

      default:
        store.dispatch(new NextCloudCredentialsLogInFail("Unknown error"));
        break;
    }
  };
}
