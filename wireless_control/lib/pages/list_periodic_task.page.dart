import 'dart:async';
import 'dart:math' as math;
import 'package:connectivity/connectivity.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:redux/redux.dart';
import 'package:wireless_control/actions/area.actions.dart';
import 'package:wireless_control/actions/periodic_task.actions.dart';
import 'package:wireless_control/actions/wireless_socket.actions.dart';
import 'package:wireless_control/constants/color.constants.dart';
import 'package:wireless_control/enums/app_theme.enum.dart';
import 'package:wireless_control/helper/icon.helper.dart';
import 'package:wireless_control/middleware/area.thunk_action.dart';
import 'package:wireless_control/middleware/periodic_task.thunk_action.dart';
import 'package:wireless_control/middleware/wireless_socket.thunk_action.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/area.model.dart';
import 'package:wireless_control/models/periodic_task.model.dart';
import 'package:wireless_control/models/wireless_socket.model.dart';
import 'package:wireless_control/presentation/periodic_task_card.dart';

class ListPeriodicTaskPage extends StatefulWidget {
  static String tag = 'list-periodic-task-page';

  final Store<AppState> store;

  ListPeriodicTaskPage(this.store);

  @override
  State createState() => new ListPeriodicTaskPageState(store);
}

class ListPeriodicTaskPageState extends State<ListPeriodicTaskPage> with TickerProviderStateMixin {
  final Connectivity _connectivity = Connectivity();
  StreamSubscription<ConnectivityResult> _connectivitySubscription;

  final Store<AppState> store;
  AnimationController _animationController;
  static const List<IconData> icons = const [
    Icons.add_location,
    Icons.add_circle,
    Icons.add_alarm
  ];

  ListPeriodicTaskPageState(this.store);

  @override
  void initState() {
    super.initState();
    initConnectivity();
    _connectivitySubscription = _connectivity.onConnectivityChanged.listen(_updateConnectionStatus);
    _animationController = new AnimationController(vsync: this, duration: const Duration(milliseconds: 250));
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

  ListView _buildList(context) {
    return new ListView.builder(
      itemCount: store.state.periodicTaskListWirelessSocket != null ? store.state.periodicTaskListWirelessSocket.length : 0,
      itemBuilder: (context, index) => new PeriodicTaskCard(store.state.periodicTaskListWirelessSocket[index], store),
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
        actions: <Widget>[
          DropdownButton<String>(
            hint: Text("Please choose a filter", style: TextStyle(color: ColorConstants.Hint)),
            value: store.state.selectedWirelessSocket != null ? store.state.selectedWirelessSocket.name : '',
            items: (store.state.wirelessSocketListAll != null ? store.state.wirelessSocketListAll : new List<WirelessSocket>()).map((WirelessSocket wirelessSocket) {
              return new DropdownMenuItem<String>(
                value: wirelessSocket.name,
                child: new Row(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: <Widget>[
                    IconButton(
                      icon: Icon(fromString(wirelessSocket.icon), color: ColorConstants.IconDark,),
                      onPressed: () {
                        widget.store.dispatch(new WirelessSocketSelectSuccessful(wirelessSocket: wirelessSocket));
                        Navigator.pushNamed(context, '/details-wireless-socket');
                      },
                    ),
                    Text(wirelessSocket.name, style: TextStyle(color: ColorConstants.TextDark)),
                  ],
                ),
              );
            }).toList(),
            onChanged: (wirelessSocketName) {
              var wirelessSocketSelected = store.state.wirelessSocketListAll.firstWhere((WirelessSocket wirelessSocket) => wirelessSocket.name == wirelessSocketName);
              store.dispatch(new WirelessSocketSelectSuccessful(wirelessSocket: wirelessSocketSelected));
            },
          ),
          IconButton(
            icon: Icon(Icons.sync),
            onPressed: () {
              store.dispatch(loadAreas(store.state.nextCloudCredentials));
              store.dispatch(loadWirelessSockets(store.state.nextCloudCredentials));
              store.dispatch(loadPeriodicTasks(store.state.nextCloudCredentials));
            },
          ),
          IconButton(
            icon: Icon(FontAwesomeIcons.cogs),
            onPressed: () {
              Navigator.pushNamed(context, '/settings');
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
                      store.dispatch(new AreaAdd(area: new Area()));
                      Navigator.pushNamed(context, '/details-area');
                      break;
                    case 1: // WirelessSocket
                      store.dispatch(new WirelessSocketAdd(wirelessSocket: new WirelessSocket()));
                      Navigator.pushNamed(context, '/details-wireless-socket');
                      break;
                    case 2: // PeriodicTask
                      var periodicTask = new PeriodicTask();
                      periodicTask.wirelessSocketId = store.state.selectedWirelessSocket.id;
                      store.dispatch(new PeriodicTaskAdd(periodicTask: periodicTask));
                      Navigator.pushNamed(context, '/details-periodic-task');
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
            color: widget.store.state.theme == AppTheme.Light ? ColorConstants.BackgroundLight : ColorConstants.BackgroundDark,
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
