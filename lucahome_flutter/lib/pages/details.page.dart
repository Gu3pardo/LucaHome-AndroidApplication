import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:lucahome_flutter/helper/icon.helper.dart';
import 'package:lucahome_flutter/middleware/wireless_socket.thunk_action.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/models/wireless_socket.model.dart';
import 'package:redux/redux.dart';

class DetailsPage extends StatefulWidget {
  static String tag = 'details-page';

  final WirelessSocket wirelessSocket;

  DetailsPage(this.wirelessSocket);

  @override
  _DetailsPageState createState() => new _DetailsPageState();
}

class _DetailsPageState extends State<DetailsPage> {
  final _formKey = GlobalKey<FormState>();

  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  Widget get icon {
    return new Icon(
      fromString(widget.wirelessSocket.icon),
      size: 150,
      color: Colors.white,
    );
  }

  Widget get nameTextFormField {
    return new TextFormField(
      keyboardType: TextInputType.text,
      autofocus: false,
      initialValue: widget.wirelessSocket.name,
      decoration: InputDecoration(
        hintText: 'Name',
        contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
      ),
      style: TextStyle(color: Colors.white),
      validator: (value) {
        if (value.isEmpty) {
          return 'Name is required';
        }
      },
      onSaved: (String value) {
        widget.wirelessSocket.name = value;
      },
    );
  }

  Widget get codeTextFormField {
    return new TextFormField(
      keyboardType: TextInputType.text,
      autofocus: false,
      initialValue: widget.wirelessSocket.code,
      decoration: InputDecoration(
        hintText: 'Code',
        contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
      ),
      style: TextStyle(color: Colors.white),
      validator: (value) {
        if (value.isEmpty) {
          return 'Code is required';
        }

        if (!new RegExp(r"^([01]{5}[ABCDE]{1})$").hasMatch(value)) {
          return 'Invalid code (Must be of format 11001A)';
        }
      },
      onSaved: (String value) {
        widget.wirelessSocket.code = value;
      },
    );
  }

  Widget areaTextFormField(_ViewModel viewModel) {
    return new TextFormField(
      keyboardType: TextInputType.text,
      autofocus: false,
      initialValue: widget.wirelessSocket.area,
      decoration: InputDecoration(
        hintText: 'Area',
        contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
      ),
      style: TextStyle(color: Colors.white),
      validator: (value) {
        if (value.isEmpty) {
          return 'Area is required';
        }
        if(!viewModel.validateArea(value)){
          return 'Area is not valid (Must exis)';
        }
      },
      onSaved: (String value) {
        widget.wirelessSocket.area = value;
      },
    );
  }

  Widget get descriptionTextFormField {
    return new TextFormField(
      keyboardType: TextInputType.text,
      autofocus: false,
      initialValue: widget.wirelessSocket.description,
      decoration: InputDecoration(
        hintText: 'Description',
        contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
      ),
      style: TextStyle(color: Colors.white),
      validator: (value) {},
      onSaved: (String value) {
        widget.wirelessSocket.description = value;
      },
    );
  }

  Widget get iconTextFormField {
    return new TextFormField(
      keyboardType: TextInputType.text,
      autofocus: false,
      initialValue: widget.wirelessSocket.icon,
      decoration: InputDecoration(
        hintText: 'Icon',
        contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
      ),
      style: TextStyle(color: Colors.white),
      validator: (value) {
        if (value.isEmpty) {
          return 'Icon is required';
        }
      },
      onSaved: (String value) {
        widget.wirelessSocket.icon = value;
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    var pageSize = MediaQuery.of(context).size;

    return new StoreConnector<AppState, _ViewModel>(
      converter: _ViewModel.fromStore,
      builder: (BuildContext context, _ViewModel viewModel) {
        return Form(
            key: _formKey,
            child: new Scaffold(
              appBar: new AppBar(
                backgroundColor: Color(0xFF3744B0),
                title: new Text('Details for ${widget.wirelessSocket.name}'),
              ),
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
                      child: new Center(
                        child: ListView(
                          padding: EdgeInsets.only(left: 24.0, right: 24.0),
                          children: <Widget>[
                            SizedBox(height: 24.0),
                            icon,
                            SizedBox(height: 24.0),
                            nameTextFormField,
                            SizedBox(height: 24.0),
                            codeTextFormField,
                            SizedBox(height: 24.0),
                            areaTextFormField(viewModel),
                            SizedBox(height: 24.0),
                            descriptionTextFormField,
                            SizedBox(height: 24.0),
                            iconTextFormField,
                            SizedBox(height: 24.0),
                            Padding(
                              padding: EdgeInsets.symmetric(vertical: 16.0),
                              child: RaisedButton(
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(24),
                                ),
                                onPressed: () {
                                  if (_formKey.currentState.validate()) {
                                    _formKey.currentState.save();
                                    viewModel.save(widget.wirelessSocket);
                                  }
                                },
                                padding: EdgeInsets.all(12),
                                color: Colors.lightBlueAccent,
                                child: Text('Save',
                                    style: TextStyle(color: Colors.white)),
                              ),
                            )
                          ],
                        ),
                      )),
                ],
              ),
            ));
      },
    );
  }
}

class _ViewModel {
  final Function save;
  final Function validateArea;

  _ViewModel({this.save, this.validateArea});

  static _ViewModel fromStore(Store<AppState> store) {
    return new _ViewModel(save: (WirelessSocket wirelessSocket) {
      if (wirelessSocket.id == -1) {
        store.dispatch(addWirelessSocket(
            store.state.nextCloudCredentials, wirelessSocket));
      } else {
        store.dispatch(updateWirelessSocket(
            store.state.nextCloudCredentials, wirelessSocket));
      }
    }, validateArea: (String areaName) {
      return store.state.areaList.singleWhere((area) => area.name == areaName, orElse: () => null) != null;
    });
  }
}
