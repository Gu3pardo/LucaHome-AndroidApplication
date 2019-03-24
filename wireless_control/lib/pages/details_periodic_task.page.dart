import 'dart:async';
import 'package:connectivity/connectivity.dart';
import 'package:datetime_picker_formfield/datetime_picker_formfield.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:intl/intl.dart';
import 'package:redux/redux.dart';
import 'package:wireless_control/constants/color.constants.dart';
import 'package:wireless_control/enums/app_theme.enum.dart';
import 'package:wireless_control/enums/state_action.enum.dart';
import 'package:wireless_control/enums/weekday.enum.dart';
import 'package:wireless_control/middleware/periodic_task.thunk_action.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/periodic_task.model.dart';
import 'package:wireless_control/presentation/shared-presentation.dart';
import 'package:wireless_control/utils/actions.util.dart';

class DetailsPeriodicTaskPage extends StatefulWidget {
  static String tag = 'details-periodic-task-page';

  final PeriodicTask periodicTask;
  final String wirelessSocketName;

  DetailsPeriodicTaskPage(this.periodicTask, this.wirelessSocketName);

  @override
  _DetailsPeriodicTaskPageState createState() => new _DetailsPeriodicTaskPageState();
}

class _DetailsPeriodicTaskPageState extends State<DetailsPeriodicTaskPage> {
  final _formKey = GlobalKey<FormState>();
  final Connectivity _connectivity = Connectivity();
  StreamSubscription<ConnectivityResult> _connectivitySubscription;
  StateAction stateAction;

  @override
  void initState() {
    super.initState();
    initConnectivity();
    _connectivitySubscription = _connectivity.onConnectivityChanged.listen(_updateConnectionStatus);
    this.stateAction = widget.periodicTask.name == ""
          ? StateAction.Add
          : StateAction.Update;
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
                title: (this.stateAction == StateAction.Update || this.stateAction == StateAction.Readonly)
                    ? Text('Details for ${widget.periodicTask.name}')
                    : Text('Add Periodic Task for ${widget.wirelessSocketName}'),
              ),
              body: ListView(
                children: <Widget>[
                  Row(
                    children: <Widget>[
                      Container(
                        width: pageSize.width,
                        height: pageSize.height * 0.275,
                        color: viewModel.loadTheme() == AppTheme.Light ? ColorConstants.BackgroundLight : ColorConstants.BackgroundDark,
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
                          color: viewModel.loadTheme() == AppTheme.Light ? ColorConstants.BackgroundLight : ColorConstants.BackgroundDark,
                          alignment: Alignment.center,
                          child: Center(
                            child: ListView(
                              padding: EdgeInsets.only(left: 24.0, right: 24.0),
                              children: <Widget>[
                                getTextFormField(widget.periodicTask.name, 'Name',
                                        (value) {if (value.isEmpty) {return 'Name is required';}},
                                        (String value) {widget.periodicTask.name = value;},
                                        viewModel.loadTheme()),
                                new CheckboxListTile(
                                    title: Text('Activate/Deactivated', style: TextStyle(color: viewModel.loadTheme() == AppTheme.Light ? ColorConstants.TextDark : ColorConstants.TextLight),),
                                    value: widget.periodicTask.wirelessSocketState == 1,
                                    onChanged: (bool value) {setState(() {widget.periodicTask.wirelessSocketState = (value ? 1 : 0);});}
                                    ),
                                DropdownButton<int>(
                                  hint: Text("Please choose a weekday", style: TextStyle(color: ColorConstants.Hint)),
                                  value: widget.periodicTask.weekday,
                                  items: Weekday.values.map((Weekday weekday) {
                                    return new DropdownMenuItem<int>(
                                      value: weekday.index,
                                      child: Text(weekday.toString(), style: TextStyle(color: viewModel.loadTheme() == AppTheme.Light ? ColorConstants.TextDark : ColorConstants.TextLight)),
                                    );
                                  }).toList(),
                                  onChanged: (int weekdayIndex) {
                                    widget.periodicTask.weekday = weekdayIndex + 1;
                                  },
                                ),
                                DateTimePickerFormField(
                                  inputType: InputType.time,
                                  format: DateFormat('HH:mm'),
                                  editable: true,
                                  decoration: InputDecoration(labelText: 'Time', hasFloatingPlaceholder: false),
                                  onChanged: (DateTime dateTime) {widget.periodicTask.hour = dateTime.hour;widget.periodicTask.minute = dateTime.minute;},
                                ),
                                new CheckboxListTile(
                                    title: Text('Periodic', style: TextStyle(color: viewModel.loadTheme() == AppTheme.Light ? ColorConstants.TextDark : ColorConstants.TextLight),),
                                    value: widget.periodicTask.periodic == 1,
                                    onChanged: (bool value) {setState(() {widget.periodicTask.periodic = (value ? 1 : 0);});}
                                ),
                                new CheckboxListTile(
                                    title: Text('Active', style: TextStyle(color: viewModel.loadTheme() == AppTheme.Light ? ColorConstants.TextDark : ColorConstants.TextLight),),
                                    value: widget.periodicTask.active == 1,
                                    onChanged: (bool value) {setState(() {widget.periodicTask.active = (value ? 1 : 0);});}
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
                        height: pageSize.height * 0.175,
                        color: viewModel.loadTheme() == AppTheme.Light ? ColorConstants.BackgroundLight : ColorConstants.BackgroundDark,
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
                                    viewModel.save(context, widget.periodicTask);
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
                                onPressed: () => viewModel.delete(context, widget.periodicTask),
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
  final Function validatePeriodicTask;

  _ViewModel({this.loadTheme, this.save, this.delete, this.validatePeriodicTask});

  static _ViewModel fromStore(Store<AppState> store) {
    return _ViewModel(
        loadTheme:() {
          return store.state.theme;
        },
        save: (BuildContext context, PeriodicTask periodicTask) {
          if (periodicTask.id == -1) {
            store.dispatch(addPeriodicTask(
                store.state.nextCloudCredentials,
                periodicTask,
                () => onSuccess(context, 'Successfully added periodic task ${periodicTask.name}'),
                () => onError(context, 'Failed to add periodic task ${periodicTask.name}')));
          } else {
            store.dispatch(updatePeriodicTask(
                store.state.nextCloudCredentials,
                periodicTask,
                () => onSuccess(context, 'Successfully updated periodic task ${periodicTask.name}'),
                () => onError(context, 'Failed to update periodic task ${periodicTask.name}')));
          }
        },
        delete: (BuildContext context, PeriodicTask periodicTask) {
          deleteDialog(store, context, periodicTask);
        },
        validatePeriodicTask: (PeriodicTask periodicTask) {
          return store.state.periodicTaskList.firstWhere((periodicTask) => periodicTask.name == periodicTask.name, orElse: () => null) != null;
        });
  }

  static void deleteDialog(Store<AppState> store, BuildContext context, PeriodicTask periodicTask) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text('Delete ${periodicTask.name}?'),
          content: Text('Do you really want to delete ${periodicTask.name}?'),
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
                store.dispatch(deletePeriodicTask(
                    store.state.nextCloudCredentials,
                    periodicTask,
                    () => onSuccess(context, 'Successfully deleted periodic task ${periodicTask.name}'),
                    () => onError(context, 'Failed to delete periodic task ${periodicTask.name}')));
                Navigator.pop(context, true);
              },
            ),
          ],
        );
      },
    );
  }
}
