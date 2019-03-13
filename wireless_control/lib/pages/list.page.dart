import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:redux/redux.dart';
import 'package:wireless_control/actions/area.actions.dart';
import 'package:wireless_control/actions/wireless_socket.actions.dart';
import 'package:wireless_control/constants/color.constants.dart';
import 'package:wireless_control/middleware/area.thunk_action.dart';
import 'package:wireless_control/middleware/wireless_socket.thunk_action.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/area.model.dart';
import 'package:wireless_control/models/wireless_socket.model.dart';
import 'package:wireless_control/presentation/wireless_socket_card.dart';

class ListPage extends StatefulWidget {
  static String tag = 'list-page';

  final Store<AppState> store;

  ListPage(this.store);

  @override
  State createState() => new ListPageState(store);
}

class ListPageState extends State<ListPage> with TickerProviderStateMixin {
  final Store<AppState> store;
  AnimationController _animationController;
  static const List<IconData> icons = const [
    Icons.add_location,
    Icons.add_circle
  ];

  ListPageState(this.store);

  @override
  void initState() {
    _animationController = new AnimationController(vsync: this, duration: const Duration(milliseconds: 250));
    super.initState();
  }

  ListView _buildList(context) {
    return new ListView.builder(
      itemCount: store.state.wirelessSocketListArea != null ? store.state.wirelessSocketListArea.length : 0,
      itemBuilder: (context, index) => new WirelessSocketCard(store.state.wirelessSocketListArea[index], store),
    );
  }

  @override
  Widget build(BuildContext context) {
    var pageSize = MediaQuery.of(context).size;

    return new Container(
        child: new Scaffold(
      appBar: new AppBar(
        backgroundColor: ColorConstants.AppBar,
        automaticallyImplyLeading: false,
        title: new Text('Wireless Sockets'),
        actions: <Widget>[
          DropdownButton<String>(
            hint: Text("Please choose an area to filter"),
            value: store.state.selectedArea.name,
            items: (store.state.areaList != null ? store.state.areaList : new List<Area>()).map((Area area) {
              return new DropdownMenuItem<String>(
                value: area.name,
                child: new Text(area.name),
              );
            }).toList(),
            onChanged: (areaName) {
              var areaSelected = store.state.areaList.firstWhere((Area area) => area.name == areaName);
              store.dispatch(new AreaSelectSuccessful(area: areaSelected));
            },
          ),
          IconButton(
            icon: Icon(Icons.sync),
            onPressed: () {
              store.dispatch(loadAreas(store.state.nextCloudCredentials));
              store.dispatch(loadWirelessSockets(store.state.nextCloudCredentials));
            },
          ),
        ],
      ),
      floatingActionButton: new Column(
        mainAxisSize: MainAxisSize.min,
        children: new List.generate(icons.length, (int index) {
          Widget child = new Container(
            height: 75.0,
            width: 50.0,
            alignment: FractionalOffset.topCenter,
            child: new ScaleTransition(
              scale: new CurvedAnimation(
                parent: _animationController,
                curve: new Interval(0.0, 1.0 - index / icons.length / 2.0, curve: Curves.easeOut),
              ),
              child: new FloatingActionButton(
                heroTag: null,
                backgroundColor: ColorConstants.BackgroundLight,
                child: new Icon(icons[index], color: ColorConstants.ButtonSubmit),
                tooltip: index == 0 ? 'Area' : 'WirelessSocket',
                onPressed: () {
                  switch (index) {
                    case 0: // Area
                      break;
                    case 1: // WirelessSocket
                      store.dispatch(new WirelessSocketSelectSuccessful(wirelessSocket: new WirelessSocket()));
                      Navigator.pushNamed(context, '/details');
                      break;
                    default:
                      break;
                  }
                },
              ),
            ),
          );
          return child;
        }).toList()
          ..add(
            new FloatingActionButton(
              heroTag: null,
              child: new AnimatedBuilder(
                animation: _animationController,
                builder: (BuildContext context, Widget child) {
                  return new Transform(
                    transform: new Matrix4.rotationZ(_animationController.value * 0.5 * math.pi),
                    alignment: FractionalOffset.center,
                    child: new Icon(_animationController.isDismissed ? Icons.add : Icons.close),
                  );
                },
              ),
              onPressed: () {
                _animationController.isDismissed ? _animationController.forward() : _animationController.reverse();
              },
            ),
          ),
      ),
      body: new Stack(
        children: <Widget>[
          new Container(
            color: ColorConstants.BackgroundLight,
            alignment: Alignment.center,
            width: pageSize.width,
            height: pageSize.height,
            child: new Center(
              child: _buildList(context),
            ),
          ),
        ],
      ),
    ));
  }
}
