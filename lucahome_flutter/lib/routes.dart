library routes;

import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/pages/login.page.dart';
import 'package:lucahome_flutter/pages/loading.page.dart';
import 'package:lucahome_flutter/pages/list.page.dart';
import 'package:lucahome_flutter/pages/details.page.dart';
import 'package:lucahome_flutter/pages/no_network.page.dart';

void updateRoute(AppState state, BuildContext context) {
  if (state.nextCloudCredentials != null) {
    Navigator.pushNamed(context, '/');
  }
}

Map<String, WidgetBuilder> getRoutes(context, store) {
  return {
    '/': (BuildContext context) => new StoreBuilder<AppState>(
          builder: (context, store) {
            return new ListPage(store.state.wirelessSocketList);
          },
        ),
    '/details': (BuildContext context) => new StoreBuilder<AppState>(
          builder: (context, store) {
            return new DetailsPage(store.state.selectedWirelessSocket);
          },
        ),
    '/login': (BuildContext context) => new StoreBuilder<AppState>(
          builder: (context, store) {
            return new LoginPage();
          },
        ),
    '/loading': (BuildContext context) => new StoreBuilder<AppState>(
          onInit: (store) {
            var oldState = store.state.copyWith();
            store.onChange.listen((state) {
              if (state.nextCloudCredentials != oldState.nextCloudCredentials) {
                updateRoute(state, context);
                oldState = state.copyWith();
              }
            });
          },
          builder: (context, store) {
            return new LoadingPage();
          },
        ),
    '/no_network': (BuildContext context) => new StoreBuilder<AppState>(
          builder: (context, store) {
            return new NoNetworkPage();
          },
        ),
  };
}
