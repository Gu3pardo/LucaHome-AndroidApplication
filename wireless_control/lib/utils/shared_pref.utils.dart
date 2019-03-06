import 'dart:convert';
import 'package:wireless_control/constants/shared_pref.constants.dart';
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
