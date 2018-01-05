package guepardoapps.lucahome.common.service;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;

import java.util.Collection;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BluetoothController;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.Position;
import guepardoapps.lucahome.common.classes.PuckJs;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

@SuppressWarnings("unused")
public class PositioningService extends Service implements BeaconConsumer {
    private static final String TAG = PositioningService.class.getSimpleName();

    public class PositioningServiceBinder extends Binder {
        public PositioningService getService() {
            return PositioningService.this;
        }
    }

    public static class PositioningUpdateFinishedContent extends ObjectChangeFinishedContent {
        public Position LatestPosition;

        PositioningUpdateFinishedContent(Position latestPosition, boolean succcess) {
            super(succcess, new byte[]{});
            LatestPosition = latestPosition;
        }
    }

    public static final String PositioningCalulationFinishedBroadcast = "guepardoapps.lucahome.common.service.positioning.calculation.finished";
    public static final String PositioningCalulationFinishedBundle = "PositioningCalulationFinishedBundle";

    private boolean _isInitialized;
    private boolean _permissionGranted;

    private Context _context;
    private Context _activeActivityContext;

    private BluetoothController _bluetoothController;
    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private static final int MIN_BETWEEN_SCAN_SEC = 15;
    private static final int MAX_BETWEEN_SCAN_SEC = 60 * 60;

    private static final int MIN_SCAN_MSEC = 500;
    private static final int MAX_SCAN_MSEC = 5 * 60 * 1000;

    private boolean _bluetoothIsEnabled;
    private boolean _scanEnabled;
    private boolean _handleBluetoothAutomatically;

    private Collection<Beacon> _beaconList;

    private BeaconManager _beaconManager;

    /*private MonitorNotifier _monitorNotifier = new MonitorNotifier() {
        @Override
        public void didEnterRegion(Region region) {
            Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "didEnterRegion Region: %s", region.toString()));
            calculatePosition();
        }

        @Override
        public void didExitRegion(Region region) {
            Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "didExitRegion Region: %s", region.toString()));
            calculatePosition();
        }

        @Override
        public void didDetermineStateForRegion(int state, Region region) {
            Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "didDetermineStateForRegion Region: %s, State: %d", region.toString(), state));
        }
    };*/

    private RangeNotifier _rangeNotifier = (beaconList, region) -> {
        _beaconList = beaconList;

        if (_beaconList.size() > 0) {
            for (int beaconIndex = 0; beaconIndex < _beaconList.size(); beaconIndex++) {
                Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "didRangeBeaconsInRegion: Found Beacon %s", _beaconList.toArray()[beaconIndex].toString()));
            }
        }

        calculatePosition();
    };

    private BroadcastReceiver _bluetoothChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) {
                Logger.getInstance().Error(TAG, "_bluetoothChangedReceiver action is null");
                return;
            }

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        _bluetoothIsEnabled = false;
                        if (_beaconManager.isBound(PositioningService.this)) {
                            _beaconManager.unbind(PositioningService.this);
                        }
                        break;

                    case BluetoothAdapter.STATE_ON:
                        _bluetoothIsEnabled = true;
                        SetScanEnabled(_scanEnabled);
                        break;

                    default:
                        break;
                }
            }

        }
    };

    private BroadcastReceiver _homeNetworkAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SetScanEnabled(_scanEnabled);
        }
    };

    private BroadcastReceiver _homeNetworkNotAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (_beaconManager.isBound(PositioningService.this)) {
                _beaconManager.unbind(PositioningService.this);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onBeaconServiceConnect() {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "onBeaconServiceConnect, _scanEnabled: %s", _scanEnabled));
        //_beaconManager.addMonitorNotifier(_monitorNotifier);
        _beaconManager.addRangeNotifier(_rangeNotifier);
    }

    @Override
    public Context getApplicationContext() {
        Logger.getInstance().Debug(TAG, "getApplicationContext");
        return _context.getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        Logger.getInstance().Debug(TAG, "unbindService");
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        Logger.getInstance().Debug(TAG, "bindService");
        return false;
    }

    public void Initialize(
            @NonNull Context context, @NonNull Context activeActivityContext,
            boolean scanEnabled, boolean handleBluetoothAutomatically,
            long betweenScanPeriod, long scanPeriod) {
        if (_isInitialized) {
            Logger.getInstance().Warning(TAG, "Already initialized!");
            return;
        }

        _context = context;
        _beaconManager = BeaconManager.getInstanceForApplication(_context);

        _bluetoothController = new BluetoothController();
        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);

        _receiverController.RegisterReceiver(_bluetoothChangedReceiver, new String[]{BluetoothAdapter.ACTION_STATE_CHANGED});
        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        _bluetoothIsEnabled = _bluetoothController.IsBluetoothEnabled();

        SetActiveActivityContext(activeActivityContext);
        SetScanEnabled(scanEnabled);
        SetHandleBluetoothAutomatically(handleBluetoothAutomatically);
        SetBetweenScanPeriod(betweenScanPeriod);
        SetScanPeriod(scanPeriod);

        _isInitialized = true;
    }

    public void Dispose() {
        _receiverController.Dispose();
        if (_beaconManager.isBound(PositioningService.this)) {
            _beaconManager.unbind(PositioningService.this);
        }
        _isInitialized = false;
    }

    public void SetActiveActivityContext(Context activeActivityContext) {
        _activeActivityContext = activeActivityContext;
    }

    public Context GetActiveActivityContext() {
        return _activeActivityContext;
    }

    public void SetHandleBluetoothAutomatically(boolean handleBluetoothAutomatically) {
        _handleBluetoothAutomatically = handleBluetoothAutomatically;

        if (_activeActivityContext == null) {
            Logger.getInstance().Error(TAG, "_activeActivityContext is null!");
            return;
        }

        if (_handleBluetoothAutomatically) {
            try {
                Dexter.withActivity((Activity) _activeActivityContext)
                        .withPermission(Manifest.permission.BLUETOOTH_ADMIN)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                _permissionGranted = true;
                                SetScanEnabled(_scanEnabled);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                _permissionGranted = false;
                                Toasty.error(_activeActivityContext, "BluetoothPermission not granted! Beacon not working!", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            }
                        });
            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.toString());
                Toasty.error(_context, exception.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean GetHandleBluetoothAutomatically() {
        return _handleBluetoothAutomatically;
    }

    public void SetScanEnabled(boolean scanEnabled) {
        _scanEnabled = scanEnabled;
        if (validateBluetooth() && _scanEnabled && !_beaconManager.isBound(this)) {
            _beaconManager.bind(this);
        }
    }

    public boolean GetScanEnabled() {
        return _scanEnabled;
    }

    public void SetBetweenScanPeriod(long betweenScanPeriod) {
        if (betweenScanPeriod < MIN_BETWEEN_SCAN_SEC * 1000) {
            betweenScanPeriod = MIN_BETWEEN_SCAN_SEC * 1000;
        }
        if (betweenScanPeriod > MAX_BETWEEN_SCAN_SEC * 1000) {
            betweenScanPeriod = MAX_BETWEEN_SCAN_SEC * 1000;
        }

        _beaconManager.setBackgroundBetweenScanPeriod(betweenScanPeriod);
        _beaconManager.setForegroundBetweenScanPeriod(betweenScanPeriod);
    }

    public long GetBackgroundBetweenScanPeriod() {
        return _beaconManager.getBackgroundBetweenScanPeriod();
    }

    public long GetForegroundBetweenScanPeriod() {
        return _beaconManager.getForegroundBetweenScanPeriod();
    }

    public void SetScanPeriod(long scanPeriod) {
        if (scanPeriod < MIN_SCAN_MSEC) {
            scanPeriod = MIN_SCAN_MSEC;
        }
        if (scanPeriod > MAX_SCAN_MSEC) {
            scanPeriod = MAX_SCAN_MSEC;
        }

        _beaconManager.setBackgroundScanPeriod(scanPeriod);
        _beaconManager.setForegroundScanPeriod(scanPeriod);
    }

    public long GetBackgroundScanPeriod() {
        return _beaconManager.getBackgroundScanPeriod();
    }

    public long GetForegroundScanPeriod() {
        return _beaconManager.getForegroundScanPeriod();
    }

    private boolean validateBluetooth() {
        if (_bluetoothIsEnabled) {
            return true;
        } else {
            if (_permissionGranted) {
                _bluetoothController.SetNewBluetoothState(true);
                return true;
            } else {
                return false;
            }
        }
    }

    private void calculatePosition() {
        Logger.getInstance().Debug(TAG, "calculatePosition");

        if (_beaconList == null) {
            Logger.getInstance().Error(TAG, "_beaconList is null!");
            return;
        }

        if (_beaconList.size() == 0) {
            Logger.getInstance().Error(TAG, "_beaconList has size 0!");
            return;
        }

        if (_beaconList == null || _beaconList.size() == 0) {
            Logger.getInstance().Error(TAG, "Invalid BeaconList! Cannot calculate position!");
            return;
        }

        SerializableList<PuckJs> puckJsList = PuckJsListService.getInstance().GetDataList();
        if (puckJsList == null || puckJsList.getSize() == 0) {
            Logger.getInstance().Error(TAG, "Invalid PuckJsList! Cannot calculate position!");
            return;
        }

        // TODO create calculation (ASYNC!?!?!)
        Logger.getInstance().Information(TAG, "Calculating... TODO");

        _broadcastController.SendSerializableBroadcast(
                PositioningCalulationFinishedBroadcast,
                PositioningCalulationFinishedBundle,
                new PositioningUpdateFinishedContent(
                        new Position(
                                new PuckJs(-1, "", "", "", false, ILucaClass.LucaServerDbAction.Null), -1)
                        , true));
    }
}
