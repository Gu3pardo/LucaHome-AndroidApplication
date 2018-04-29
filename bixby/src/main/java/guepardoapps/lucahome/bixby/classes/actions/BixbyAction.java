package guepardoapps.lucahome.bixby.classes.actions;

import android.support.annotation.NonNull;

import java.util.Locale;

@SuppressWarnings({"unused", "WeakerAccess"})
public class BixbyAction implements IBixbyAction {
    private static final String Tag = BixbyAction.class.getSimpleName();

    private int _id;
    private int _actionId;

    private ActionType _actionType;

    private ApplicationAction _applicationAction;
    private NetworkAction _networkAction;
    private WirelessSocketAction _wirelessSocketAction;
    private WirelessSwitchAction _wirelessSwitchAction;

    public BixbyAction(
            int id,
            int actionId,
            @NonNull ActionType actionType,
            @NonNull ApplicationAction applicationAction,
            @NonNull NetworkAction networkAction,
            @NonNull WirelessSocketAction wirelessSocketAction,
            @NonNull WirelessSwitchAction wirelessSwitchAction) {
        _id = id;
        _actionId = actionId;

        _actionType = actionType;

        _applicationAction = applicationAction;
        _networkAction = networkAction;
        _wirelessSocketAction = wirelessSocketAction;
        _wirelessSwitchAction = wirelessSwitchAction;
    }

    public BixbyAction(int id, int actionId) {
        _id = id;
        _actionId = actionId;

        _actionType = ActionType.Null;

        _applicationAction = new ApplicationAction();
        _networkAction = new NetworkAction();
        _wirelessSocketAction = new WirelessSocketAction();
        _wirelessSwitchAction = new WirelessSwitchAction();
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

    public WirelessSwitchAction GetWirelessSwitchAction() {
        return _wirelessSwitchAction;
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
            case WirelessSwitch:
                return _wirelessSwitchAction.GetInformationString();
            case Null:
            default:
                return "No information available!";
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Id\":%d,\"ActionId\":%d,\"ActionType\":\"%s\",\"ApplicationAction\":\"%s\",\"NetworkAction\":\"%s\",\"WirelessSocketAction\":\"%s\",\"WirelessSwitchAction\":\"%s\"}",
                Tag, _id, _actionId, _actionType, _applicationAction, _networkAction, _wirelessSocketAction, _wirelessSwitchAction);
    }
}
