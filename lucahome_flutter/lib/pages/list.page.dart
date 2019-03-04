import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:lucahome_flutter/actions/wireless_socket.actions.dart';
import 'package:lucahome_flutter/middleware/area.thunk_action.dart';
import 'package:lucahome_flutter/middleware/wireless_socket.thunk_action.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/models/wireless_socket.model.dart';
import 'package:lucahome_flutter/presentation/wireless_socket_card.dart';
import 'package:redux/redux.dart';

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
    _animationController = new AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 250),
    );
    super.initState();
  }

  ListView _buildList(context) {
    return new ListView.builder(
      itemCount: store.state.wirelessSocketList != null
          ? store.state.wirelessSocketList.length
          : 0,
      itemBuilder: (context, index) =>
          new WirelessSocketCard(store.state.wirelessSocketList[index], store),
    );
  }

  @override
  Widget build(BuildContext context) {
    var pageSize = MediaQuery.of(context).size;
    var backgroundColor = Theme.of(context).cardColor;
    var foregroundColor = Theme.of(context).accentColor;

    return new Container(
        child: new Scaffold(
      appBar: new AppBar(
        backgroundColor: Color(0xFF3744B0),
        automaticallyImplyLeading: false,
        title: new Text('Wireless Sockets'),
        actions: <Widget>[
          /*DropdownButton<String>(
            hint: Text("Please choose an area to filter"),
            value: selectedAreaName,
            items: (store.state.areaList != null ? store.state.areaList : new List<Area>()).map((Area area) {
              return new DropdownMenuItem<String>(
                value: area.name,
                child: new Text(area.name),
              );
            }).toList(),
            onChanged: (areaName) {
              selectedAreaName = areaName;
              // TODO filter for area
            },
          ),*/
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
                backgroundColor: backgroundColor,
                child: new Icon(icons[index], color: foregroundColor),
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
                    child: new Icon(_animationController.isDismissed
                        ? Icons.add
                        : Icons.close),
                  );
                },
              ),
              onPressed: () {
                if (_animationController.isDismissed) {
                  _animationController.forward();
                } else {
                  _animationController.reverse();
                }
              },
            ),
          ),
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
            child: new Center(child: _buildList(context),
            ),
          ),
        ],
      ),
    ));
  }
}
