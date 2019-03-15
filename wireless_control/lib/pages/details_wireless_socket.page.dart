import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:redux/redux.dart';
import 'package:wireless_control/constants/color.constants.dart';
import 'package:wireless_control/enums/state_action.enum.dart';
import 'package:wireless_control/helper/icon.helper.dart';
import 'package:wireless_control/middleware/wireless_socket.thunk_action.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/wireless_socket.model.dart';
import 'package:wireless_control/presentation/details-widgets.dart';
import 'package:wireless_control/utils/actions.util.dart';

class DetailsWirelessSocketPage extends StatefulWidget {
  static String tag = 'details-wireless-socket-page';

  final WirelessSocket wirelessSocket;

  DetailsWirelessSocketPage(this.wirelessSocket);

  @override
  _DetailsWirelessSocketPageState createState() => new _DetailsWirelessSocketPageState();
}

class _DetailsWirelessSocketPageState extends State<DetailsWirelessSocketPage> {
  final _formKey = GlobalKey<FormState>();
  StateAction stateAction;

  @override
  void initState() {
    super.initState();
    this.stateAction = widget.wirelessSocket.deletable == 1
        ? widget.wirelessSocket.name == ""
          ? StateAction.Add
          : StateAction.Update
        : StateAction.Readonly;
  }

  @override
  void dispose() {
    super.dispose();
  }

  Widget get nameTextFormField {
    return getTextFormField(widget.wirelessSocket.name, 'Name',
            (value) {if (value.isEmpty) {return 'Name is required';}},
            (String value) {widget.wirelessSocket.name = value;});
  }

  Widget get codeTextFormField {
    return getTextFormField(widget.wirelessSocket.code, 'Code',
            (value) {
      if (value.isEmpty) {return 'Code is required';}
      if (!new RegExp(r"^([01]{5}[ABCDE]{1})$").hasMatch(value)) {return 'Invalid code (Must be of format 11001A)';}},
            (String value) {widget.wirelessSocket.code = value;});
  }

  Widget areaTextFormField(_ViewModel viewModel) {
    return getTextFormField(widget.wirelessSocket.area, 'Area',
            (value) {
      if (value.isEmpty) {return 'Area is required';}
      if (!viewModel.validateArea(value)) {return 'Area is not valid (Must exist)';}},
            (String value) {widget.wirelessSocket.area = value;});
  }

  Widget get descriptionTextFormField {
    return getTextFormField(widget.wirelessSocket.description, 'Description',
            (value) {},
            (String value) {widget.wirelessSocket.description = value;});
  }

  Widget get iconTextFormField {
    return getTextFormField(widget.wirelessSocket.icon, 'Icon',
            (value) {if (value.isEmpty) {return 'Icon is required';}},
            (String value) {widget.wirelessSocket.icon = value;});
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
                    ? Text('Details for ${widget.wirelessSocket.name}')
                    : Text('Add wireless socket'),
              ),
              body: ListView(
                children: <Widget>[
                  Row(
                    children: <Widget>[
                      Container(
                        width: pageSize.width,
                        height: pageSize.height * 0.25,
                        color: ColorConstants.BackgroundLight,
                        alignment: Alignment.center,
                        child: getDetailsIcon(fromString(widget.wirelessSocket.icon)),
                      )
                    ],
                  ),
                  Row(
                    children: <Widget>[
                      Container(
                          width: pageSize.width,
                          height: pageSize.height * 0.45,
                          color: ColorConstants.BackgroundLight,
                          alignment: Alignment.center,
                          child: Center(
                            child: ListView(
                              padding: EdgeInsets.only(left: 24.0, right: 24.0),
                              children: <Widget>[
                                nameTextFormField,
                                SizedBox(height: 12.0),
                                codeTextFormField,
                                SizedBox(height: 12.0),
                                areaTextFormField(viewModel),
                                SizedBox(height: 12.0),
                                descriptionTextFormField,
                                SizedBox(height: 12.0),
                                iconTextFormField,
                              ],
                            ),
                          )),
                    ],
                  ),
                  Row(
                    children: <Widget>[
                      Container(
                        width: pageSize.width,
                        height: pageSize.height * 0.15,
                        color: ColorConstants.BackgroundLight,
                        alignment: Alignment.center,
                        child: ListView(
                          padding: EdgeInsets.only(left: 24.0, right: 24.0),
                          children: <Widget>[
                            this.stateAction != StateAction.Readonly ?
                            Padding(
                              padding: EdgeInsets.symmetric(vertical: 4.0),
                              child: RaisedButton(
                                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24),),
                                onPressed: () {
                                  if (_formKey.currentState.validate()) {
                                    _formKey.currentState.save();
                                    viewModel.save(context, widget.wirelessSocket);
                                  }
                                },
                                padding: EdgeInsets.all(12),
                                color: ColorConstants.ButtonSubmit,
                                child: Text('Save', style: TextStyle(color: ColorConstants.TextLight)),
                              ),
                            ) : Padding(padding: EdgeInsets.symmetric(vertical: 4.0)),
                            this.stateAction == StateAction.Update ?
                            Padding(
                              padding: EdgeInsets.symmetric(vertical: 4.0),
                              child: RaisedButton(
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(24),
                                ),
                                onPressed: () => viewModel.delete(context, widget.wirelessSocket),
                                padding: EdgeInsets.all(12),
                                color: ColorConstants.ButtonDelete,
                                child: Text('Delete', style: TextStyle(color: ColorConstants.TextLight)),
                              ),
                            ) : Padding(padding: EdgeInsets.symmetric(vertical: 4.0)),
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
  final Function save;
  final Function delete;
  final Function validateArea;

  _ViewModel({this.save, this.delete, this.validateArea});

  static _ViewModel fromStore(Store<AppState> store) {
    return _ViewModel(
        save: (BuildContext context, WirelessSocket wirelessSocket) {
          if (wirelessSocket.id == -1) {
            store.dispatch(addWirelessSocket(
                store.state.nextCloudCredentials,
                wirelessSocket,
                () => onSuccess(context, 'Successfully added wireless socket ${wirelessSocket.name}'),
                () => onError(context, 'Failed to add wireless socket ${wirelessSocket.name}')));
          } else {
            store.dispatch(updateWirelessSocket(
                store.state.nextCloudCredentials,
                wirelessSocket,
                () => onSuccess(context, 'Successfully updated wireless socket ${wirelessSocket.name}'),
                () => onError(context, 'Failed to updated wireless socket ${wirelessSocket.name}')));
          }
        },
        delete: (BuildContext context, WirelessSocket wirelessSocket) {
          deleteDialog(store, context, wirelessSocket);
        },
        validateArea: (String areaName) {
          return store.state.areaList.singleWhere((area) => area.name == areaName, orElse: () => null) != null;
        });
  }

  static void deleteDialog(Store<AppState> store, BuildContext context, WirelessSocket wirelessSocket) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text('Delete ${wirelessSocket.name}?'),
          content: Text('Do you really want to delete ${wirelessSocket.name}?'),
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
                store.dispatch(deleteWirelessSocket(
                    store.state.nextCloudCredentials,
                    wirelessSocket,
                    () => onSuccess(context, 'Successfully deleted wireless socket ${wirelessSocket.name}'),
                    () => onError(context, 'Failed to delete wireless socket ${wirelessSocket.name}')));
                Navigator.pop(context, true);
              },
            ),
          ],
        );
      },
    );
  }
}
