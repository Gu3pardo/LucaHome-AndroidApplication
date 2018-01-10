package guepardoapps.bixby.classes.actions;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.bixby.classes.BixbyPair;
import guepardoapps.bixby.interfaces.IBixbyAction;

@SuppressWarnings("WeakerAccess")
public class BixbyAction implements IBixbyAction, Serializable {
    private static final String TAG = BixbyAction.class.getSimpleName();

    public enum ActionType {Null, Application, Network, WirelessSocket}

    private int _id;
    private int _actionId;

    private ActionType _actionType;

    private ApplicationAction _applicationAction;
    private NetworkAction _networkAction;
    private WirelessSocketAction _wirelessSocketAction;

    public BixbyAction(
            int id,
            int actionId,
            @NonNull ActionType actionType,
            @NonNull ApplicationAction applicationAction,
            @NonNull NetworkAction networkAction,
            @NonNull WirelessSocketAction wirelessSocketAction) {
        _id = id;
        _actionId = actionId;

        _actionType = actionType;

        _applicationAction = applicationAction;
        _networkAction = networkAction;
        _wirelessSocketAction = wirelessSocketAction;
    }

    public BixbyAction(int id, int actionId) {

        _id = id;
        _actionId = actionId;

        _actionType = ActionType.Null;

        _applicationAction = new ApplicationAction();
        _networkAction = new NetworkAction();
        _wirelessSocketAction = new WirelessSocketAction();
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

    public ApplicationAction GetApplicationAction() {
        return _applicationAction;
    }

    public NetworkAction GetNetworkAction() {
        return _networkAction;
    }

    public WirelessSocketAction GetWirelessSocketAction() {
        return _wirelessSocketAction;
    }

    @Override
    public String GetDatabaseString() throws NoSuchMethodException {
        throw new NoSuchMethodException("This method is not available!");
    }

    @Override
    public String GetInformationString() {
        switch (_actionType) {
            case Application:
                return _applicationAction.GetInformationString();
            case Network:
                return _networkAction.GetInformationString();
            case WirelessSocket:
                return _wirelessSocketAction.GetInformationString();
            case Null:
            default:
                return "No information available!";
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{%s:{Id:%d,ActionId:%d,ActionType:%s,ApplicationAction:%s,NetworkAction:%s,WirelessSocketAction:%s}}",
                TAG, _id, _actionId, _actionType, _applicationAction, _networkAction, _wirelessSocketAction);
    }
}
