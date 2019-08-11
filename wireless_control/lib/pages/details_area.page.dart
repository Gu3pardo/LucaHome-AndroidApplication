import 'dart:async';
import 'package:connectivity/connectivity.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:redux/redux.dart';
import 'package:wireless_control/constants/color.constants.dart';
import 'package:wireless_control/enums/app_theme.enum.dart';
import 'package:wireless_control/enums/state_action.enum.dart';
import 'package:wireless_control/middleware/area.thunk_action.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/area.model.dart';
import 'package:wireless_control/presentation/shared-presentation.dart';
import 'package:wireless_control/utils/actions.util.dart';

class DetailsAreaPage extends StatefulWidget {
  static String tag = 'details-area-page';

  final Area area;

  DetailsAreaPage(this.area);

  @override
  _DetailsAreaPageState createState() => new _DetailsAreaPageState();
}

class _DetailsAreaPageState extends State<DetailsAreaPage> {
  final _formKey = GlobalKey<FormState>();
  final Connectivity _connectivity = Connectivity();
  StreamSubscription<ConnectivityResult> _connectivitySubscription;
  StateAction stateAction;

  @override
  void initState() {
    super.initState();
    initConnectivity();
    _connectivitySubscription = _connectivity.onConnectivityChanged.listen(_updateConnectionStatus);
    this.stateAction = widget.area.deletable == 1
        ? widget.area.name == ""
          ? StateAction.Add
          : StateAction.Update
        : StateAction.Readonly;
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
              backgroundColor: viewModel.loadTheme() == AppTheme.Light ? ColorConstants.BackgroundLight : Colors.grey[850],
              appBar: AppBar(
                backgroundColor: ColorConstants.AppBar,
                title: (this.stateAction == StateAction.Update || this.stateAction == StateAction.Readonly)
                    ? Text('Details for ${widget.area.name}')
                    : Text('Add area'),
              ),
              body: ListView(
                children: <Widget>[
                  Row(
                    children: <Widget>[
                      Container(
                        width: pageSize.width,
                        height: pageSize.height * 0.2,
                        alignment: Alignment.center,
                        child: getDetailsIcon(FontAwesomeIcons.map, viewModel.loadTheme()),
                      )
                    ],
                  ),
                  Row(
                    children: <Widget>[
                      Container(
                          width: pageSize.width,
                          height: pageSize.height * 0.45,
                          alignment: Alignment.center,
                          child: Center(
                            child: ListView(
                              padding: EdgeInsets.only(left: 24.0, right: 24.0),
                              children: <Widget>[
                                getTextFormField(widget.area.name, 'Name',
                                        (value) {if (value.isEmpty) {return 'Name is required';}return "";},
                                        (String value) {widget.area.name = value; widget.area.filter = value;},
                                        viewModel.loadTheme(),
                                        null),
                              ],
                            ),
                          )),
                    ],
                  ),
                  Row(
                    children: <Widget>[
                      Container(
                        width: pageSize.width,
                        height: pageSize.height * 0.2,
                        alignment: Alignment.center,
                        child: ListView(
                          padding: EdgeInsets.only(left: 24.0, right: 24.0),
                          children: <Widget>[
                            this.stateAction != StateAction.Readonly ?
                            Padding(
                              padding: EdgeInsets.symmetric(vertical: 2.0),
                              child: RaisedButton(
                                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24),),
                                onPressed: () {
                                  if (_formKey.currentState.validate()) {
                                    _formKey.currentState.save();
                                    viewModel.save(context, widget.area);
                                  }
                                },
                                padding: EdgeInsets.all(12),
                                color: ColorConstants.ButtonSubmit,
                                child: Text('Save', style: TextStyle(color: viewModel.loadTheme() == AppTheme.Light ? ColorConstants.TextLight : ColorConstants.TextDark)),
                              ),
                            ) : Padding(padding: EdgeInsets.symmetric(vertical: 2.0)),
                            this.stateAction == StateAction.Update ?
                            Padding(
                              padding: EdgeInsets.symmetric(vertical: 2.0),
                              child: RaisedButton(
                                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                                onPressed: () => viewModel.delete(context, widget.area),
                                padding: EdgeInsets.all(12),
                                color: ColorConstants.ButtonDelete,
                                child: Text('Delete', style: TextStyle(color: viewModel.loadTheme() == AppTheme.Light ? ColorConstants.TextLight : ColorConstants.TextDark)),
                              ),
                            ) : Padding(padding: EdgeInsets.symmetric(vertical: 2.0)),
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
  final Function loadTheme;
  final Function save;
  final Function delete;
  final Function validateArea;

  _ViewModel({this.loadTheme, this.save, this.delete, this.validateArea});

  static _ViewModel fromStore(Store<AppState> store) {
    return _ViewModel(
        loadTheme:() {
          return store.state.theme;
        },
        save: (BuildContext context, Area area) {
          if (area.id == -1) {
            store.dispatch(addArea(
                store.state.nextCloudCredentials,
                area,
                () => onSuccess(context, 'Successfully added area ${area.name}'),
                () => onError(context, 'Failed to add area ${area.name}')));
          } else {
            store.dispatch(updateArea(
                store.state.nextCloudCredentials,
                area,
                () => onSuccess(context, 'Successfully updated area ${area.name}'),
                () => onError(context, 'Failed to update area ${area.name}')));
          }
        },
        delete: (BuildContext context, Area area) {
          deleteDialog(store, context, area);
        },
        validateArea: (String areaName) {
          return store.state.areaList.singleWhere((area) => area.name == areaName, orElse: () => null) != null;
        });
  }

  static void deleteDialog(Store<AppState> store, BuildContext context, Area area) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text('Delete ${area.name}?'),
          content: Text('Do you really want to delete ${area.name}? (Also deletes wireless sockets in this area!)'),
          actions: <Widget>[
            FlatButton(
              child: Text("Cancel"),
              onPressed: () {
                Navigator.pop(context, true);
              },
            ),
            FlatButton(
              child: Text("Delete"),
              onPressed: () {
                store.dispatch(deleteArea(
                    store.state.nextCloudCredentials,
                    area,
                    () => onSuccess(context, 'Successfully deleted area ${area.name}'),
                    () => onError(context, 'Failed to delete area ${area.name}')));
                Navigator.pop(context, true);
              },
            ),
          ],
        );
      },
    );
  }
}
