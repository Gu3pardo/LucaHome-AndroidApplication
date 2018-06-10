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
import guepardoapps.lucahome.bixby.classes.actions.BixbyAction;
import guepardoapps.lucahome.bixby.classes.requirements.BixbyRequirement;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.UserInformationController;
import guepardoapps.lucahome.common.services.PositioningService;
import guepardoapps.lucahome.common.services.WirelessSocketService;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
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
    private Position _lastReceivedPosition = new Position(new PuckJs(UUID.randomUUID(), UUID.randomUUID(), "", "", false, ILucaClass.LucaServerDbAction.Null), -1);
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

    private void performApplicationAction(@NonNull ApplicationAction applicationAction) {
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

    private void performNetworkAction(@NonNull NetworkAction networkAction) {
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

    private void performWirelessSwitchAction(@NonNull WirelessSwitchAction wirelessSwitchAction) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "performWirelessSwitchAction for %s", wirelessSwitchAction));
        WirelessSwitch wirelessSwitch = WirelessSwitchService.getInstance().GetByName(wirelessSwitchAction.GetWirelessSwitchName());
        WirelessSwitchService.getInstance().ToggleWirelessSwitch(wirelessSwitch);
    }
}
