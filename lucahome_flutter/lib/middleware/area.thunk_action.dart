import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:lucahome_flutter/actions/area.actions.dart';
import 'package:lucahome_flutter/constants/nextcloud.constants.dart';
import 'package:lucahome_flutter/models/api_response.model.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/models/area.model.dart';
import 'package:lucahome_flutter/models/next_cloud_credentials.model.dart';
import 'package:redux/redux.dart';
import 'package:redux_thunk/redux_thunk.dart';

ThunkAction<AppState> loadAreas(NextCloudCredentials nextCloudCredentials) {
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
        store.dispatch(new AreaLoadFail("Invalid URL"));
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new AreaLoadFail("Invalid Credentials"));
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel<List<Area>>.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success") {
          store.dispatch(new AreaLoadSuccessful(list: apiResponseModel.data));
        } else {
          store.dispatch(new AreaLoadFail(apiResponseModel.message));
        }
        break;

      default:
        store.dispatch(new AreaLoadFail("Unknown error"));
        break;
    }
  };
}

ThunkAction<AppState> addArea(NextCloudCredentials nextCloudCredentials, Area area) {
  return (Store<AppState> store) async {
    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.post(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + "area",
        body: jsonEncode(area),
        headers: {'authorization': authorization});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new AreaAddFail("Invalid URL"));
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new AreaAddFail("Invalid Credentials"));
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel<int>.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success" && apiResponseModel.data >= 0) {
          area.id = apiResponseModel.data;
          store.dispatch(new AreaAddSuccessful(area: area));
        } else {
          store.dispatch(new AreaAddFail(apiResponseModel.message));
        }
        break;

      default:
        store.dispatch(new AreaAddFail("Unknown error"));
        break;
    }
  };
}

ThunkAction<AppState> updateArea(NextCloudCredentials nextCloudCredentials, Area area) {
  return (Store<AppState> store) async {
    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.put(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + "area",
        body: jsonEncode(area),
        headers: {'authorization': authorization});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new AreaUpdateFail("Invalid URL"));
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new AreaUpdateFail("Invalid Credentials"));
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel<int>.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success" && apiResponseModel.data == 0) {
          store.dispatch(new AreaUpdateSuccessful(area: area));
        } else {
          store.dispatch(new AreaUpdateFail(apiResponseModel.message));
        }
        break;

      default:
        store.dispatch(new AreaUpdateFail("Unknown error"));
        break;
    }
  };
}

ThunkAction<AppState> deleteArea(NextCloudCredentials nextCloudCredentials, Area area) {
  return (Store<AppState> store) async {
    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.delete(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + "area/$area.id",
        headers: {'authorization': authorization});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new AreaDeleteFail("Invalid URL"));
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new AreaDeleteFail("Invalid Credentials"));
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel<int>.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success" && apiResponseModel.data == 0) {
          store.dispatch(new AreaDeleteSuccessful(area: area));
        } else {
          store.dispatch(new AreaDeleteFail(apiResponseModel.message));
        }
        break;

      default:
        store.dispatch(new AreaDeleteFail("Unknown error"));
        break;
    }
  };
}
