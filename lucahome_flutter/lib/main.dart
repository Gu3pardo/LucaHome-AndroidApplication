import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/reducers/app.reducer.dart';
import 'package:lucahome_flutter/routes.dart';
import 'package:redux/redux.dart';

void main() => runApp(new MainApp());

class MainApp extends StatelessWidget {
  final String title = 'LucaHome';

  MainApp();

  @override
  Widget build(BuildContext context) {
    var store = new Store<AppState>(
      appReducer,
      initialState: new AppState(),
      distinct: true
    );

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