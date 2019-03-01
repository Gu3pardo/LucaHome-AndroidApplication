import 'dart:convert' as convert;
import 'package:http/http.dart' as http;
import 'package:lucahome_flutter/actions/wireless_socket.actions.dart';
import 'package:lucahome_flutter/constants/nextcloud.constants.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/models/wireless_socket.model.dart';
import 'package:redux/redux.dart';

class WirelessSocketService {
  Store<AppState> store;

  WirelessSocketService({this.store});

  void loadWirelessSockets() {
    var url = NextCloudConstants.baseUrl + "wireless_socket";
    http.get(url).then((response) {
      if (response.statusCode == 200) {
        try {
          var jsonResponse = convert.jsonDecode(response.body);
          var data = jsonResponse.data;

          if (data == false) {
            this
                .store
                .dispatch(new WirelessSocketLoadFail(jsonResponse.message));
          } else {
            this.store.dispatch(new WirelessSocketLoadSuccessful(list: data));
          }
        } catch (exception) {
          this.store.dispatch(new WirelessSocketLoadFail(exception));
        }
      } else {
        this.store.dispatch(new WirelessSocketLoadFail(response.statusCode));
      }
    });
  }

  void addWirelessSocket(WirelessSocket wirelessSocket) {
    var url = NextCloudConstants.baseUrl + "wireless_socket";
    http.post(url, body: convert.jsonEncode(wirelessSocket)).then((response) {
      if (response.statusCode == 200) {
        try {
          var jsonResponse = convert.jsonDecode(response.body);
          var data = jsonResponse.data;

          if (data == false) {
            this
                .store
                .dispatch(new WirelessSocketAddFail(jsonResponse.message));
          } else {
            if (data >= 0) {
              wirelessSocket.id = data;
              this.store.dispatch(new WirelessSocketAddSuccessful(
                  wirelessSocket: wirelessSocket));
            } else {
              this.store.dispatch(new WirelessSocketAddFail(data));
            }
          }
        } catch (exception) {
          this.store.dispatch(new WirelessSocketAddFail(exception));
        }
      } else {
        this.store.dispatch(new WirelessSocketAddFail(response.statusCode));
      }
    });
  }

  void updateWirelessSocket(WirelessSocket wirelessSocket) {
    var url = NextCloudConstants.baseUrl + "wireless_socket";
    http.put(url, body: convert.jsonEncode(wirelessSocket)).then((response) {
      if (response.statusCode == 200) {
        try {
          var jsonResponse = convert.jsonDecode(response.body);
          var data = jsonResponse.data;

          if (data == false) {
            this
                .store
                .dispatch(new WirelessSocketUpdateFail(jsonResponse.message));
          } else {
            if (data == 0) {
              this.store.dispatch(new WirelessSocketUpdateSuccessful(
                  wirelessSocket: wirelessSocket));
            } else {
              this.store.dispatch(new WirelessSocketUpdateFail(data));
            }
          }
        } catch (exception) {
          this.store.dispatch(new WirelessSocketUpdateFail(exception));
        }
      } else {
        this.store.dispatch(new WirelessSocketUpdateFail(response.statusCode));
      }
    });
  }

  void deleteWirelessSocket(WirelessSocket wirelessSocket) {
    var url = NextCloudConstants.baseUrl + "wireless_socket/$wirelessSocket.id";
    http.delete(url).then((response) {
      if (response.statusCode == 200) {
        try {
          var jsonResponse = convert.jsonDecode(response.body);
          var data = jsonResponse.data;

          if (data == false) {
            this
                .store
                .dispatch(new WirelessSocketDeleteFail(jsonResponse.message));
          } else {
            if (data == 0) {
              this.store.dispatch(new WirelessSocketDeleteSuccessful(
                  wirelessSocket: wirelessSocket));
            } else {
              this.store.dispatch(new WirelessSocketDeleteFail(data));
            }
          }
        } catch (exception) {
          this.store.dispatch(new WirelessSocketDeleteFail(exception));
        }
      } else {
        this.store.dispatch(new WirelessSocketDeleteFail(response.statusCode));
      }
    });
  }
}
