package guepardoapps.lucahome.common.server.handler;

public interface IAccessControlDataHandler extends IDataHandler {
    String BroadcastAlarmState = "guepardoapps.lucahome.accesscontrol.broadcast.alarmState";
    String BroadcastEnteredCodeValid = "guepardoapps.lucahome.accesscontrol.broadcast.enteredCodeValid";
    String BroadcastEnteredCodeInvalid = "guepardoapps.lucahome.accesscontrol.broadcast.enteredCodeInvalid";

    String BundleAlarmState = "BundleAlarmState";
}
