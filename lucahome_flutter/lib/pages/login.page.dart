import 'package:flutter/material.dart';

class LoginPage extends StatefulWidget {
  static String tag = 'login-page';

  @override
  _LoginPageState createState() => new _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _formKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    var pageSize = MediaQuery.of(context).size;

    final logo = Hero(
      tag: 'hero',
      child: CircleAvatar(
        backgroundColor: Colors.transparent,
        radius: 48.0,
        child: Image.asset('assets/logo.png'),
      ),
    );

    final nextCloudUrl = TextFormField(
      keyboardType: TextInputType.url,
      autofocus: true,
      initialValue: '',
      decoration: InputDecoration(
        hintText: 'NextCloudUrl',
        contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
        hintStyle: TextStyle(color: Colors.white54),
        errorStyle: TextStyle(color: Colors.red),
      ),
      style: TextStyle(color: Colors.white),
      validator: (value) {
        if (value.isEmpty) {
          return 'NextCloudUrl is required';
        }
      },
    );

    final userName = TextFormField(
      keyboardType: TextInputType.text,
      autofocus: false,
      initialValue: '',
      decoration: InputDecoration(
        hintText: 'UserName',
        contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
        hintStyle: TextStyle(color: Colors.white54),
        errorStyle: TextStyle(color: Colors.red),
      ),
      style: TextStyle(color: Colors.white),
      validator: (value) {
        if (value.isEmpty) {
          return 'UserName is required';
        }
      },
    );

    final passPhrase = TextFormField(
      autofocus: false,
      initialValue: '',
      obscureText: true,
      decoration: InputDecoration(
        hintText: 'Password',
        contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
        hintStyle: TextStyle(color: Colors.white54),
        errorStyle: TextStyle(color: Colors.red),
      ),
      style: TextStyle(color: Colors.white),
      validator: (value) {
        if (value.isEmpty) {
          return 'Password is required';
        }
      },
    );

    final loginButton = Padding(
      padding: EdgeInsets.symmetric(vertical: 16.0),
      child: RaisedButton(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(24),
        ),
        onPressed: () {
          if (_formKey.currentState.validate()) {
            // TODO Do something with the data
            var route = new MaterialPageRoute(
                settings: new RouteSettings(name: '/login'),
                builder: (context) => new LoginPage());
            Navigator.of(context)
                .pushAndRemoveUntil(route, ModalRoute.withName('/loading'));
          }
        },
        padding: EdgeInsets.all(12),
        color: Colors.lightBlueAccent,
        child: Text('Log In', style: TextStyle(color: Colors.white)),
      ),
    );

    return Form(
        key: _formKey,
        child: new Scaffold(
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
                child: ListView(
                  shrinkWrap: true,
                  padding: EdgeInsets.only(left: 24.0, right: 24.0),
                  children: <Widget>[
                    logo,
                    SizedBox(height: 48.0),
                    nextCloudUrl,
                    SizedBox(height: 24.0),
                    userName,
                    SizedBox(height: 8.0),
                    passPhrase,
                    SizedBox(height: 24.0),
                    loginButton
                  ],
                ),
              ),
            ],
          ),
        ));
  }
}
