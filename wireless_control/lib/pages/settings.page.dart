import 'dart:async';
import 'package:connectivity/connectivity.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:redux/redux.dart';
import 'package:wireless_control/constants/color.constants.dart';
import 'package:wireless_control/enums/app_theme.enum.dart';
import 'package:wireless_control/middleware/next_cloud_credentials.thunk_action.dart';
import 'package:wireless_control/middleware/theme.thunk_action.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/next_cloud_credentials.model.dart';
import 'package:wireless_control/presentation/shared-presentation.dart';

class SettingsPage extends StatefulWidget {
  static String tag = 'settings-page';

  SettingsPage();

  @override
  _SettingsPageState createState() => new _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  final _formKey = GlobalKey<FormState>();
  final Connectivity _connectivity = Connectivity();
  StreamSubscription<ConnectivityResult> _connectivitySubscription;

  @override
  void initState() {
    super.initState();
    initConnectivity();
    _connectivitySubscription = _connectivity.onConnectivityChanged.listen(_updateConnectionStatus);
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
        Navigator.of(context).pushNamed('/no-network');
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    var pageSize = MediaQuery.of(context).size;

    return StoreConnector<AppState, _ViewModel>(
      converter: _ViewModel.fromStore,
      builder: (BuildContext context, _ViewModel viewModel) {
        return Form(
            key: _formKey,
            child: Scaffold(
              appBar: AppBar(
                backgroundColor: ColorConstants.AppBar,
                title: Text('Settings'),
              ),
              body: ListView(
                children: <Widget>[
                  Row(
                    children: <Widget>[
                      Container(
                        width: pageSize.width,
                        height: pageSize.height * 0.275,
                        color: viewModel.theme == AppTheme.Light ? ColorConstants.BackgroundLight : ColorConstants.BackgroundDark,
                        alignment: Alignment.center,
                        child: getDetailsIcon(FontAwesomeIcons.cogs, viewModel.theme),
                      )
                    ],
                  ),
                  Row(
                    children: <Widget>[
                      Container(
                          width: pageSize.width,
                          height: pageSize.height * 0.35,
                          color: viewModel.theme == AppTheme.Light ? ColorConstants.BackgroundLight : ColorConstants.BackgroundDark,
                          alignment: Alignment.center,
                          child: Center(
                            child: ListView(
                              padding: EdgeInsets.only(left: 24.0, right: 24.0),
                              children: <Widget>[
                                getTextFormField(viewModel.nextCloudCredentials.userName, 'UserName',
                                        (value) {if (value.isEmpty) {return 'UserName is required';}},
                                        (String value) {viewModel.nextCloudCredentials.userName = value;},
                                        viewModel.theme),
                                getTextFormField(viewModel.nextCloudCredentials.passPhrase, 'PassPhrase',
                                        (value) {if (value.isEmpty) {return 'PassPhrase is required';}},
                                        (String value) {viewModel.nextCloudCredentials.passPhrase = value;},
                                        viewModel.theme),
                                getTextFormField(viewModel.nextCloudCredentials.baseUrl, 'Url',
                                        (value) {if (value.isEmpty) {return 'Url is required';}},
                                        (String value) {viewModel.nextCloudCredentials.baseUrl = value;},
                                        viewModel.theme),
                                Padding(
                                  padding: EdgeInsets.symmetric(vertical: 2.0),
                                  child: RaisedButton(
                                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24),),
                                    onPressed: () {
                                      if (_formKey.currentState.validate()) {
                                        _formKey.currentState.save();
                                        viewModel.saveCredentials(viewModel.nextCloudCredentials);
                                      }
                                    },
                                    padding: EdgeInsets.all(12),
                                    color: ColorConstants.ButtonSubmit,
                                    child: Text('Save Credentials', style: TextStyle(color: viewModel.theme == AppTheme.Light ? ColorConstants.TextLight : ColorConstants.TextDark)),
                                  ),
                                ),
                              ],
                            ),
                          )),
                    ],
                  ),
                  Row(
                    children: <Widget>[
                      Container(
                        width: pageSize.width,
                        height: pageSize.height * 0.275,
                        color: viewModel.theme == AppTheme.Light ? ColorConstants.BackgroundLight : ColorConstants.BackgroundDark,
                        alignment: Alignment.center,
                        child: ListView(
                          padding: EdgeInsets.only(left: 24.0, right: 24.0),
                          children: <Widget>[
                            DropdownButton<String>(
                              hint: Text("Please choose a theme", style: TextStyle(color: ColorConstants.Hint)),
                              value: viewModel.theme.toString(),
                              items: AppTheme.values.map((AppTheme appTheme) {
                                return new DropdownMenuItem<String>(
                                  value: appTheme.toString(),
                                  child: Text(appTheme.toString(), style: TextStyle(color: ColorConstants.TextDark)),
                                );
                              }).toList(),
                              onChanged: (themeString) {
                                viewModel.theme = AppTheme.values.firstWhere((AppTheme a) => a.toString() == themeString);
                                viewModel.saveAppTheme(viewModel.theme);
                              },
                            ),
                          ],
                        ),
                      )
                    ],
                  ),
                ],
              ),
            ));
      },
    );
  }
}

class _ViewModel {
  AppTheme theme;
  NextCloudCredentials nextCloudCredentials;

  final Function saveAppTheme;
  final Function saveCredentials;

  _ViewModel({this.nextCloudCredentials, this.theme, this.saveAppTheme, this.saveCredentials});

  static _ViewModel fromStore(Store<AppState> store) {
    return _ViewModel(
        theme: store.state.theme,
        nextCloudCredentials: store.state.nextCloudCredentials,

        saveCredentials: (NextCloudCredentials nextCloudCredentials) {
          store.dispatch(logIn(nextCloudCredentials));
        },
        saveAppTheme: (AppTheme theme) {
          store.dispatch(saveTheme(theme));
        });
  }
}
