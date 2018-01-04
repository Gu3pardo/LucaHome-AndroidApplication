package guepardoapps.lucahome.bixby;

import android.support.annotation.NonNull;

import java.util.Locale;

public class BixbyRequirement {
    private static final String TAG = BixbyRequirement.class.getSimpleName();

    public enum RequirementType {Null, Position, Light, Network, WirelessSocket}

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
    public String toString() {
        return String.format(Locale.getDefault(),
                "{%s:{Id:%d,ActionId:%d,RequirementType:%s,PuckJsPosition:%s,LightRequirement:%s,NetworkRequirement:%s,WirelessSocketRequirement:%s}}",
                TAG, _id, _actionId, _requirementType, _puckJsPosition, _lightRequirement, _networkRequirement, _wirelessSocketRequirement);
    }
}
