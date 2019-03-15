import 'dart:convert';
import 'package:wireless_control/constants/shared_pref.constants.dart';
import 'package:wireless_control/enums/app_theme.enum.dart';
import 'package:wireless_control/models/next_cloud_credentials.model.dart';
import 'package:shared_preferences/shared_preferences.dart';

void saveNextCloudCredentials(NextCloudCredentials nextCloudCredentials) async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  await prefs.setString(SharedPrefConstants.nextCloudCredentials_Key, jsonEncode(nextCloudCredentials));
}

Future<NextCloudCredentials> loadNextCloudCredentials() async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  String jsonString = prefs.getString(SharedPrefConstants.nextCloudCredentials_Key) ?? "{\"baseUrl\":\"\",\"userName\":\"\",\"passPhrase\":\"\"}";
  return NextCloudCredentials.fromJson(jsonDecode(jsonString));
}

void saveAppTheme(AppTheme appTheme) async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  await prefs.setString(SharedPrefConstants.theme_Key, appTheme.index.toString());
}

Future<AppTheme> loadAppTheme() async {
  SharedPreferences prefs = await SharedPreferences.getInstance();
  String themeIndexString = prefs.getString(SharedPrefConstants.theme_Key) ?? "0";
  return AppTheme.values[int.parse(themeIndexString)];
}