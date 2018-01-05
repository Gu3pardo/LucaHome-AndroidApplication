package guepardoapps.lucahome.bixby;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.ref.WeakReference;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.Position;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.service.PositioningService;
import guepardoapps.lucahome.common.service.WirelessSocketService;

@SuppressWarnings("unused")
public class BixbyService extends AccessibilityService {
    private static final String TAG = BixbyService.class.getSimpleName();
    private static final String BIXBY_PACKAGE = "com.samsung.android.app.spage";

    private long _lastRunMillis = 0;
    private long _maxRunFrequencyMs = 500;

    private Position _lastPosition;

    private NetworkController _networkController;
    private ReceiverController _receiverController;

    private DatabaseBixbyActionList _databaseBixbyActionList;
    private DatabaseBixbyRequirementList _databaseBixbyRequirementList;

    private SerializableList<BixbyPair> _pairList;

    @SuppressWarnings("FieldCanBeLocal")
    private BroadcastReceiver _positioningUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PositioningService.PositioningUpdateFinishedContent positionResult = (PositioningService.PositioningUpdateFinishedContent) intent.getSerializableExtra(PositioningService.PositioningCalulationFinishedBundle);
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

    public BixbyService() {
        super();
        Logger.getInstance().Debug(TAG, "Constructor");
        _networkController = new NetworkController(this);
        _receiverController = new ReceiverController(this);

        _receiverController.RegisterReceiver(_positioningUpdateReceiver, new String[]{PositioningService.PositioningCalulationFinishedBroadcast});

        _databaseBixbyActionList = new DatabaseBixbyActionList(this);
        _databaseBixbyActionList.Open();

        _databaseBixbyRequirementList = new DatabaseBixbyRequirementList(this);
        _databaseBixbyRequirementList.Open();

        _pairList = createPairList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _receiverController.Dispose();
        Logger.getInstance().Warning(TAG, "onDestroy");
    }

    @Override
    protected void onServiceConnected() {
        Logger.getInstance().Debug(TAG, "onServiceConnected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String activeWindowPackage = getActiveWindowPackage();

        Logger.getInstance().Verbose(TAG, String.format(
                "onAccessibilityEvent: [type] %s [time] %s [activeWindowPackage] %s",
                AccessibilityEvent.eventTypeToString(event.getEventType()), event.getEventTime(), activeWindowPackage));

        long currentMillis = System.currentTimeMillis();
        long maxRunFrequencyMs = GetMaxRunFrequencyMs();
        boolean runTooSoon = (currentMillis - _lastRunMillis) < maxRunFrequencyMs;

        if (runTooSoon || !BIXBY_PACKAGE.equals(activeWindowPackage)) {
            return;
        }

        for (int index = 0; index < _pairList.getSize(); index++) {
            boolean allRequirementsTrue = true;

            BixbyPair bixbyPair = _pairList.getValue(index);

            BixbyAction bixbyAction = bixbyPair.GetAction();
            SerializableList<BixbyRequirement> requirementList = bixbyPair.GetRequirements();

            for (int requirementIndex = 0; requirementIndex < requirementList.getSize(); requirementIndex++) {
                BixbyRequirement bixbyRequirement = requirementList.getValue(requirementIndex);

                switch (bixbyRequirement.GetRequirementType()) {
                    case Position:
                        allRequirementsTrue &= validatePositionRequirement(bixbyRequirement);
                        break;

                    case Light:
                        LightRequirement lightRequirement = bixbyRequirement.GetLightRequirement();
                        allRequirementsTrue &= validateLightRequirement(lightRequirement);
                        break;

                    case Network:
                        NetworkRequirement networkRequirement = bixbyRequirement.GetNetworkRequirement();
                        allRequirementsTrue &= validateNetworkRequirement(networkRequirement);
                        break;

                    case WirelessSocket:
                        WirelessSocketRequirement wirelessSocketRequirement = bixbyRequirement.GetWirelessSocketRequirement();
                        allRequirementsTrue &= validateWirelessSocketRequirement(wirelessSocketRequirement);
                        break;

                    default:
                        Logger.getInstance().Error(TAG, "Invalid BixbyRequirement!");
                        allRequirementsTrue = false;
                        break;
                }
            }

            if (allRequirementsTrue) {
                Logger.getInstance().Information(TAG, "All requirements true! Running callback!");
                try {
                    performAction(bixbyAction);
                } catch (Exception exception) {
                    Logger.getInstance().Error(TAG, exception.toString());
                }
            }
        }

        _lastRunMillis = currentMillis;
        new DelayedBackButtonTask(this).execute();
    }

    @Override
    public void onInterrupt() {
        Logger.getInstance().Warning(TAG, "onInterrupt");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.getInstance().Verbose(TAG, "onUnbind");
        return false;
    }

    public long GetMaxRunFrequencyMs() {
        return _maxRunFrequencyMs;
    }

    public void SetMaxRunFrequencyMs(long maxRunFrequencyMs) {
        _maxRunFrequencyMs = maxRunFrequencyMs;
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

    private String getActiveWindowPackage() {
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        return rootInActiveWindow != null ? rootInActiveWindow.getPackageName().toString() : null;
    }

    private boolean validatePositionRequirement(@NonNull BixbyRequirement bixbyRequirement) {
        return _lastPosition != null && bixbyRequirement.GetPuckJsPosition().contains(_lastPosition.GetPuckJs().GetArea());
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

    private void performNetworkAction(@NonNull NetworkAction networkAction) throws Exception {
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

    private static class DelayedBackButtonTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<BixbyService> _bixbyServiceWeakReference;

        DelayedBackButtonTask(BixbyService context) {
            _bixbyServiceWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Logger.getInstance().Error(TAG, "interrupted");
            }

            BixbyService bixbyService = _bixbyServiceWeakReference.get();
            if (bixbyService != null) {
                bixbyService.performGlobalAction(GLOBAL_ACTION_BACK);
            }

            return null;
        }
    }
}
