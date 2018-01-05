package guepardoapps.lucahome.bixby;

import android.support.annotation.NonNull;

import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public class BixbyAction {
    private static final String TAG = BixbyAction.class.getSimpleName();

    public enum ActionType {Null, Network, WirelessSocket}

    private int _id;
    private int _actionId;

    private ActionType _actionType;

    private NetworkAction _networkAction;
    private WirelessSocketAction _wirelessSocketAction;

    public BixbyAction(
            int id,
            int actionId,
            @NonNull ActionType actionType,
            @NonNull NetworkAction networkAction,
            @NonNull WirelessSocketAction wirelessSocketAction) {
        _id = id;
        _actionId = actionId;

        _actionType = actionType;

        _networkAction = networkAction;
        _wirelessSocketAction = wirelessSocketAction;
    }

    public int GetId() {
        return _id;
    }

    public int GetActionId() {
        return _actionId;
    }

    public ActionType GetActionType() {
        return _actionType;
    }

    public NetworkAction GetNetworkAction() {
        return _networkAction;
    }

    public WirelessSocketAction GetWirelessSocketAction() {
        return _wirelessSocketAction;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{%s:{Id:%d,ActionId:%d,ActionType:%s,NetworkAction:%s,WirelessSocketAction:%s}}",
                TAG, _id, _actionId, _actionType, _networkAction, _wirelessSocketAction);
    }
}
