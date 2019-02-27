import 'package:flutter/material.dart';
import 'package:lucahome_flutter/helper/icon.helper.dart';
import 'package:lucahome_flutter/models/wireless_socket.model.dart';
import 'package:lucahome_flutter/pages/details.page.dart';

class WirelessSocketCard extends StatefulWidget {
  final WirelessSocket wirelessSocket;

  WirelessSocketCard(this.wirelessSocket);

  @override
  WirelessSocketCardState createState() {
    return new WirelessSocketCardState(wirelessSocket);
  }
}

class WirelessSocketCardState extends State<WirelessSocketCard> {
  WirelessSocket wirelessSocket;

  WirelessSocketCardState(this.wirelessSocket);

  Widget get image {
    var icon = new Hero(
      tag: widget.wirelessSocket.name,
      child: new Container(
        color: Colors.white,
        child: new SizedBox.expand(
          child: new Hero(
            tag: widget.wirelessSocket.icon,
            child: new Icon(
              fromString(widget.wirelessSocket.icon),
              size: 100.0,
            ),
          ),
        ),
      ),
    );

    var placeholder = new Container(
        width: 100.0,
        height: 100.0,
        decoration: new BoxDecoration(
          shape: BoxShape.circle,
          gradient: new LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Colors.black54, Colors.black, Colors.blueGrey[600]],
          ),
        ),
        alignment: Alignment.center,
        child: new Text(
          'Wireless Socket',
          textAlign: TextAlign.center,
        ));

    var crossFade = new AnimatedCrossFade(
      firstChild: placeholder,
      secondChild: icon,
      crossFadeState: wirelessSocket.icon == ""
          ? CrossFadeState.showFirst
          : CrossFadeState.showSecond,
      duration: new Duration(milliseconds: 1000),
    );

    return crossFade;
  }

  Widget get wirelessSocketCard {
    return new Positioned(
      right: 0.0,
      child: new Container(
        width: 290.0,
        height: 115.0,
        child: new Card(
          color: Colors.blueAccent,
          child: new Padding(
            padding: const EdgeInsets.only(
              top: 8.0,
              bottom: 8.0,
              left: 64.0,
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
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return new InkWell(
      onTap: () => showDetailsPage(),
      child: new Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
        child: new Container(
          height: 115.0,
          child: new Stack(
            children: <Widget>[
              wirelessSocketCard,
              new Positioned(top: 7.5, child: image),
            ],
          ),
        ),
      ),
    );
  }

  showDetailsPage() {
    // TODO Navigate to details page using router and store
  }
}
