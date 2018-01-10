package guepardoapps.lucahome.bixby;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.Position;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.service.PositioningService;
import guepardoapps.lucahome.common.service.WirelessSocketService;

@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
public class BixbyPairService {
    private static final String TAG = BixbyPairService.class.getSimpleName();

    private static final BixbyPairService SINGLETON = new BixbyPairService();
    private boolean _isInitialized;

    private Context _context;

    private NetworkController _networkController;
    private ReceiverController _receiverController;

    private DatabaseBixbyActionList _databaseBixbyActionList;
    private DatabaseBixbyRequirementList _databaseBixbyRequirementList;

    private Position _lastPosition;
    private SerializableList<BixbyPair> _pairList;

    private BroadcastReceiver _positioningUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Debug(TAG, "Received new positioning update!");
            PositioningService.PositioningUpdateFinishedContent positionResult = (PositioningService.PositioningUpdateFinishedContent) intent.getSerializableExtra(PositioningService.PositioningCalculationFinishedBundle);
            if (positionResult != null) {
                if (positionResult.Success) {
                    _lastPosition = positionResult.LatestPosition;
                } else {
                    Logger.getInstance().Warning(TAG, "Last calculation of position seems to be failed!");
                }
            } else {
                Logger.getInstance().Error(TAG, "Received positionResult is null!");
            }
        }
    };

    private BixbyPairService() {
    }

    public static BixbyPairService getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        if (_isInitialized) {
            Logger.getInstance().Warning(TAG, "Already initialized!");
            return;
        }

        _context = context;

        _networkController = new NetworkController(_context);
        _receiverController = new ReceiverController(_context);

        _receiverController.RegisterReceiver(_positioningUpdateReceiver, new String[]{PositioningService.PositioningCalculationFinishedBroadcast});

        _databaseBixbyActionList = new DatabaseBixbyActionList(_context);
        _databaseBixbyActionList.Open();

        _databaseBixbyRequirementList = new DatabaseBixbyRequirementList(_context);
        _databaseBixbyRequirementList.Open();

        _pairList = createPairList();

        _isInitialized = true;
    }

    public void Dispose() {
        Logger.getInstance().Warning(TAG, "Dispose");
        _receiverController.Dispose();
        _databaseBixbyActionList.Close();
        _databaseBixbyRequirementList.Close();
        _isInitialized = false;
    }

    public void BixbyButtonPressed() {
        Logger.getInstance().Debug(TAG, "BixbyButtonPressed");

        for (int index = 0; index < _pairList.getSize(); index++) {
            BixbyPair bixbyPair = _pairList.getValue(index);

            BixbyAction bixbyAction = bixbyPair.GetAction();
            SerializableList<BixbyRequirement> requirementList = bixbyPair.GetRequirements();

            if (validateRequirements(requirementList)) {
                Logger.getInstance().Information(TAG, "All requirements true! Running callback!");
                try {
                    performAction(bixbyAction);
                } catch (Exception exception) {
                    Logger.getInstance().Error(TAG, exception.toString());
                }
            }
        }
    }

    public SerializableList<BixbyPair> GetPairList() {
        return _pairList;
    }

    public void SetPairList(@NonNull SerializableList<BixbyPair> pairList) {
        _pairList = pairList;
        clearDatabases();
        saveToDatabases();
    }

    public void AddBixbyPair(@NonNull BixbyPair bixbyPair) {
        _pairList.addValue(bixbyPair);

        try {
            _databaseBixbyActionList.CreateEntry(bixbyPair.GetAction());
            SerializableList<BixbyRequirement> requirementList = bixbyPair.GetRequirements();
            for (int index = 0; index < requirementList.getSize(); index++) {
                _databaseBixbyRequirementList.CreateEntry(requirementList.getValue(index));
            }
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            clearDatabases();
            saveToDatabases();
        }
    }

    public void UpdateBixbyPair(@NonNull BixbyPair bixbyPair) {
        _pairList.setValue(bixbyPair.GetActionId(), bixbyPair);

        try {
            _databaseBixbyActionList.Update(bixbyPair.GetAction());
            SerializableList<BixbyRequirement> requirementList = bixbyPair.GetRequirements();
            for (int index = 0; index < requirementList.getSize(); index++) {
                _databaseBixbyRequirementList.Update(requirementList.getValue(index));
            }
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            clearDatabases();
            saveToDatabases();
        }
    }

    public void DeleteBixbyPair(@NonNull BixbyPair bixbyPair) {
        _pairList.removeValue(bixbyPair);

        try {
            _databaseBixbyActionList.Delete(bixbyPair.GetAction());
            SerializableList<BixbyRequirement> requirementList = bixbyPair.GetRequirements();
            for (int index = 0; index < requirementList.getSize(); index++) {
                _databaseBixbyRequirementList.Delete(requirementList.getValue(index));
            }
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            clearDatabases();
            saveToDatabases();
        }
    }

    private SerializableList<BixbyPair> createPairList() {
        SerializableList<BixbyAction> actionList = _databaseBixbyActionList.GetBixbyActionList();
        SerializableList<BixbyRequirement> requirementList = _databaseBixbyRequirementList.GetBixbyRequirementList();

        SerializableList<BixbyPair> pairList = new SerializableList<>();

        for (int actionIndex = 0; actionIndex < actionList.getSize(); actionIndex++) {
            BixbyAction bixbyAction = actionList.getValue(actionIndex);
            int actionId = bixbyAction.GetActionId();
            SerializableList<BixbyRequirement> pairRequirementList = new SerializableList<>();

            for (int requirementIndex = 0; requirementIndex < requirementList.getSize(); requirementIndex++) {
                BixbyRequirement bixbyRequirement = requirementList.getValue(requirementIndex);
                if (bixbyRequirement.GetActionId() == actionId) {
                    pairRequirementList.addValue(bixbyRequirement);
                }
            }

            BixbyPair bixbyPair = new BixbyPair(actionId, bixbyAction, pairRequirementList);
            pairList.addValue(bixbyPair);
        }

        return pairList;
    }

    private boolean validateRequirements(@NonNull SerializableList<BixbyRequirement> requirementList) {
        boolean allRequirementsTrue = true;

        for (int requirementIndex = 0; requirementIndex < requirementList.getSize(); requirementIndex++) {
            BixbyRequirement bixbyRequirement = requirementList.getValue(requirementIndex);

            switch (bixbyRequirement.GetRequirementType()) {
                case Light:
                    LightRequirement lightRequirement = bixbyRequirement.GetLightRequirement();
                    allRequirementsTrue &= validateLightRequirement(lightRequirement);
                    break;

                case Position:
                    String puckJsPosition = bixbyRequirement.GetPuckJsPosition();
                    allRequirementsTrue &= validatePositionRequirement(puckJsPosition);
                    break;

                case Network:
                    NetworkRequirement networkRequirement = bixbyRequirement.GetNetworkRequirement();
                    allRequirementsTrue &= validateNetworkRequirement(networkRequirement);
                    break;

                case WirelessSocket:
                    WirelessSocketRequirement wirelessSocketRequirement = bixbyRequirement.GetWirelessSocketRequirement();
                    allRequirementsTrue &= validateWirelessSocketRequirement(wirelessSocketRequirement);
                    break;

                case Null:
                default:
                    Logger.getInstance().Error(TAG, "Invalid BixbyRequirement!");
                    allRequirementsTrue = false;
                    break;
            }
        }

        return allRequirementsTrue;
    }

    private boolean validatePositionRequirement(@NonNull String puckJsPosition) {
        return _lastPosition != null && puckJsPosition.contains(_lastPosition.GetPuckJs().GetArea());
    }

    private boolean validateLightRequirement(@NonNull LightRequirement lightRequirement) {
        return _lastPosition != null && lightRequirement.ValidateActualValue(_lastPosition.GetLightValue());
    }

    private boolean validateNetworkRequirement(@NonNull NetworkRequirement networkRequirement) {
        switch (networkRequirement.GetNetworkType()) {
            case MOBILE:
                boolean isMobileDataEnabled = _networkController.IsMobileDataEnabled();
                return (networkRequirement.GetStateType() == NetworkRequirement.StateType.ON) == isMobileDataEnabled;

            case WIFI:
                boolean isWifiEnabled = _networkController.IsWifiConnected();
                NetworkRequirement.StateType stateType = networkRequirement.GetStateType();

                switch (stateType) {
                    case ON:
                        String wifiSsid = _networkController.GetWifiSsid();
                        return isWifiEnabled && wifiSsid.contains(networkRequirement.GetWifiSsid());

                    case OFF:
                        return !isWifiEnabled;

                    case NULL:
                    default:
                        Logger.getInstance().Error(TAG, "Invalid StateType for WIFI!");
                        return false;
                }

            case NULL:
            default:
                Logger.getInstance().Error(TAG, "Invalid NetworkType!");
                return false;
        }
    }

    private boolean validateWirelessSocketRequirement(@NonNull WirelessSocketRequirement wirelessSocketRequirement) {
        WirelessSocket wirelessSocket = WirelessSocketService.getInstance().GetSocketByName(wirelessSocketRequirement.GetWirelessSocketName());
        return (wirelessSocketRequirement.GetStateType() == WirelessSocketRequirement.StateType.ON) == wirelessSocket.IsActivated();
    }

    private void performAction(@NonNull BixbyAction bixbyAction) throws Exception {
        BixbyAction.ActionType actionType = bixbyAction.GetActionType();

        switch (actionType) {
            case Application:
                ApplicationAction applicationAction = bixbyAction.GetApplicationAction();
                performApplicationAction(applicationAction);
                break;

            case Network:
                NetworkAction networkAction = bixbyAction.GetNetworkAction();
                performNetworkAction(networkAction);
                break;

            case WirelessSocket:
                WirelessSocketAction wirelessSocketAction = bixbyAction.GetWirelessSocketAction();
                performWirelessSocketAction(wirelessSocketAction);
                break;

            case Null:
            default:
                Logger.getInstance().Error(TAG, "Invalid ActionType!");
                break;
        }
    }

    private void performApplicationAction(@NonNull ApplicationAction applicationAction) throws Exception {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "performApplicationAction for %s", applicationAction));

        String packageName = applicationAction.GetPackageName();

        PackageManager packageManager = _context.getPackageManager();

        Intent startApplicationIntent = packageManager.getLaunchIntentForPackage(packageName);
        if (startApplicationIntent == null) {
            throw new NullPointerException(String.format(Locale.getDefault(), "Created startApplicationContent for %s is null", packageName));
        }

        startApplicationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        _context.startActivity(startApplicationIntent);
    }

    private void performNetworkAction(@NonNull NetworkAction networkAction) throws Exception {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "performNetworkAction for %s", networkAction));

        NetworkAction.NetworkType networkType = networkAction.GetNetworkType();
        switch (networkType) {
            case MOBILE:
                switch (networkAction.GetStateType()) {
                    case ON:
                        _networkController.SetMobileDataState(true);
                        break;
                    case OFF:
                        _networkController.SetMobileDataState(false);
                        break;
                    case NULL:
                    default:
                        Logger.getInstance().Error(TAG, "Invalid StateType!");
                        break;
                }
                break;

            case WIFI:
                switch (networkAction.GetStateType()) {
                    case ON:
                        _networkController.SetWifiState(true);
                        break;
                    case OFF:
                        _networkController.SetWifiState(false);
                        break;
                    case NULL:
                    default:
                        Logger.getInstance().Error(TAG, "Invalid StateType!");
                        break;
                }
                break;

            case NULL:
            default:
                Logger.getInstance().Error(TAG, "Invalid NetworkType!");
                break;
        }
    }

    private void performWirelessSocketAction(@NonNull WirelessSocketAction wirelessSocketAction) throws Exception {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "performWirelessSocketAction for %s", wirelessSocketAction));

        WirelessSocket wirelessSocket = WirelessSocketService.getInstance().GetSocketByName(wirelessSocketAction.GetWirelessSocketName());
        WirelessSocketAction.StateType stateType = wirelessSocketAction.GetStateType();

        switch (stateType) {
            case ON:
                WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocket, true);
                break;

            case OFF:
                WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocket, false);
                break;

            case NULL:
            default:
                Logger.getInstance().Error(TAG, "Invalid StateType!");
                break;
        }
    }

    private void clearDatabases() {
        SerializableList<BixbyAction> actionList = _databaseBixbyActionList.GetBixbyActionList();
        for (int index = 0; index < actionList.getSize(); index++) {
            BixbyAction action = actionList.getValue(index);
            _databaseBixbyActionList.Delete(action);
        }

        SerializableList<BixbyRequirement> requirementList = _databaseBixbyRequirementList.GetBixbyRequirementList();
        for (int index = 0; index < requirementList.getSize(); index++) {
            BixbyRequirement requirement = requirementList.getValue(index);
            _databaseBixbyRequirementList.Delete(requirement);
        }
    }

    private void saveToDatabases() {
        for (int index = 0; index < _pairList.getSize(); index++) {
            BixbyPair bixbyPair = _pairList.getValue(index);

            BixbyAction action = bixbyPair.GetAction();
            _databaseBixbyActionList.CreateEntry(action);

            SerializableList<BixbyRequirement> requirementList = bixbyPair.GetRequirements();
            for (int requirementIndex = 0; requirementIndex < requirementList.getSize(); requirementIndex++) {
                _databaseBixbyRequirementList.CreateEntry(requirementList.getValue(requirementIndex));
            }
        }
    }
}
