import 'package:flutter/material.dart';
import 'package:wireless_control/constants/color.constants.dart';

onSuccess(BuildContext context, String text) {
  Scaffold.of(context).showSnackBar(new SnackBar(
    backgroundColor: ColorConstants.Success,
    content: Text(text),
  ));
}

onError(BuildContext context, String text) {
  Scaffold.of(context).showSnackBar(new SnackBar(
    backgroundColor: ColorConstants.Error,
    content: Text(text),
  ));
}

onErrorRetry(BuildContext context, String text, VoidCallback retryAction) {
  Scaffold.of(context).showSnackBar(new SnackBar(
    backgroundColor: ColorConstants.Error,
    content: Text(text),
    action: SnackBarAction(
      label: 'Retry',
      onPressed: retryAction,
    ),
  ));
}
