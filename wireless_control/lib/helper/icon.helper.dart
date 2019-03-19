import 'package:flutter/widgets.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:wireless_control/constants/font_awesome.constants.dart';
import 'package:wireless_control/helper/string.helper.dart';

IconData fromString(String iconString) {
  var icon = FontAwesomeConstants()
       .icons
       .firstWhere((x) => x.key == new StringHelper(iconString.substring(iconString.indexOf("-"))).camelCase);
  return icon != null ? icon.value : FontAwesomeIcons.circle;
}
