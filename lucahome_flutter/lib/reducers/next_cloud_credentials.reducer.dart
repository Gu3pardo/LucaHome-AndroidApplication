import 'package:lucahome_flutter/actions/next_cloud_credentials.actions.dart';
import 'package:lucahome_flutter/models/next_cloud_credentials.model.dart';
import 'package:redux/redux.dart';

final nextCloudCredentialsReducer = combineReducers<NextCloudCredentials>([
  new TypedReducer<NextCloudCredentials, NextCloudCredentialsLogInSuccessful>(_logInSuccessful),
  new TypedReducer<NextCloudCredentials, NextCloudCredentialsLogInFail>(_logInFailed),
  new TypedReducer<NextCloudCredentials, NextCloudCredentialsLogOutSuccessful>(_logOutSuccessful),
  new TypedReducer<NextCloudCredentials, NextCloudCredentialsLogOutFail>(_logOutFailed),
]);

NextCloudCredentials _logInSuccessful(NextCloudCredentials nextCloudCredentials, action) => nextCloudCredentials;
NextCloudCredentials _logInFailed(NextCloudCredentials nextCloudCredentials, action) => null;

NextCloudCredentials _logOutSuccessful(NextCloudCredentials nextCloudCredentials, action) => null;
NextCloudCredentials _logOutFailed(NextCloudCredentials nextCloudCredentials, action) => nextCloudCredentials;
