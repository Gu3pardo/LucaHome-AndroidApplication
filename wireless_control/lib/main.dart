import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:wireless_control/actions/route.actions.dart';
import 'package:wireless_control/middleware/next_cloud_credentials.thunk_action.dart';
import 'package:wireless_control/middleware/theme.thunk_action.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/next_cloud_credentials.model.dart';
import 'package:wireless_control/reducers/app.reducer.dart';
import 'package:wireless_control/routes.dart';
import 'package:wireless_control/utils/shared_pref.utils.dart';
import 'package:redux/redux.dart';
import 'package:redux_thunk/redux_thunk.dart';

void main() => runApp(new MainApp());

class MainApp extends StatelessWidget {
  final String title = 'WirelessControl';

  MainApp();

  @override
  Widget build(BuildContext context) {
    var store = new Store<AppState>(appReducer,
        initialState: new AppState(),
        distinct: true,
        middleware: [thunkMiddleware]);

    return FutureBuilder(
        future: loadNextCloudCredentials(),
        builder: (BuildContext context, AsyncSnapshot<NextCloudCredentials> snapshot) {
          switch (snapshot.connectionState) {
            case ConnectionState.none:
            case ConnectionState.waiting:
            case ConnectionState.active:
              return new Center(child: new CircularProgressIndicator());
            case ConnectionState.done:
              store.dispatch(loadTheme());

              if (snapshot.data.hasServer() && snapshot.data.isLoggedIn()) {
                print("Already hasServer and isLoggedIn");
                store.dispatch(new RouteChange(route: "/loading"));
                store.dispatch(logIn(snapshot.data));

                return new StoreProvider(
                  store: store,
                  child: new MaterialApp(
                    debugShowCheckedModeBanner: false,
                    title: title,
                    routes: getRoutes(context, store),
                    initialRoute: '/loading',
                  ),
                );
              } else {
                print("Needs login");
                store.dispatch(new RouteChange(route: "/login"));
                return new StoreProvider(
                  store: store,
                  child: new MaterialApp(
                    debugShowCheckedModeBanner: false,
                    title: title,
                    routes: getRoutes(context, store),
                    initialRoute: '/login',
                  ),
                );
              }
          }
        });
  }
}
