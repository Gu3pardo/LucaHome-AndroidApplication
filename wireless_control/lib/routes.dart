library routes;

import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:wireless_control/actions/route.actions.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/pages/details_area.page.dart';
import 'package:wireless_control/pages/details_wireless_socket.page.dart';
import 'package:wireless_control/pages/list_wireless_socket.page.dart';
import 'package:wireless_control/pages/loading.page.dart';
import 'package:wireless_control/pages/login.page.dart';
import 'package:wireless_control/pages/no_network.page.dart';
import 'package:wireless_control/pages/settings.page.dart';

Map<String, WidgetBuilder> getRoutes(context, store) {
  return {
    '/': (BuildContext context) => new StoreBuilder<AppState>(
          onInit: (store) {
            store.onChange.listen((state) {
              var isLoading = state.isLoadingNextCloudCredentials ||
                  state.isLoadingArea ||
                  state.isLoadingNextCloudCredentials;

              if (isLoading && state.currentRoute != '/loading') {
                store.dispatch(new RouteChange(route: '/loading'));
                Navigator.popAndPushNamed(context, '/loading');
              }
            });
          },
          builder: (context, store) {
            return new ListWirelessSocketPage(store);
          },
        ),
    '/details-area': (BuildContext context) => new StoreBuilder<AppState>(
      onInit: (store) {
        store.onChange.listen((state) {
          var isLoading = state.isLoadingNextCloudCredentials ||
              state.isLoadingArea ||
              state.isLoadingNextCloudCredentials;

          if (isLoading && state.currentRoute != '/loading') {
            store.dispatch(new RouteChange(route: '/loading'));
            Navigator.popAndPushNamed(context, '/loading');
          }
        });
      },
      builder: (context, store) {
        var area = store.state.toBeAddedArea != null
            ? store.state.toBeAddedArea
            : store.state.selectedArea;
        return new DetailsAreaPage(area);
      },
    ),
    '/details-periodic-task': (BuildContext context) => new StoreBuilder<AppState>(
      onInit: (store) {
        // TODO
      },
      builder: (context, store) {
        return null; // TODO
      },
    ),
    '/details-wireless-socket': (BuildContext context) => new StoreBuilder<AppState>(
          onInit: (store) {
            store.onChange.listen((state) {
              var isLoading = state.isLoadingNextCloudCredentials ||
                  state.isLoadingArea ||
                  state.isLoadingNextCloudCredentials;

              if (isLoading && state.currentRoute != '/loading') {
                store.dispatch(new RouteChange(route: '/loading'));
                Navigator.popAndPushNamed(context, '/loading');
              }
            });
          },
          builder: (context, store) {
            var wirelessSocket = store.state.toBeAddedWirelessSocket != null
                ? store.state.toBeAddedWirelessSocket
                : store.state.selectedWirelessSocket;
            return new DetailsWirelessSocketPage(wirelessSocket);
          },
        ),
    '/list-periodic-task': (BuildContext context) => new StoreBuilder<AppState>(
      onInit: (store) {
        // TODO
      },
      builder: (context, store) {
        return null; // TODO
      },
    ),
    '/login': (BuildContext context) => new StoreBuilder<AppState>(
          onInit: (store) {
            store.onChange.listen((state) {
              var isLoading = state.isLoadingNextCloudCredentials ||
                  state.isLoadingArea ||
                  state.isLoadingNextCloudCredentials;

              if (isLoading && state.currentRoute != '/loading') {
                store.dispatch(new RouteChange(route: '/loading'));
                Navigator.popAndPushNamed(context, '/loading');
              }
            });
          },
          builder: (context, store) {
            return new LoginPage();
          },
        ),
    '/loading': (BuildContext context) => new StoreBuilder<AppState>(
          onInit: (store) {
            store.onChange.listen((state) {
              var isLoading = state.isLoadingNextCloudCredentials ||
                  state.isLoadingArea ||
                  state.isLoadingNextCloudCredentials;

              var isLoggedInOnServer = state.nextCloudCredentials != null &&
                  state.nextCloudCredentials.hasServer() &&
                  state.nextCloudCredentials.isLoggedIn();

              if (!isLoading && !isLoggedInOnServer && state.currentRoute != '/login') {
                store.dispatch(new RouteChange(route: '/login'));
                Navigator.popAndPushNamed(context, '/login');
              } else if (!isLoading && isLoggedInOnServer && state.currentRoute != '/') {
                store.dispatch(new RouteChange(route: '/'));
                Navigator.popAndPushNamed(context, '/');
              }
            });
          },
          builder: (context, store) {
            return new LoadingPage();
          },
        ),
    '/no-network': (BuildContext context) => new StoreBuilder<AppState>(
           onInit: (store) {},
           builder: (context, store) {
             return new NoNetworkPage();
           },
        ),
    '/settings': (BuildContext context) => new StoreBuilder<AppState>(
           onInit: (store) {},
           builder: (context, store) {
             return new SettingsPage();
           },
        ),
  };
}
