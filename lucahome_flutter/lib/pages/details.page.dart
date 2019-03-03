import 'package:flutter/material.dart';
import 'package:lucahome_flutter/helper/icon.helper.dart';
import 'package:lucahome_flutter/models/wireless_socket.model.dart';

class DetailsPage extends StatefulWidget {
  static String tag = 'details-page';

  final WirelessSocket wirelessSocket;

  DetailsPage(this.wirelessSocket);

  @override
  _DetailsPageState createState() => new _DetailsPageState();
}

class _DetailsPageState extends State<DetailsPage> {
  Widget get icon {
    return new Icon(
      fromString(widget.wirelessSocket.icon),
      size: 150,
      color: Colors.white,
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
      style: TextStyle(color: Colors.white),
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
      style: TextStyle(color: Colors.white),
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
      style: TextStyle(color: Colors.white),
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
      style: TextStyle(color: Colors.white),
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
      style: TextStyle(color: Colors.white),
    );
  }

  @override
  Widget build(BuildContext context) {
    var pageSize = MediaQuery.of(context).size;

    return new Container(
        child: new Scaffold(
      appBar: new AppBar(
        backgroundColor: Color(0xFF3744B0),
        title: new Text('Details for ${widget.wirelessSocket.name}'),
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
              child: new Center(
                child: ListView(
                  padding: EdgeInsets.only(left: 24.0, right: 24.0),
                  children: <Widget>[
                    SizedBox(height: 24.0),
                    icon,
                    SizedBox(height: 24.0),
                    nameTextFormField,
                    SizedBox(height: 24.0),
                    codeTextFormField,
                    SizedBox(height: 24.0),
                    areaTextFormField,
                    SizedBox(height: 24.0),
                    descriptionTextFormField,
                    SizedBox(height: 24.0),
                    iconTextFormField
                  ],
                ),
              )),
        ],
      ),
    ));
  }
}
