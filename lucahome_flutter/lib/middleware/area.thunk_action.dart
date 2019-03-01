import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:lucahome_flutter/actions/area.actions.dart';
import 'package:lucahome_flutter/constants/nextcloud.constants.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:redux/redux.dart';
import 'package:redux_thunk/redux_thunk.dart';

ThunkAction<AppState> loadAreas = (Store<AppState> store) async {
  var nextCloudCredentials = store.state.nextCloudCredentials;
  var authorization = 'Basic ' +
      base64Encode(utf8.encode(
          '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

  var response = await http.get(NextCloudConstants.baseUrl + "area",
      headers: {'authorization': authorization});
  if (response.statusCode == 200) {
    try {
      var jsonResponse = jsonDecode(response.body);
      var data = jsonResponse.data;

      if (data == false) {
        store.dispatch(new AreaLoadFail(jsonResponse.message));
      } else {
        store.dispatch(new AreaLoadSuccessful(list: data));
      }
    } catch (exception) {
      store.dispatch(new AreaLoadFail(exception));
    }
  } else {
    store.dispatch(new AreaLoadFail(response.statusCode));
  }
};

ThunkAction<AppState> addArea = (Store<AppState> store) async {
  var nextCloudCredentials = store.state.nextCloudCredentials;
  var authorization = 'Basic ' +
      base64Encode(utf8.encode(
          '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

  var area = store.state.selectedArea;
  var response = await http.post(NextCloudConstants.baseUrl + "area",
      body: jsonEncode(area), headers: {'authorization': authorization});
  if (response.statusCode == 200) {
    try {
      var jsonResponse = jsonDecode(response.body);
      var data = jsonResponse.data;

      if (data == false) {
        store.dispatch(new AreaAddFail(jsonResponse.message));
      } else {
        if (data >= 0) {
          area.id = data;
          store.dispatch(new AreaAddSuccessful(area: area));
        } else {
          store.dispatch(new AreaAddFail(data));
        }
      }
    } catch (exception) {
      store.dispatch(new AreaAddFail(exception));
    }
  } else {
    store.dispatch(new AreaAddFail(response.statusCode));
  }
};

ThunkAction<AppState> updateArea = (Store<AppState> store) async {
  var nextCloudCredentials = store.state.nextCloudCredentials;
  var authorization = 'Basic ' +
      base64Encode(utf8.encode(
          '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

  var area = store.state.selectedArea;
  var response = await http.put(NextCloudConstants.baseUrl + "area",
      body: jsonEncode(area), headers: {'authorization': authorization});
  if (response.statusCode == 200) {
    try {
      var jsonResponse = jsonDecode(response.body);
      var data = jsonResponse.data;

      if (data == false) {
        store.dispatch(new AreaUpdateFail(jsonResponse.message));
      } else {
        if (data == 0) {
          store.dispatch(new AreaUpdateSuccessful(area: area));
        } else {
          store.dispatch(new AreaUpdateFail(data));
        }
      }
    } catch (exception) {
      store.dispatch(new AreaUpdateFail(exception));
    }
  } else {
    store.dispatch(new AreaUpdateFail(response.statusCode));
  }
};

ThunkAction<AppState> deleteArea = (Store<AppState> store) async {
  var nextCloudCredentials = store.state.nextCloudCredentials;
  var authorization = 'Basic ' +
      base64Encode(utf8.encode(
          '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

  var area = store.state.selectedArea;
  var response = await http.delete(NextCloudConstants.baseUrl + "area/$area.id",
      headers: {'authorization': authorization});
  if (response.statusCode == 200) {
    try {
      var jsonResponse = jsonDecode(response.body);
      var data = jsonResponse.data;

      if (data == false) {
        store.dispatch(new AreaDeleteFail(jsonResponse.message));
      } else {
        if (data == 0) {
          store.dispatch(new AreaDeleteSuccessful(area: area));
        } else {
          store.dispatch(new AreaDeleteFail(data));
        }
      }
    } catch (exception) {
      store.dispatch(new AreaDeleteFail(exception));
    }
  } else {
    store.dispatch(new AreaDeleteFail(response.statusCode));
  }
};
