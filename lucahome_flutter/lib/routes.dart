library routes;

import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:lucahome_flutter/actions/route.actions.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/pages/login.page.dart';
import 'package:lucahome_flutter/pages/loading.page.dart';
import 'package:lucahome_flutter/pages/list.page.dart';
import 'package:lucahome_flutter/pages/details.page.dart';
import 'package:lucahome_flutter/pages/no_network.page.dart';

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
            return new ListPage(store.state.wirelessSocketList);
          },
        ),
    '/details': (BuildContext context) => new StoreBuilder<AppState>(
          onInit: (store) {},
          builder: (context, store) {
            return new DetailsPage(store.state.selectedWirelessSocket);
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
    '/no_network': (BuildContext context) => new StoreBuilder<AppState>(
          onInit: (store) {},
          builder: (context, store) {
            return new NoNetworkPage();
          },
        ),
  };
}
