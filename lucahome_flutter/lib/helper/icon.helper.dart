import 'package:flutter/widgets.dart';
import 'package:lucahome_flutter/constants/font_awesome.constants.dart';
import 'package:lucahome_flutter/helper/string.helper.dart';

class IconHelper {
  IconData fromString(String icon) {
    return FontAwesomeConstants()
        .icons
        .singleWhere((x) => x.key == new StringHelper(icon.substring(icon.indexOf("-"))).camelCase)
        .value;
  }
}
