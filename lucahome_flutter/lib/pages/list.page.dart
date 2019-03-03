import 'package:flutter/material.dart';
import 'package:lucahome_flutter/actions/wireless_socket.actions.dart';
import 'package:lucahome_flutter/models/app_state.model.dart';
import 'package:lucahome_flutter/models/wireless_socket.model.dart';
import 'package:lucahome_flutter/presentation/wireless_socket_card.dart';
import 'package:redux/redux.dart';

class ListPage extends StatelessWidget {
  static String tag = 'list-page';

  final Store<AppState> store;

  ListPage(this.store);

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
    // String selectedAreaName;

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
          /*IconButton(
            icon: Icon(Icons.add),
            onPressed: () {
              // TODO add area
            },
          ),*/
        ],
      ),
      floatingActionButton: new FloatingActionButton(
          child: Icon(Icons.add),
          onPressed: () {
            store.dispatch(new WirelessSocketSelectSuccessful(wirelessSocket: new WirelessSocket()));
            Navigator.pushNamed(context, '/details');
          }),
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
              child: _buildList(context),
            ),
          ),
        ],
      ),
    ));
  }
}
