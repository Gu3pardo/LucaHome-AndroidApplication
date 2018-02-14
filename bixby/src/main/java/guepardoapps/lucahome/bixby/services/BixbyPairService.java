package guepardoapps.lucahome.bixby.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.bixby.classes.BixbyPair;
import guepardoapps.lucahome.bixby.classes.actions.ApplicationAction;
import guepardoapps.lucahome.bixby.classes.actions.BixbyAction;
import guepardoapps.lucahome.bixby.classes.actions.NetworkAction;
import guepardoapps.lucahome.bixby.classes.actions.WirelessSocketAction;
import guepardoapps.lucahome.bixby.classes.actions.WirelessSwitchAction;
import guepardoapps.lucahome.bixby.classes.requirements.BixbyRequirement;
import guepardoapps.lucahome.bixby.classes.requirements.LightRequirement;
import guepardoapps.lucahome.bixby.classes.requirements.NetworkRequirement;
import guepardoapps.lucahome.bixby.classes.requirements.WirelessSocketRequirement;
import guepardoapps.lucahome.bixby.databases.DatabaseBixbyActionList;
import guepardoapps.lucahome.bixby.databases.DatabaseBixbyRequirementList;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.Position;
import guepardoapps.lucahome.common.classes.PuckJs;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.UserInformationController;
import guepardoapps.lucahome.common.services.PositioningService;
import guepardoapps.lucahome.common.services.WirelessSocketService;
import guepardoapps.lucahome.common.services.WirelessSwitchService;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"deprecation", "FieldCanBeLocal", "WeakerAccess"})
public class BixbyPairService implements IBixbyPairService<BixbyPair> {
    private static final String Tag = BixbyPairService.class.getSimpleName();

    private static final BixbyPairService Singleton = new BixbyPairService();

    private Context _context;

    private DatabaseBixbyActionList _databaseBixbyActionList;
    private DatabaseBixbyRequirementList _databaseBixbyRequirementList;

    private BroadcastController _broadcastController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;
    private UserInformationController _userInformationController;

    private Calendar _lastUpdate;

    private boolean _isInitialized;

    // TODO should be used directly and not asynchron...
    private Position _lastReceivedPosition = new Position(new PuckJs(UUID.randomUUID(), "", "", "", false, ILucaClass.LucaServerDbAction.Null), -1);
    private BroadcastReceiver _positionUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PositioningService.PositionCalculationObject calculationObject = (PositioningService.PositionCalculationObject) intent.getSerializableExtra(PositioningService.PositioningCalculationFinishedBundle);
            if (calculationObject != null) {
                if (calculationObject.Valid) {
                    _lastReceivedPosition = calculationObject.CalculatedPosition;
                }
            }
        }
    };

    private BixbyPairService() {
    }

    public static BixbyPairService getInstance() {
        return Singleton;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout, boolean displayNotification, Class<?> receiverActivity) {
        if (_isInitialized) {
            Logger.getInstance().Warning(Tag, "Already initialized!");
            return;
        }

        _lastUpdate = Calendar.getInstance();

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _networkController = new NetworkController(_context);
        _receiverController = new ReceiverController(_context);
        _userInformationController = new UserInformationController(_context);

        _receiverController.RegisterReceiver(_positionUpdateReceiver, new String[]{PositioningService.PositioningCalculationFinishedBroadcast});

        _databaseBixbyActionList = new DatabaseBixbyActionList(_context);
        _databaseBixbyActionList.Open();

        _databaseBixbyRequirementList = new DatabaseBixbyRequirementList(_context);
        _databaseBixbyRequirementList.Open();

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        Logger.getInstance().Warning(Tag, "Dispose");
        _receiverController.Dispose();
        _databaseBixbyActionList.Close();
        _databaseBixbyRequirementList.Close();
        _isInitialized = false;
    }

    @Override
    public void BixbyButtonPressed() throws Exception {
        Logger.getInstance().Debug(Tag, "BixbyButtonPressed");

        for (BixbyPair bixbyPair : createPairList()) {
            BixbyAction bixbyAction = bixbyPair.GetAction();
            ArrayList<BixbyRequirement> requirementList = bixbyPair.GetRequirements();

            if (validateRequirements(requirementList)) {
                Logger.getInstance().Information(Tag, "All requirements true! Running callback!");
                try {
                    performAction(bixbyAction);
                } catch (Exception exception) {
                    Logger.getInstance().Error(Tag, exception.toString());
                }
            }
        }
    }

    @Override
    public ArrayList<BixbyPair> GetDataList() {
        return createPairList();
    }

    @Override
    public BixbyPair GetById(int id) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetById not implemented for " + Tag);
    }

    @Override
    public int GetHighestId() {
        ArrayList<BixbyPair> list = GetDataList();
        int highestId = -1;
        for (int index = 0; index < list.size(); index++) {
            int id = list.get(index).GetActionId();
            if (id > highestId) {
                highestId = id;
            }
        }
        return highestId;
    }

    @Override
    public ArrayList<BixbyPair> SearchDataList(@NonNull String searchKey) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SearchDataList not implemented for " + Tag);
    }

    @Override
    public ArrayList<String> GetPackageNameList() {
        return _userInformationController.GetApkPackageNameArrayList();
    }

    @Override
    public void LoadData() {
        _broadcastController.SendBooleanBroadcast(BixbyPairLoadFinishedBroadcast, BixbyPairLoadFinishedBundle, true);
    }

    @Override
    public void AddEntry(@NonNull BixbyPair bixbyPair) {
        try {
            _databaseBixbyActionList.AddEntry(bixbyPair.GetAction());
            ArrayList<BixbyRequirement> requirementList = bixbyPair.GetRequirements();
            for (int index = 0; index < requirementList.size(); index++) {
                _databaseBixbyRequirementList.AddEntry(requirementList.get(index));
            }
            _broadcastController.SendBooleanBroadcast(BixbyPairAddFinishedBroadcast, BixbyPairAddFinishedBundle, true);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
            _broadcastController.SendBooleanBroadcast(BixbyPairAddFinishedBroadcast, BixbyPairAddFinishedBundle, false);
        }
    }

    @Override
    public void UpdateEntry(@NonNull BixbyPair bixbyPair) {
        try {
            _databaseBixbyActionList.UpdateEntry(bixbyPair.GetAction());
            ArrayList<BixbyRequirement> requirementList = bixbyPair.GetRequirements();
            for (int index = 0; index < requirementList.size(); index++) {
                try {
                    _databaseBixbyRequirementList.UpdateEntry(requirementList.get(index));
                } catch (Exception exception) {
                    _databaseBixbyRequirementList.AddEntry(requirementList.get(index));
                }
            }
            _broadcastController.SendBooleanBroadcast(BixbyPairUpdateFinishedBroadcast, BixbyPairUpdateFinishedBundle, true);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
            _broadcastController.SendBooleanBroadcast(BixbyPairUpdateFinishedBroadcast, BixbyPairUpdateFinishedBundle, false);
        }
    }

    @Override
    public void DeleteEntry(@NonNull BixbyPair bixbyPair) {
        try {
            _databaseBixbyActionList.DeleteEntry(bixbyPair.GetAction());
            ArrayList<BixbyRequirement> requirementList = bixbyPair.GetRequirements();
            for (int index = 0; index < requirementList.size(); index++) {
                _databaseBixbyRequirementList.DeleteEntry(requirementList.get(index));
            }
            _broadcastController.SendBooleanBroadcast(BixbyPairDeleteFinishedBroadcast, BixbyPairDeleteFinishedBundle, true);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
            _broadcastController.SendBooleanBroadcast(BixbyPairDeleteFinishedBroadcast, BixbyPairDeleteFinishedBundle, false);
        }
    }

    @Override
    public void SetReloadEnabled(boolean reloadEnabled) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetReloadEnabled not implemented for " + Tag);
    }

    @Override
    public boolean GetReloadEnabled() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetReloadEnabled not implemented for " + Tag);
    }

    @Override
    public void SetReloadTimeout(int reloadTimeout) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetReloadTimeout not implemented for " + Tag);
    }

    @Override
    public int GetReloadTimeout() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetReloadTimeout not implemented for " + Tag);
    }

    @Override
    public Calendar GetLastUpdate() {
        return _lastUpdate;
    }

    @Override
    public void ShowNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method ShowNotification not implemented for " + Tag);
    }

    @Override
    public void CloseNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method CloseNotification not implemented for " + Tag);
    }

    @Override
    public void SetDisplayNotification(boolean displayNotification) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetDisplayNotification not implemented for " + Tag);
    }

    @Override
    public boolean GetDisplayNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetDisplayNotification not implemented for " + Tag);
    }

    @Override
    public void SetReceiverActivity(@NonNull Class<?> receiverActivity) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetReceiverActivity not implemented for " + Tag);
    }

    @Override
    public Class<?> GetReceiverActivity() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetReceiverActivity not implemented for " + Tag);
    }

    private ArrayList<BixbyPair> createPairList() {
        ArrayList<BixbyPair> pairList = new ArrayList<>();

        try {
            ArrayList<BixbyAction> actionList = _databaseBixbyActionList.GetList(null, null, null);
            ArrayList<BixbyRequirement> requirementList = _databaseBixbyRequirementList.GetList(null, null, null);

            for (int actionIndex = 0; actionIndex < actionList.size(); actionIndex++) {
                BixbyAction bixbyAction = actionList.get(actionIndex);
                int actionId = bixbyAction.GetActionId();
                ArrayList<BixbyRequirement> pairRequirementList = new ArrayList<>();

                for (int requirementIndex = 0; requirementIndex < requirementList.size(); requirementIndex++) {
                    BixbyRequirement bixbyRequirement = requirementList.get(requirementIndex);
                    if (bixbyRequirement.GetActionId() == actionId) {
                        pairRequirementList.add(bixbyRequirement);
                    }
                }

                BixbyPair bixbyPair = new BixbyPair(actionId, bixbyAction, pairRequirementList, BixbyPair.DatabaseAction.Null);
                pairList.add(bixbyPair);
            }
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
            _broadcastController.SendBooleanBroadcast(BixbyPairLoadFinishedBroadcast, BixbyPairLoadFinishedBundle, false);
        }

        return pairList;
    }

    private boolean validateRequirements(@NonNull ArrayList<BixbyRequirement> requirementList) throws Exception {
        boolean allRequirementsTrue = true;

        for (int requirementIndex = 0; requirementIndex < requirementList.size(); requirementIndex++) {
            BixbyRequirement bixbyRequirement = requirementList.get(requirementIndex);

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
                    Logger.getInstance().Error(Tag, "Invalid BixbyRequirement!");
                    allRequirementsTrue = false;
                    break;
            }
        }

        return allRequirementsTrue;
    }

    private boolean validatePositionRequirement(@NonNull String puckJsPosition) throws Exception {
        return _lastReceivedPosition != null && puckJsPosition.contains(_lastReceivedPosition.GetPuckJs().GetArea());
    }

    private boolean validateLightRequirement(@NonNull LightRequirement lightRequirement) throws Exception {
        return _lastReceivedPosition != null && lightRequirement.ValidateActualValue(_lastReceivedPosition.GetLightValue());
    }

    private boolean validateNetworkRequirement(@NonNull NetworkRequirement networkRequirement) {
        switch (networkRequirement.GetNetworkType()) {
            case Mobile:
                boolean isMobileDataEnabled = _networkController.IsMobileDataEnabled();
                return (networkRequirement.GetStateType() == NetworkRequirement.StateType.On) == isMobileDataEnabled;

            case Wifi:
                boolean isWifiEnabled = _networkController.IsWifiConnected();
                NetworkRequirement.StateType stateType = networkRequirement.GetStateType();

                switch (stateType) {
                    case On:
                        String wifiSsid = _networkController.GetWifiSsid();
                        return isWifiEnabled && wifiSsid.contains(networkRequirement.GetWifiSsid());

                    case Off:
                        return !isWifiEnabled;

                    case Null:
                    default:
                        Logger.getInstance().Error(Tag, "Invalid StateType for WIFI!");
                        return false;
                }

            case Null:
            default:
                Logger.getInstance().Error(Tag, "Invalid NetworkType!");
                return false;
        }
    }

    private boolean validateWirelessSocketRequirement(@NonNull WirelessSocketRequirement wirelessSocketRequirement) {
        WirelessSocket wirelessSocket = WirelessSocketService.getInstance().GetByName(wirelessSocketRequirement.GetWirelessSocketName());
        return (wirelessSocketRequirement.GetStateType() == WirelessSocketRequirement.StateType.On) == wirelessSocket.IsActivated();
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

            case WirelessSwitch:
                WirelessSwitchAction wirelessSwitchAction = bixbyAction.GetWirelessSwitchAction();
                performWirelessSwitchAction(wirelessSwitchAction);
                break;

            case Null:
            default:
                Logger.getInstance().Error(Tag, "Invalid ActionType!");
                break;
        }
    }

    private void performApplicationAction(@NonNull ApplicationAction applicationAction) throws Exception {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "performApplicationAction for %s", applicationAction));

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
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "performNetworkAction for %s", networkAction));

        NetworkAction.NetworkType networkType = networkAction.GetNetworkType();
        switch (networkType) {
            case Mobile:
                switch (networkAction.GetStateType()) {
                    case On:
                        _networkController.SetMobileDataState(true);
                        break;
                    case Off:
                        _networkController.SetMobileDataState(false);
                        break;
                    case Null:
                    default:
                        Logger.getInstance().Error(Tag, "Invalid StateType!");
                        break;
                }
                break;

            case Wifi:
                switch (networkAction.GetStateType()) {
                    case On:
                        _networkController.SetWifiState(true);
                        break;
                    case Off:
                        _networkController.SetWifiState(false);
                        break;
                    case Null:
                    default:
                        Logger.getInstance().Error(Tag, "Invalid StateType!");
                        break;
                }
                break;

            case Null:
            default:
                Logger.getInstance().Error(Tag, "Invalid NetworkType!");
                break;
        }
    }

    private void performWirelessSocketAction(@NonNull WirelessSocketAction wirelessSocketAction) throws Exception {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "performWirelessSocketAction for %s", wirelessSocketAction));

        WirelessSocket wirelessSocket = WirelessSocketService.getInstance().GetByName(wirelessSocketAction.GetWirelessSocketName());
        WirelessSocketAction.StateType stateType = wirelessSocketAction.GetStateType();

        switch (stateType) {
            case On:
                WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocket, true);
                break;

            case Off:
                WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocket, false);
                break;

            case Null:
            default:
                Logger.getInstance().Error(Tag, "Invalid StateType!");
                break;
        }
    }

    private void performWirelessSwitchAction(@NonNull WirelessSwitchAction wirelessSwitchAction) throws Exception {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "performWirelessSwitchAction for %s", wirelessSwitchAction));

        WirelessSwitch wirelessSwitch = WirelessSwitchService.getInstance().GetByName(wirelessSwitchAction.GetWirelessSwitchName());
        WirelessSwitchService.getInstance().ToggleWirelessSwitch(wirelessSwitch);
    }
}
