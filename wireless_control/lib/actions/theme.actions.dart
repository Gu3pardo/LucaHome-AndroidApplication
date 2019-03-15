import 'package:flutter/foundation.dart';
import 'package:wireless_control/enums/app_theme.enum.dart';

class ThemeChange {
  final AppTheme theme;

  ThemeChange({@required this.theme});

  @override
  String toString() {
    return 'ThemeChange{theme: $theme}';
  }
}
