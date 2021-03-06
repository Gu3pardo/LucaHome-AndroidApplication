import 'package:flutter/material.dart';
import 'package:wave/config.dart';
import 'package:wave/wave.dart';
import 'package:wireless_control/constants/color.constants.dart';
import 'package:wireless_control/enums/app_theme.enum.dart';

Widget getDetailsIcon(IconData iconData, AppTheme theme) {
  return new Icon(
    iconData,
    size: 75,
    color: theme == AppTheme.Light ? ColorConstants.IconDark : ColorConstants.IconLight,
  );
}

Widget getTextFormField(
    String initialValue,
    String hintText,
    Function(String) validator,
    Function(String) onSaved,
    AppTheme theme,
    Function(String) onFieldSubmitted) {
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
    onFieldSubmitted: onFieldSubmitted ?? (String value) {},
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