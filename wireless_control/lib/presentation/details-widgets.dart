import 'package:flutter/material.dart';
import 'package:wireless_control/constants/color.constants.dart';

Widget getDetailsIcon(IconData iconData) {
  return new Icon(
    iconData,
    size: 125,
    color: ColorConstants.IconDark,
  );
}

Widget getTextFormField(
    String initialValue,
    String hintText,
    Function(String) validator,
    Function(String) onSaved) {
  return new TextFormField(
    keyboardType: TextInputType.text,
    autofocus: false,
    initialValue: initialValue,
    decoration: InputDecoration(
      hintText: hintText,
      contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
      border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
    ),
    style: TextStyle(color: ColorConstants.TextDark),
    validator: validator,
    onSaved: onSaved,
  );
}