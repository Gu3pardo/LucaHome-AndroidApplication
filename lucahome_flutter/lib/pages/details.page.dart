import 'package:flutter/material.dart';
import 'package:lucahome_flutter/helper/icon.helper.dart';
import 'package:lucahome_flutter/models/wireless_socket.model.dart';

class DetailsPage extends StatefulWidget {
  final WirelessSocket wirelessSocket;

  DetailsPage(this.wirelessSocket);

  @override
  _DetailsPageState createState() => new _DetailsPageState();
}

class _DetailsPageState extends State<DetailsPage> {
  Widget get wirelessSocketImage {
    return new Hero(
      tag: widget.wirelessSocket.name,
      child: new Container(
        color: Colors.white,
        child: new SizedBox.expand(
          child: new Hero(
            tag: widget.wirelessSocket.icon,
            child: new Icon(
              fromString(widget.wirelessSocket.icon),
              size: 50.0,
            ),
          ),
        ),
      ),
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
    );
  }

  Widget get areaTextFormField {
    return new TextFormField(
      keyboardType: TextInputType.text,
      autofocus: false,
      initialValue: widget.wirelessSocket.area,
      decoration: InputDecoration(
        hintText: 'Area',
        contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
      ),
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
    );
  }

  Widget get wirelessSocketDetails {
    return new Container(
      padding: new EdgeInsets.symmetric(vertical: 32.0),
      decoration: new BoxDecoration(
        gradient: new LinearGradient(
          begin: Alignment.topRight,
          end: Alignment.bottomLeft,
          stops: [0.1, 0.5, 0.7, 0.9],
          colors: [
            Colors.indigo[800],
            Colors.indigo[700],
            Colors.indigo[600],
            Colors.indigo[400],
          ],
        ),
      ),
      child: new Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: <Widget>[
          wirelessSocketImage,
          nameTextFormField,
          codeTextFormField,
          areaTextFormField,
          descriptionTextFormField,
          iconTextFormField
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      backgroundColor: Colors.black87,
      appBar: new AppBar(
        backgroundColor: Colors.black87,
        title: new Text('Details for ${widget.wirelessSocket.name}'),
      ),
      body: new ListView(
        children: <Widget>[wirelessSocketDetails],
      ),
    );
  }
}
