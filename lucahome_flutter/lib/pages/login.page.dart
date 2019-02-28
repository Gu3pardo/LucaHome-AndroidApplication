import 'dart:async';
import 'package:connectivity/connectivity.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:lucahome_flutter/actions/next_cloud_credentials.actions.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:redux/redux.dart';

class LoginPage extends StatefulWidget {
  static String tag = 'login-page';

  @override
  _LoginPageState createState() => new _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _formKey = GlobalKey<FormState>();
  final Connectivity _connectivity = Connectivity();
  StreamSubscription<ConnectivityResult> _connectivitySubscription;

  @override
  void initState() {
    super.initState();
    initConnectivity();
    _connectivitySubscription =
        _connectivity.onConnectivityChanged.listen(_updateConnectionStatus);
  }

  @override
  void dispose() {
    _connectivitySubscription.cancel();
    super.dispose();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initConnectivity() async {
    ConnectivityResult result;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      result = await _connectivity.checkConnectivity();
    } on PlatformException catch (e) {
      print(e.toString());
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) {
      return;
    }

    _updateConnectionStatus(result);
  }

  Future<void> _updateConnectionStatus(ConnectivityResult result) async {
    switch (result) {
      case ConnectivityResult.mobile:
      case ConnectivityResult.wifi:
        // Everything is fine, we can stay here
        break;
      default:
        // No valid network, so we navigate to no network page
        Navigator.of(context).pushNamed('/no_network');
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    var pageSize = MediaQuery.of(context).size;

    final logo = Hero(
      tag: 'hero',
      child: CircleAvatar(
        backgroundColor: Colors.transparent,
        radius: 48.0,
        child: Image.asset('assets/logo.png'),
      ),
    );

    final nextCloudUrl = TextFormField(
      keyboardType: TextInputType.url,
      autofocus: true,
      initialValue: '',
      decoration: InputDecoration(
        hintText: 'NextCloudUrl',
        contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
        hintStyle: TextStyle(color: Colors.white54),
        errorStyle: TextStyle(color: Colors.red),
      ),
      style: TextStyle(color: Colors.white),
      validator: (value) {
        if (value.isEmpty) {
          return 'NextCloudUrl is required';
        }
      },
    );

    final userName = TextFormField(
      keyboardType: TextInputType.text,
      autofocus: false,
      initialValue: '',
      decoration: InputDecoration(
        hintText: 'UserName',
        contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
        hintStyle: TextStyle(color: Colors.white54),
        errorStyle: TextStyle(color: Colors.red),
      ),
      style: TextStyle(color: Colors.white),
      validator: (value) {
        if (value.isEmpty) {
          return 'UserName is required';
        }
      },
    );

    final passPhrase = TextFormField(
      autofocus: false,
      initialValue: '',
      obscureText: true,
      decoration: InputDecoration(
        hintText: 'Password',
        contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
        hintStyle: TextStyle(color: Colors.white54),
        errorStyle: TextStyle(color: Colors.red),
      ),
      style: TextStyle(color: Colors.white),
      validator: (value) {
        if (value.isEmpty) {
          return 'Password is required';
        }
      },
    );

    return new StoreConnector<AppState, _ViewModel>(
      converter: _ViewModel.fromStore,
      builder: (BuildContext context, _ViewModel viewModel) {
        return Form(
            key: _formKey,
            child: new Scaffold(
              body: new Stack(
                children: <Widget>[
                  new Container(
                    alignment: Alignment.center,
                    width: pageSize.width,
                    height: pageSize.height,
                    decoration: new BoxDecoration(
                      gradient: new LinearGradient(
                        begin: Alignment.topRight,
                        end: Alignment.bottomLeft,
                        stops: [0.2, 1.0],
                        colors: [
                          const Color(0xFF3744B0),
                          const Color(0xFF3799B0),
                        ],
                      ),
                    ),
                    child: ListView(
                      shrinkWrap: true,
                      padding: EdgeInsets.only(left: 24.0, right: 24.0),
                      children: <Widget>[
                        logo,
                        SizedBox(height: 48.0),
                        nextCloudUrl,
                        SizedBox(height: 24.0),
                        userName,
                        SizedBox(height: 8.0),
                        passPhrase,
                        SizedBox(height: 24.0),
                        Padding(
                          padding: EdgeInsets.symmetric(vertical: 16.0),
                          child: RaisedButton(
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(24),
                            ),
                            onPressed: () {
                              if (_formKey.currentState.validate()) {
                                // TODO Do something with the data
                                viewModel.onPressedCallback(context);
                              }
                            },
                            padding: EdgeInsets.all(12),
                            color: Colors.lightBlueAccent,
                            child: Text('Log In',
                                style: TextStyle(color: Colors.white)),
                          ),
                        )
                      ],
                    ),
                  ),
                ],
              ),
            ));
      },
    );
  }
}

class _ViewModel {
  final Function onPressedCallback;

  _ViewModel({this.onPressedCallback});

  static _ViewModel fromStore(Store<AppState> store) {
    return new _ViewModel(
      onPressedCallback: (context) {
        store.dispatch(new NextCloudCredentialsLogIn());
        Navigator.of(context).pushNamed('/loading');
      },
    );
  }
}
