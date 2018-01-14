package guepardoapps.bixby.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import guepardoapps.bixby.classes.BixbyPair;
import guepardoapps.bixby.classes.actions.*;
import guepardoapps.bixby.classes.requirements.*;
import guepardoapps.bixby.database.*;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.controller.UserInformationController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.Position;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.interfaces.services.IDataService;
import guepardoapps.lucahome.common.service.PositioningService;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
public class BixbyPairService implements IDataService {
    public static class BixbyPairUpdateFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<BixbyPair> BixbyPairList;

        BixbyPairUpdateFinishedContent(@NonNull SerializableList<BixbyPair> bixbyPairList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            BixbyPairList = bixbyPairList;
        }
    }

    public final static String BIXBY_PAIR_INTENT = "BIXBY_PAIR_INTENT";

    public static final String BixbyPairLoadFinishedBroadcast = "guepardoapps.bixby.services.bixpypair.load.finished";
    public static final String BixbyPairLoadFinishedBundle = "BixbyPairLoadFinishedBundle";

    public static final String BixbyPairAddFinishedBroadcast = "guepardoapps.bixby.services.bixpypair.add.finished";
    public static final String BixbyPairAddFinishedBundle = "BixbyPairAddFinishedBundle";

    public static final String BixbyPairUpdateFinishedBroadcast = "guepardoapps.bixby.services.bixpypair.update.finished";
    public static final String BixbyPairUpdateFinishedBundle = "BixbyPairUpdateFinishedBundle";

    public static final String BixbyPairDeleteFinishedBroadcast = "guepardoapps.bixby.services.bixpypair.delete.finished";
    public static final String BixbyPairDeleteFinishedBundle = "BixbyPairDeleteFinishedBundle";

    private static final String TAG = BixbyPairService.class.getSimpleName();

    private static final BixbyPairService SINGLETON = new BixbyPairService();
    private boolean _isInitialized;

    private Date _lastUpdate;

    private Context _context;

    private BroadcastController _broadcastController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;
    private UserInformationController _userInformationController;

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
                    _lastUpdate = new Date();
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

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout) {
        if (_isInitialized) {
            Logger.getInstance().Warning(TAG, "Already initialized!");
            return;
        }
        Logger.getInstance().Debug(TAG, "Initialize");

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _networkController = new NetworkController(_context);
        _receiverController = new ReceiverController(_context);
        _userInformationController = new UserInformationController(_context);

        _receiverController.RegisterReceiver(_positioningUpdateReceiver, new String[]{PositioningService.PositioningCalculationFinishedBroadcast});

        _databaseBixbyActionList = new DatabaseBixbyActionList(_context);
        _databaseBixbyActionList.Open();

        _databaseBixbyRequirementList = new DatabaseBixbyRequirementList(_context);
        _databaseBixbyRequirementList.Open();

        _pairList = createPairList();
        _broadcastController.SendSerializableBroadcast(
                BixbyPairLoadFinishedBroadcast,
                BixbyPairLoadFinishedBundle,
                new BixbyPairUpdateFinishedContent(_pairList, true, Tools.CompressStringToByteArray("Successfully loaded")));

        _lastUpdate = new Date();

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        Logger.getInstance().Warning(TAG, "Dispose");
        _receiverController.Dispose();
        _databaseBixbyActionList.Close();
        _databaseBixbyRequirementList.Close();
        _isInitialized = false;
    }

    @Override
    public SerializableList<BixbyPair> GetDataList() {
        return _pairList;
    }

    @Override
    public SerializableList<BixbyPair> SearchDataList(@NonNull String searchKey) {
        SerializableList<BixbyPair> foundBixbyPairs = new SerializableList<>();
        for (int index = 0; index < _pairList.getSize(); index++) {
            BixbyPair entry = _pairList.getValue(index);
            if (entry.toString().contains(searchKey)) {
                foundBixbyPairs.addValue(entry);
            }
        }
        return foundBixbyPairs;
    }

    @Override
    public int GetHighestId() {
        int highestId = -1;
        for (int index = 0; index < _pairList.getSize(); index++) {
            int id = _pairList.getValue(index).GetActionId();
            if (id > highestId) {
                highestId = id;
            }
        }
        return highestId;
    }

    public int GetHighestActionId() {
        SerializableList<BixbyAction> actionList = _databaseBixbyActionList.GetBixbyActionList();
        int highestActionId = -1;
        for (int index = 0; index < actionList.getSize(); index++) {
            int id = actionList.getValue(index).GetId();
            if (id > highestActionId) {
                highestActionId = id;
            }
        }
        return highestActionId;
    }

    public int GetHighestRequirementId() {
        SerializableList<BixbyRequirement> requirementList = _databaseBixbyRequirementList.GetBixbyRequirementList();
        int highestRequirementId = -1;
        for (int index = 0; index < requirementList.getSize(); index++) {
            int id = requirementList.getValue(index).GetId();
            if (id > highestRequirementId) {
                highestRequirementId = id;
            }
        }
        return highestRequirementId;
    }

    public ArrayList<String> GetPackageNameList() {
        return _userInformationController.GetApkPackageNameArrayList();
    }

    @Override
    public void LoadData() {
        _pairList = createPairList();
        _broadcastController.SendSerializableBroadcast(
                BixbyPairLoadFinishedBroadcast,
                BixbyPairLoadFinishedBundle,
                new BixbyPairUpdateFinishedContent(_pairList, true, Tools.CompressStringToByteArray("Successfully loaded")));
    }

    @Override
    public boolean GetReloadEnabled() {
        Logger.getInstance().Warning(TAG, "method not necessary!");
        return false;
    }

    @Override
    public void SetReloadEnabled(boolean reloadEnabled) {
        Logger.getInstance().Warning(TAG, "method not necessary!");
    }

    @Override
    public int GetReloadTimeout() {
        Logger.getInstance().Warning(TAG, "method not necessary!");
        return 0;
    }

    @Override
    public void SetReloadTimeout(int reloadTimeout) {
        Logger.getInstance().Warning(TAG, "method not necessary!");
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

    public void AddBixbyPair(@NonNull BixbyPair bixbyPair) {
        _pairList.addValue(bixbyPair);

        try {
            _databaseBixbyActionList.CreateEntry(bixbyPair.GetAction());
            SerializableList<BixbyRequirement> requirementList = bixbyPair.GetRequirements();
            for (int index = 0; index < requirementList.getSize(); index++) {
                _databaseBixbyRequirementList.CreateEntry(requirementList.getValue(index));
            }
            _broadcastController.SendSerializableBroadcast(
                    BixbyPairAddFinishedBroadcast,
                    BixbyPairAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, Tools.CompressStringToByteArray("Add finished")));
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            clearDatabases();
            saveToDatabases();
            sendFailedAddBroadcast(exception.toString());
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
            _broadcastController.SendSerializableBroadcast(
                    BixbyPairUpdateFinishedBroadcast,
                    BixbyPairUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, Tools.CompressStringToByteArray("Update finished")));
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            clearDatabases();
            saveToDatabases();
            sendFailedUpdateBroadcast(exception.toString());
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
            _broadcastController.SendSerializableBroadcast(
                    BixbyPairDeleteFinishedBroadcast,
                    BixbyPairDeleteFinishedBundle,
                    new ObjectChangeFinishedContent(true, Tools.CompressStringToByteArray("Delete finished")));
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            clearDatabases();
            saveToDatabases();
            sendFailedDeleteBroadcast(exception.toString());
        }
    }

    public Date GetLastUpdate() {
        return _lastUpdate;
    }

    private SerializableList<BixbyPair> createPairList() {
        SerializableList<BixbyPair> pairList = new SerializableList<>();

        try {
            SerializableList<BixbyAction> actionList = _databaseBixbyActionList.GetBixbyActionList();
            SerializableList<BixbyRequirement> requirementList = _databaseBixbyRequirementList.GetBixbyRequirementList();

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

                BixbyPair bixbyPair = new BixbyPair(actionId, bixbyAction, pairRequirementList, BixbyPair.DatabaseAction.Null);
                pairList.addValue(bixbyPair);
            }
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            sendFailedLoadBroadcast(exception.toString());
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
        _lastUpdate = new Date();
    }

    private void sendFailedLoadBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Load for bixbypair failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                BixbyPairLoadFinishedBroadcast,
                BixbyPairLoadFinishedBundle,
                new BixbyPairUpdateFinishedContent(_pairList, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedAddBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Add for bixbypair failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                BixbyPairAddFinishedBroadcast,
                BixbyPairAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedUpdateBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Update for bixbypair failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                BixbyPairUpdateFinishedBroadcast,
                BixbyPairUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedDeleteBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Delete for bixbypair failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                BixbyPairDeleteFinishedBroadcast,
                BixbyPairDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }
}
