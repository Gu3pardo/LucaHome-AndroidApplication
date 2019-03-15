import 'package:flutter/material.dart';
import 'package:redux/redux.dart';
import 'package:wave/config.dart';
import 'package:wave/wave.dart';
import 'package:wireless_control/actions/wireless_socket.actions.dart';
import 'package:wireless_control/constants/color.constants.dart';
import 'package:wireless_control/helper/icon.helper.dart';
import 'package:wireless_control/middleware/wireless_socket.thunk_action.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/wireless_socket.model.dart';

class WirelessSocketCard extends StatefulWidget {
  final WirelessSocket wirelessSocket;
  final Store<AppState> store;

  WirelessSocketCard(this.wirelessSocket, this.store);

  @override
  WirelessSocketCardState createState() {
    return new WirelessSocketCardState();
  }
}

class WirelessSocketCardState extends State<WirelessSocketCard> {

  Widget wirelessSocketCard(BuildContext context, Size pageSize) {
    return new Positioned(
      right: 0.0,
      child: new Container(
        width: pageSize.width * 0.65,
        height: 115.0,
        child: new Card(
          color: ColorConstants.CardBackgroundLightTransparent,
          child: InkWell(
              splashColor: ColorConstants.ButtonSubmit,
              onTap: () {
                widget.store.dispatch(new WirelessSocketSelectSuccessful(wirelessSocket: widget.wirelessSocket));
                Navigator.pushNamed(context, '/details');
              },
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
                    new Text(widget.wirelessSocket.name, style: Theme.of(context).textTheme.headline),
                    new Text(widget.wirelessSocket.code, style: Theme.of(context).textTheme.subhead),
                    new Text(widget.wirelessSocket.area, style: Theme.of(context).textTheme.body1),
                    new Text(widget.wirelessSocket.description, style: Theme.of(context).textTheme.body1)
                  ],
                ),
              )),
        ),
      ),
    );
  }

  WaveWidget waveWidgetOff() {
    return WaveWidget(
      config: CustomConfig(
        gradients: ColorConstants.WaveOff,
        durations: [35000, 19440, 10800, 6000],
        heightPercentages: [0.70, 0.73, 0.75, 0.80],
        gradientBegin: Alignment.bottomLeft,
        gradientEnd: Alignment.topRight,
      ),
      backgroundColor: Colors.transparent,
      size: Size(double.infinity, double.infinity),
      waveAmplitude: 0,
    );
  }

  WaveWidget waveWidgetOn() {
    return WaveWidget(
      config: CustomConfig(
        gradients: ColorConstants.WaveOn,
        durations: [35000, 19440, 10800, 6000],
        heightPercentages: [0.10, 0.13, 0.15, 0.20],
        gradientBegin: Alignment.bottomLeft,
        gradientEnd: Alignment.topRight,
      ),
      backgroundColor: Colors.transparent,
      size: Size(double.infinity, double.infinity),
      waveAmplitude: 0,
    );
  }

  @override
  Widget build(BuildContext context) {
    var pageSize = MediaQuery.of(context).size;

    return new Card(
      color: ColorConstants.CardBackgroundLight,
      margin: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 8.0),
      child: new Padding(
        padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 8.0),
        child: new Container(
          height: 115.0,
          child: new Stack(
            children: <Widget>[
              widget.wirelessSocket.state == 1 ? waveWidgetOn() : waveWidgetOff(),
              wirelessSocketCard(context, pageSize),
              new Positioned(
                  top: 5,
                  left: 5,
                  bottom: 5,
                  child: FlatButton(
                    color: Color.fromARGB(0, 0, 0, 0),
                    splashColor: ColorConstants.ButtonSubmit,
                    highlightColor: ColorConstants.ButtonSubmitHighlight,
                    padding: const EdgeInsets.all(10.0),
                    onPressed: () {
                      widget.wirelessSocket.state = widget.wirelessSocket.state == 1 ? 0 : 1;
                      widget.store.dispatch(updateWirelessSocket(widget.store.state.nextCloudCredentials, widget.wirelessSocket));
                    },
                    child: new Icon(
                      fromString(widget.wirelessSocket.icon),
                      size: 50,
                      color: ColorConstants.IconDark,
                    ),
                  ))],
          ),
        ),
      ),
    );
  }
}
