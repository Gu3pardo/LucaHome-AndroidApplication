package guepardoapps.lucahome.bixby.classes.requirements;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public class BixbyRequirement implements IBixbyRequirement, Serializable {
    private static final String Tag = BixbyRequirement.class.getSimpleName();

    private int _id;
    private int _actionId;

    private RequirementType _requirementType;

    private String _puckJsPosition;

    private LightRequirement _lightRequirement;
    private NetworkRequirement _networkRequirement;
    private WirelessSocketRequirement _wirelessSocketRequirement;

    public BixbyRequirement(
            int id,
            int actionId,
            @NonNull RequirementType requirementType,
            @NonNull String puckJsPosition,
            @NonNull LightRequirement lightRequirement,
            @NonNull NetworkRequirement networkRequirement,
            @NonNull WirelessSocketRequirement wirelessSocketRequirement) {
        _id = id;
        _actionId = actionId;

        _requirementType = requirementType;

        _puckJsPosition = puckJsPosition;

        _lightRequirement = lightRequirement;
        _networkRequirement = networkRequirement;
        _wirelessSocketRequirement = wirelessSocketRequirement;
    }

    public int GetId() {
        return _id;
    }

    public int GetActionId() {
        return _actionId;
    }

    public RequirementType GetRequirementType() {
        return _requirementType;
    }

    public String GetPuckJsPosition() {
        return _puckJsPosition;
    }

    public LightRequirement GetLightRequirement() {
        return _lightRequirement;
    }

    public NetworkRequirement GetNetworkRequirement() {
        return _networkRequirement;
    }

    public WirelessSocketRequirement GetWirelessSocketRequirement() {
        return _wirelessSocketRequirement;
    }

    @Override
    public String GetDatabaseString() throws NoSuchMethodException {
        throw new NoSuchMethodException("This method is not available!");
    }

    @Override
    public String GetInformationString() {
        switch (_requirementType) {
            case Light:
                return _lightRequirement.GetInformationString();
            case Network:
                return _networkRequirement.GetInformationString();
            case Position:
                return String.format(Locale.getDefault(), "Position: %s", _puckJsPosition);
            case WirelessSocket:
                return _wirelessSocketRequirement.GetInformationString();
            case Null:
            default:
                return "No information available!";
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Id\":%d,\"ActionId\":%d,\"RequirementType\":\"%s\",\"PuckJsPosition\":\"%s\",\"LightRequirement\":\"%s\",\"NetworkRequirement\":\"%s\",\"WirelessSocketRequirement\":\"%s\"}",
                Tag, _id, _actionId, _requirementType, _puckJsPosition, _lightRequirement, _networkRequirement, _wirelessSocketRequirement);
    }
}
