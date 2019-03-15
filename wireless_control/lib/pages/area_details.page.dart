import 'package:flutter/material.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:redux/redux.dart';
import 'package:wireless_control/constants/color.constants.dart';
import 'package:wireless_control/enums/state_action.enum.dart';
import 'package:wireless_control/middleware/area.thunk_action.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/area.model.dart';
import 'package:wireless_control/presentation/details-widgets.dart';

class AreaDetailsPage extends StatefulWidget {
  static String tag = 'area-details-page';

  final Area area;

  AreaDetailsPage(this.area);

  @override
  _AreaDetailsPageState createState() => new _AreaDetailsPageState();
}

class _AreaDetailsPageState extends State<AreaDetailsPage> {
  final _formKey = GlobalKey<FormState>();
  StateAction stateAction;

  @override
  void initState() {
    super.initState();
    this.stateAction = widget.area.deletable == 1
        ? widget.area.name == ""
          ? StateAction.Add
          : StateAction.Update
        : StateAction.Readonly;
  }

  @override
  void dispose() {
    super.dispose();
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
                    ? Text('Details for ${widget.area.name}')
                    : Text('Add area'),
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
                        child: getDetailsIcon(FontAwesomeIcons.map),
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
                                getTextFormField(widget.area.name, 'Name',
                                        (value) {if (value.isEmpty) {return 'Name is required';}},
                                        (String value) {widget.area.name = value; widget.area.filter = value;}),
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
                              padding: EdgeInsets.symmetric(vertical: 2.0),
                              child: RaisedButton(
                                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24),),
                                onPressed: () {
                                  if (_formKey.currentState.validate()) {
                                    _formKey.currentState.save();
                                    viewModel.save(widget.area);
                                  }
                                },
                                padding: EdgeInsets.all(12),
                                color: ColorConstants.ButtonSubmit,
                                child: Text('Save', style: TextStyle(color: ColorConstants.TextLight)),
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
                                child: Text('Delete', style: TextStyle(color: ColorConstants.TextLight)),
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
  final Function save;
  final Function delete;
  final Function validateArea;

  _ViewModel({this.save, this.delete, this.validateArea});

  static _ViewModel fromStore(Store<AppState> store) {
    return _ViewModel(
        save: (Area area) {
          if (area.id == -1) {
            store.dispatch(addArea(store.state.nextCloudCredentials, area));
          } else {
            store.dispatch(updateArea(store.state.nextCloudCredentials, area));
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
                store.dispatch(deleteArea(store.state.nextCloudCredentials, area));
                Navigator.pop(context, true);
              },
            ),
          ],
        );
      },
    );
  }
}
