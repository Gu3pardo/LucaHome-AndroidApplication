import 'package:flutter/material.dart';
import 'package:lucahome_flutter/models/wireless_socket.model.dart';
import 'package:lucahome_flutter/presentation/wireless_socket_card.dart';

class ListPage extends StatelessWidget {
  final List<WirelessSocket> wirelessSocketList;

  ListPage(this.wirelessSocketList);

  ListView _buildList(context) {
    return new ListView.builder(
      itemCount: wirelessSocketList.length,
      itemBuilder: (context, index) => new WirelessSocketCard(wirelessSocketList[index]),
    );
  }

  @override
  Widget build(BuildContext context) {
    return _buildList(context);
  }
}
