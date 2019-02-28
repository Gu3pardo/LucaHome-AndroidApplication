import 'package:flutter/material.dart';
import 'package:lucahome_flutter/models/wireless_socket.model.dart';
import 'package:lucahome_flutter/presentation/wireless_socket_card.dart';

class ListPage extends StatelessWidget {
  final List<WirelessSocket> wirelessSocketList;

  ListPage(this.wirelessSocketList);

  ListView _buildList(context) {
    return new ListView.builder(
      itemCount: wirelessSocketList.length,
      itemBuilder: (context, index) =>
          new WirelessSocketCard(wirelessSocketList[index]),
    );
  }

  @override
  Widget build(BuildContext context) {
    var pageSize = MediaQuery.of(context).size;

    return new Stack(
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
    );
  }
}
