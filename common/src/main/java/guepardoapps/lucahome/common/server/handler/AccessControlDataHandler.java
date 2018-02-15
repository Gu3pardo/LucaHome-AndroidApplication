package guepardoapps.lucahome.common.server.handler;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.enums.AccessControlAlarmState;
import guepardoapps.lucahome.common.enums.AccessControlServerReceiveAction;
import guepardoapps.lucahome.common.utils.Logger;

public class AccessControlDataHandler implements IAccessControlDataHandler {
    private static final String Tag = AccessControlDataHandler.class.getSimpleName();

    private BroadcastController _broadcastController;

    @Override
    public void Initialize(@NonNull Context context) {
        _broadcastController = new BroadcastController(context);
    }

    @Override
    public String PerformAction(String action) {
        if (action == null || action.length() <= 0) {
            Logger.getInstance().Warning(Tag, "Action is null!");
            return errorResponse("Action is null!", AccessControlServerReceiveAction.Null);
        }

        if (!action.startsWith("ACTION:")) {
            Logger.getInstance().Warning(Tag, "Action has invalid format!");
            return errorResponse("Action has invalid format!", AccessControlServerReceiveAction.Null);
        }

        AccessControlServerReceiveAction receiveAction = AccessControlServerReceiveAction.GetByString(action.replace("ACTION:", ""));
        if (receiveAction == AccessControlServerReceiveAction.Null) {
            Logger.getInstance().Warning(Tag, "ReceiveAction is null!");
            return errorResponse("ReceiveAction is null!", AccessControlServerReceiveAction.Null);
        }

        switch (receiveAction) {
            case RequestCode:
                _broadcastController.SendSerializableBroadcast(BroadcastAlarmState, BundleAlarmState, AccessControlAlarmState.RequestCode);
                return successResponse(receiveAction, "Success!");

            case LoginFailed:
                _broadcastController.SendSerializableBroadcast(BroadcastAlarmState, BundleAlarmState, AccessControlAlarmState.AccessFailed);
                return successResponse(receiveAction, "LoginFailed!");

            case LoginSuccess:
                _broadcastController.SendSerializableBroadcast(BroadcastAlarmState, BundleAlarmState, AccessControlAlarmState.AccessSuccessful);
                return successResponse(receiveAction, "LoginSuccess!");

            case AlarmActive:
                _broadcastController.SendSerializableBroadcast(BroadcastAlarmState, BundleAlarmState, AccessControlAlarmState.AlarmActive);
                return successResponse(receiveAction, "Success!");

            case ActivateAccessControl:
                _broadcastController.SendSerializableBroadcast(BroadcastAlarmState, BundleAlarmState, AccessControlAlarmState.AccessControlActive);
                return successResponse(receiveAction, "Success!");

            case Null:
            default:
                Logger.getInstance().Warning(Tag, "ReceiveAction not handled! " + receiveAction.toString());
                return errorResponse("ReceiveAction not handled!", AccessControlServerReceiveAction.Null);
        }
    }

    @Override
    public void Dispose() {
        Logger.getInstance().Verbose(Tag, "Dispose");
    }

    private String successResponse(@NonNull AccessControlServerReceiveAction accessControlServerReceiveAction, @NonNull String data) {
        return String.format(Locale.getDefault(), "OK%sCommand performed%s%s%s%s",
                ResponseSplitChar,
                ResponseSplitChar, accessControlServerReceiveAction.toString(),
                ResponseSplitChar, data);
    }

    private String errorResponse(@NonNull String errorMessage, @NonNull AccessControlServerReceiveAction accessControlServerReceiveAction) {
        return String.format(Locale.getDefault(), "Error%s%s%s%s%s%s",
                ResponseSplitChar, errorMessage,
                ResponseSplitChar, accessControlServerReceiveAction.toString(),
                ResponseSplitChar, "-");
    }
}
