import 'package:flutter/material.dart';
import 'package:lucahome_flutter/helper/icon.helper.dart';
import 'package:lucahome_flutter/middleware/wireless_socket.thunk_action.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/models/wireless_socket.model.dart';
import 'package:redux/redux.dart';

class WirelessSocketCard extends StatefulWidget {
  final WirelessSocket wirelessSocket;
  final Store<AppState> store;

  WirelessSocketCard(this.wirelessSocket, this.store);

  @override
  WirelessSocketCardState createState() {
    return new WirelessSocketCardState(wirelessSocket, store);
  }
}

class WirelessSocketCardState extends State<WirelessSocketCard> {
  WirelessSocket wirelessSocket;
  final Store<AppState> store;

  WirelessSocketCardState(
      this.wirelessSocket, this.store);

  Widget wirelessSocketCard(Size pageSize) {
    return new Positioned(
      right: 0.0,
      child: new Container(
        width: pageSize.width * 0.75,
        height: 115.0,
        child: new Card(
          color: Colors.white70,
          child: InkWell(
              splashColor: Colors.lightBlue,
              onTap: () => showDetailsPage(wirelessSocket),
              child: new Padding(
                padding: const EdgeInsets.only(
                  top: 8.0,
                  bottom: 8.0,
                  left: 8.0,
                  right: 8.0,
                ),
                child: new Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: <Widget>[
                    new Text(widget.wirelessSocket.name,
                        style: Theme.of(context).textTheme.headline),
                    new Text(widget.wirelessSocket.code,
                        style: Theme.of(context).textTheme.subhead),
                    new Text(widget.wirelessSocket.area,
                        style: Theme.of(context).textTheme.body1),
                    new Text(widget.wirelessSocket.description,
                        style: Theme.of(context).textTheme.body1)
                  ],
                ),
              )),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    var pageSize = MediaQuery.of(context).size;

    return new Card(
      color: Colors.white,
      margin: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 8.0),
      child: new Padding(
        padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 8.0),
        child: new Container(
          height: 115.0,
          child: new Stack(
            children: <Widget>[
              wirelessSocketCard(pageSize),
              new Positioned(
                  top: 7.5,
                  left: 7.5,
                  bottom: 7.5,
                  child: IconButton(
                      color: wirelessSocket.state == 1 ? Colors.green : Colors.red,
                      icon: new Icon(
                        fromString(widget.wirelessSocket.icon),
                        size: 50,
                      ),
                      onPressed: () {
                        wirelessSocket.state = wirelessSocket.state == 1 ? 0 : 1;
                        print("onPressed");
                        print(wirelessSocket);
                        store.dispatch(updateWirelessSocket(store.state.nextCloudCredentials, wirelessSocket));
                      })),
            ],
          ),
        ),
      ),
    );
  }

  showDetailsPage(WirelessSocket wirelessSocket) {
    print("showDetailsPage: " + wirelessSocket.toString());
    // TODO Navigate to details page using router and store
  }
}
