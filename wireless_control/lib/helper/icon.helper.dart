import 'package:flutter/widgets.dart';
import 'package:wireless_control/constants/font_awesome.constants.dart';
import 'package:wireless_control/helper/string.helper.dart';

IconData fromString(String icon) {
  return FontAwesomeConstants()
      .icons
      .singleWhere((x) => x.key == new StringHelper(icon.substring(icon.indexOf("-"))).camelCase)
      .value;
}
