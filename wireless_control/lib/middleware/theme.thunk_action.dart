import 'package:redux/redux.dart';
import 'package:redux_thunk/redux_thunk.dart';
import 'package:wireless_control/actions/theme.actions.dart';
import 'package:wireless_control/enums/app_theme.enum.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/utils/shared_pref.utils.dart';

ThunkAction<AppState> loadTheme() {
  return (Store<AppState> store) async {
    var theme = await loadAppTheme();
    store.dispatch(ThemeChange(theme: theme));
  };
}

ThunkAction<AppState> saveTheme(AppTheme theme) {
  return (Store<AppState> store) async {
    saveAppTheme(theme);
    store.dispatch(ThemeChange(theme: theme));
  };
}
