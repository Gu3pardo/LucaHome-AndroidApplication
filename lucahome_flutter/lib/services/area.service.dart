import 'dart:convert' as convert;
import 'package:http/http.dart' as http;
import 'package:lucahome_flutter/actions/area.actions.dart';
import 'package:lucahome_flutter/constants/nextcloud.constants.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/models/area.model.dart';
import 'package:redux/redux.dart';

class AreaService {
  Store<AppState> store;

  AreaService({this.store});

  void loadAreas() {
    var url = NextCloudConstants.baseUrl + "area";
    http.get(url).then((response) {
      if (response.statusCode == 200) {
        try {
          var jsonResponse = convert.jsonDecode(response.body);
          var data = jsonResponse.data;

          if (data == false) {
            this.store.dispatch(new AreaLoadFail(jsonResponse.message));
          } else {
            this.store.dispatch(new AreaLoadSuccessful(list: data));
          }
        } catch (exception) {
          this.store.dispatch(new AreaLoadFail(exception));
        }
      } else {
        this.store.dispatch(new AreaLoadFail(response.statusCode));
      }
    });
  }

  void addArea(Area area) {
    var url = NextCloudConstants.baseUrl + "area";
    http.post(url, body: convert.jsonEncode(area)).then((response) {
      if (response.statusCode == 200) {
        try {
          var jsonResponse = convert.jsonDecode(response.body);
          var data = jsonResponse.data;

          if (data == false) {
            this.store.dispatch(new AreaAddFail(jsonResponse.message));
          } else {
            if (data >= 0) {
              area.id = data;
              this.store.dispatch(new AreaAddSuccessful(area: area));
            } else {
              this.store.dispatch(new AreaAddFail(data));
            }
          }
        } catch (exception) {
          this.store.dispatch(new AreaAddFail(exception));
        }
      } else {
        this.store.dispatch(new AreaAddFail(response.statusCode));
      }
    });
  }

  void updateArea(Area area) {
    var url = NextCloudConstants.baseUrl + "area";
    http.put(url, body: convert.jsonEncode(area)).then((response) {
      if (response.statusCode == 200) {
        try {
          var jsonResponse = convert.jsonDecode(response.body);
          var data = jsonResponse.data;

          if (data == false) {
            this.store.dispatch(new AreaUpdateFail(jsonResponse.message));
          } else {
            if (data == 0) {
              this.store.dispatch(new AreaUpdateSuccessful(area: area));
            } else {
              this.store.dispatch(new AreaUpdateFail(data));
            }
          }
        } catch (exception) {
          this.store.dispatch(new AreaUpdateFail(exception));
        }
      } else {
        this.store.dispatch(new AreaUpdateFail(response.statusCode));
      }
    });
  }

  void deleteArea(Area area) {
    var url = NextCloudConstants.baseUrl + "area/$area.id";
    http.delete(url).then((response) {
      if (response.statusCode == 200) {
        try {
          var jsonResponse = convert.jsonDecode(response.body);
          var data = jsonResponse.data;

          if (data == false) {
            this.store.dispatch(new AreaDeleteFail(jsonResponse.message));
          } else {
            if (data == 0) {
              this.store.dispatch(new AreaDeleteSuccessful(area: area));
            } else {
              this.store.dispatch(new AreaDeleteFail(data));
            }
          }
        } catch (exception) {
          this.store.dispatch(new AreaDeleteFail(exception));
        }
      } else {
        this.store.dispatch(new AreaDeleteFail(response.statusCode));
      }
    });
  }
}
