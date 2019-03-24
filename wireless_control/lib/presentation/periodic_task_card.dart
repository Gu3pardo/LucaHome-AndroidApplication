import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:redux/redux.dart';
import 'package:wireless_control/actions/periodic_task.actions.dart';
import 'package:wireless_control/constants/color.constants.dart';
import 'package:wireless_control/enums/app_theme.enum.dart';
import 'package:wireless_control/middleware/periodic_task.thunk_action.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/periodic_task.model.dart';
import 'package:wireless_control/presentation/shared-presentation.dart';
import 'package:wireless_control/utils/actions.util.dart';

class PeriodicTaskCard extends StatefulWidget {
  final PeriodicTask periodicTask;
  final Store<AppState> store;

  PeriodicTaskCard(this.periodicTask, this.store);

  @override
  PeriodicTaskCardState createState() {
    return new PeriodicTaskCardState();
  }
}

class PeriodicTaskCardState extends State<PeriodicTaskCard> {

  Widget periodicTaskCard(BuildContext context, Size pageSize) {
    return new Positioned(
      right: 0.0,
      child: new Container(
        width: pageSize.width * 0.65,
        height: 115.0,
        child: new Card(
          color: widget.store.state.theme == AppTheme.Light ? ColorConstants.CardBackgroundLightTransparent : ColorConstants.CardBackgroundDarkTransparent,
          child: InkWell(
              splashColor: ColorConstants.ButtonSubmit,
              onTap: () {
                widget.store.dispatch(new PeriodicTaskSelectSuccessful(periodicTask: widget.periodicTask));
                Navigator.pushNamed(context, '/details-periodic-task');
              },
              child: new Padding(
                padding: const EdgeInsets.only(
                  top: 8.0,
                  bottom: 8.0,
                  left: 8.0,
                  right: 8.0,
                ),
                child: new  BackdropFilter(
                  filter: new ImageFilter.blur(sigmaX: 10.0, sigmaY: 10.0),
                  child: new Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: <Widget>[
                      new Text(widget.periodicTask.name, style: TextStyle(color: widget.store.state.theme == AppTheme.Light ? ColorConstants.TextDark : ColorConstants.TextLight, fontSize: 18)),
                      new Text('${widget.periodicTask.weekday}, ${widget.periodicTask.hour}:${widget.periodicTask.minute}', style: TextStyle(color: widget.store.state.theme == AppTheme.Light ? ColorConstants.TextDark : ColorConstants.TextLight, fontSize: 15)),
                      new Text('State: ${widget.periodicTask.wirelessSocketState}', style: TextStyle(color: widget.store.state.theme == AppTheme.Light ? ColorConstants.TextDark : ColorConstants.TextLight, fontSize: 12)),
                      new Text('Periodic: ${widget.periodicTask.periodic}', style: TextStyle(color: widget.store.state.theme == AppTheme.Light ? ColorConstants.TextDark : ColorConstants.TextLight, fontSize: 12))
                    ],
                  ),
                )
              )),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    var pageSize = MediaQuery.of(context).size;

    return new Card(
      color: widget.store.state.theme == AppTheme.Light ? ColorConstants.CardBackgroundLight : ColorConstants.CardBackgroundDark,
      margin: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 8.0),
      child: new Padding(
        padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 8.0),
        child: new Container(
          height: 115.0,
          child: new Stack(
            children: <Widget>[
              widget.periodicTask.active == 1 ? waveWidgetOn() : waveWidgetOff(),
              periodicTaskCard(context, pageSize),
              new Positioned(
                  top: 5,
                  left: 5,
                  bottom: 5,
                  child: FlatButton(
                    color: Color.fromARGB(0, 0, 0, 0),
                    splashColor: ColorConstants.ButtonSubmit,
                    highlightColor: ColorConstants.ButtonSubmitHighlight,
                    padding: const EdgeInsets.all(10.0),
                    onPressed: () {
                      widget.periodicTask.active = widget.periodicTask.active == 1 ? 0 : 1;
                      widget.store.dispatch(updatePeriodicTask(
                          widget.store.state.nextCloudCredentials,
                          widget.periodicTask,
                          () => onSuccess(context, 'Successfully set state for ${widget.periodicTask.name}'),
                          () => onError(context, 'Failed to set state for ${widget.periodicTask.name}')));
                    },
                    child: new Icon(
                      widget.periodicTask.active == 1 ? FontAwesomeIcons.solidHourglass : FontAwesomeIcons.hourglass,
                      size: 50,
                      color: widget.store.state.theme == AppTheme.Light ? ColorConstants.IconDark : ColorConstants.IconLight,
                    ),
                  ))],
          ),
        ),
      ),
    );
  }
}
