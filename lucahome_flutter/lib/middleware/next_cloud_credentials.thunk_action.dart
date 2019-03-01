import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:lucahome_flutter/actions/next_cloud_credentials.actions.dart';
import 'package:lucahome_flutter/constants/nextcloud.constants.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/models/next_cloud_credentials.model.dart';
import 'package:redux/redux.dart';
import 'package:redux_thunk/redux_thunk.dart';

ThunkAction<AppState> logIn(NextCloudCredentials nextCloudCredentials) {
  return (Store<AppState> store) async {
    var authorization = 'Basic ' + base64Encode(utf8.encode('${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.get(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + 'area',
        headers: {'authorization': authorization});

    if (response.statusCode == 200 && jsonDecode(response.body)["status"] == "success") {
      store.dispatch(new NextCloudCredentialsLogInSuccessful(user: nextCloudCredentials));
    } else {
      store.dispatch(new NextCloudCredentialsLogInFail(response.statusCode));
    }
  };
}
