import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:redux/redux.dart';
import 'package:redux_thunk/redux_thunk.dart';
import 'package:wireless_control/actions/periodic_task.actions.dart';
import 'package:wireless_control/constants/nextcloud.constants.dart';
import 'package:wireless_control/converter/periodic_task.converter.dart';
import 'package:wireless_control/models/api_response.model.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/periodic_task.model.dart';
import 'package:wireless_control/models/next_cloud_credentials.model.dart';

ThunkAction<AppState> loadPeriodicTasks(NextCloudCredentials nextCloudCredentials) {
  return (Store<AppState> store) async {
    store.dispatch(new PeriodicTaskLoad());

    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.get(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + 'periodic_task',
        headers: {'authorization': authorization});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new PeriodicTaskLoadFail("Invalid URL"));
        break;

    // 405 For invalid URL
      case 405:
        store.dispatch(new PeriodicTaskLoadFail("Method not allowed"));
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new PeriodicTaskLoadFail("Invalid Credentials"));
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success") {
          try {
            var periodicTaskList = createList(apiResponseModel.data);
            store.dispatch(new PeriodicTaskLoadSuccessful(list: periodicTaskList));
            var periodicTaskSelected = periodicTaskList.length > 0 ? periodicTaskList.first : null;
            store.dispatch(new PeriodicTaskSelectSuccessful(periodicTask: periodicTaskSelected));
          } catch(exception) {
            store.dispatch(new PeriodicTaskLoadFail(exception));
          }
        } else {
          store.dispatch(new PeriodicTaskLoadFail(apiResponseModel.message));
        }
        break;

      default:
        store.dispatch(new PeriodicTaskLoadFail("Unknown error: ${response.reasonPhrase}"));
        break;
    }
  };
}

ThunkAction<AppState> addPeriodicTask(NextCloudCredentials nextCloudCredentials, PeriodicTask periodicTask, VoidCallback onSuccess, VoidCallback onError) {
  return (Store<AppState> store) async {
    store.dispatch(new PeriodicTaskAddOnServer());

    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.post(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + "periodic_task",
        body: jsonEncode(periodicTask.toAddJson()),
        headers: {'authorization': authorization, 'Accept': 'application/json', 'Content-Type': 'application/json'});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new PeriodicTaskAddFail("Invalid URL"));
        onError();
        break;

    // 405 For invalid URL
      case 405:
        store.dispatch(new PeriodicTaskAddFail("Method not allowed"));
        onError();
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new PeriodicTaskAddFail("Invalid Credentials"));
        onError();
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success" && apiResponseModel.data >= 0) {
          periodicTask.id = apiResponseModel.data;
          store.dispatch(new PeriodicTaskAddSuccessful(periodicTask: periodicTask));
          onSuccess();
        } else {
          store.dispatch(new PeriodicTaskAddFail(apiResponseModel.message));
          onError();
        }
        break;

      default:
        store.dispatch(new PeriodicTaskAddFail("Unknown error: ${response.reasonPhrase}"));
        onError();
        break;
    }
  };
}

ThunkAction<AppState> updatePeriodicTask(NextCloudCredentials nextCloudCredentials, PeriodicTask periodicTask, VoidCallback onSuccess, VoidCallback onError) {
  return (Store<AppState> store) async {
    store.dispatch(new PeriodicTaskUpdateOnServer());

    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.put(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + "periodic_task/" + periodicTask.id.toString(),
        body: jsonEncode(PeriodicTask),
        headers: {'authorization': authorization, 'Accept': 'application/json', 'Content-Type': 'application/json'});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new PeriodicTaskUpdateFail("Invalid URL"));
        onError();
        break;

    // 405 For invalid URL
      case 405:
        store.dispatch(new PeriodicTaskUpdateFail("Method not allowed"));
        onError();
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new PeriodicTaskUpdateFail("Invalid Credentials"));
        onError();
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success" && apiResponseModel.data == 0) {
          store.dispatch(new PeriodicTaskUpdateSuccessful(periodicTask: periodicTask));
          onSuccess();
        } else {
          store.dispatch(new PeriodicTaskUpdateFail(apiResponseModel.message));
          onError();
        }
        break;

      default:
        store.dispatch(new PeriodicTaskUpdateFail("Unknown error: ${response.reasonPhrase}"));
        onError();
        break;
    }
  };
}

ThunkAction<AppState> deletePeriodicTask(NextCloudCredentials nextCloudCredentials, PeriodicTask periodicTask, VoidCallback onSuccess, VoidCallback onError) {
  return (Store<AppState> store) async {
    store.dispatch(new PeriodicTaskDeleteOnServer());

    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.delete(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + "periodic_task/${periodicTask.id}",
        headers: {'authorization': authorization});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new PeriodicTaskDeleteFail("Invalid URL"));
        onError();
        break;

    // 405 For invalid URL
      case 405:
        store.dispatch(new PeriodicTaskDeleteFail("Method not allowed"));
        onError();
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new PeriodicTaskDeleteFail("Invalid Credentials"));
        onError();
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success" && apiResponseModel.data == 0) {
          var periodicTaskSelected = store.state.periodicTaskList.length > 0 ? store.state.periodicTaskList.first : null;
          store.dispatch(new PeriodicTaskSelectSuccessful(periodicTask: periodicTaskSelected));
          store.dispatch(new PeriodicTaskDeleteSuccessful(periodicTask: periodicTask));
          onSuccess();
        } else {
          store.dispatch(new PeriodicTaskDeleteFail(apiResponseModel.message));
          onError();
        }
        break;

      default:
        store.dispatch(new PeriodicTaskDeleteFail("Unknown error: ${response.reasonPhrase}"));
        onError();
        break;
    }
  };
}
