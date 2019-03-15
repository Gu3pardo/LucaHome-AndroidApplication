import 'package:flutter/material.dart';
import 'package:wireless_control/constants/color.constants.dart';
import 'package:wireless_control/enums/app_theme.enum.dart';

Widget getDetailsIcon(IconData iconData, AppTheme theme) {
  return new Icon(
    iconData,
    size: 125,
    color: theme == AppTheme.Light ? ColorConstants.IconDark : ColorConstants.IconLight,
  );
}

Widget getTextFormField(
    String initialValue,
    String hintText,
    Function(String) validator,
    Function(String) onSaved,
    AppTheme theme) {
  return new TextFormField(
    keyboardType: TextInputType.text,
    autofocus: false,
    initialValue: initialValue,
    decoration: InputDecoration(
      hintText: hintText,
      contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
      border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
    ),
    style: TextStyle(color: theme == AppTheme.Light ? ColorConstants.TextDark : ColorConstants.TextLight),
    validator: validator,
    onSaved: onSaved,
  );
}