import 'package:redux/redux.dart';
import 'package:wireless_control/actions/theme.actions.dart';
import 'package:wireless_control/enums/app_theme.enum.dart';

AppTheme _themeChange(AppTheme theme, action) => action.theme;

final themeReducer = combineReducers<AppTheme>([
  new TypedReducer<AppTheme, ThemeChange>(_themeChange),
]);
