package guepardoapps.lucahome.common.services;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
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
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.Position;
import guepardoapps.lucahome.common.classes.PuckJs;
import guepardoapps.lucahome.common.controller.BluetoothController;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings("WeakerAccess")
public class PositioningService extends Service implements BeaconConsumer, MonitorNotifier, RangeNotifier, IPositioningService {
    private static final String Tag = PositioningService.class.getSimpleName();

    public class PositionCalculationObject implements Serializable {
        public boolean Valid;
        public Position CalculatedPosition;

        PositionCalculationObject(boolean valid, Position calculatedPosition) {
            Valid = valid;
            CalculatedPosition = calculatedPosition;
        }
    }

    public class PositioningServiceBinder extends Binder {
        public PositioningService getService() {
            Logger.getInstance().Debug(Tag, "PositioningServiceBinder getService");
            return PositioningService.this;
        }
    }

    private final IBinder _positioningServiceBinder = new PositioningServiceBinder();

    private boolean _permissionGranted;

    private Context _context;
    private Context _activeActivityContext;

    private Position _currentPosition;

    private BluetoothController _bluetoothController;
    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private static final int MinBetweenScanSec = 15;
    private static final int MaxBetweenScanSec = 60 * 60;

    private static final int MinScanMSec = 500;
    private static final int MaxScanMSec = 5 * 60 * 1000;

    private boolean _bluetoothIsEnabled;
    private boolean _scanEnabled;
    private boolean _handleBluetoothAutomatically;

    private ArrayList<Region> _previousRegionList = new ArrayList<>();
    private ArrayList<Region> _activeRegionList = new ArrayList<>();
    private Collection<Beacon> _beaconList;

    private BeaconManager _beaconManager;

    private BroadcastReceiver _bluetoothChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) {
                Logger.getInstance().Error(Tag, "_bluetoothChangedReceiver action is null");
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

    private BroadcastReceiver _puckJsDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setActiveRegionList();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return _positioningServiceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _context = this;
        _beaconManager = BeaconManager.getInstanceForApplication(_context.getApplicationContext());

        _currentPosition = new Position(new PuckJs(UUID.randomUUID(), UUID.randomUUID(), "", "", false, ILucaClass.LucaServerDbAction.Null), -1);

        BeaconParser beaconParser = new BeaconParser();
        _beaconManager.getBeaconParsers().add(beaconParser.setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));
        _beaconManager.getBeaconParsers().add(beaconParser.setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        _beaconManager.getBeaconParsers().add(beaconParser.setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        _beaconManager.getBeaconParsers().add(beaconParser.setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        _beaconManager.getBeaconParsers().add(beaconParser.setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT));

        _bluetoothController = new BluetoothController();
        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);

        _receiverController.RegisterReceiver(_bluetoothChangedReceiver, new String[]{BluetoothAdapter.ACTION_STATE_CHANGED});
        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_puckJsDownloadReceiver, new String[]{PuckJsService.PuckJsDownloadFinishedBroadcast});

        _bluetoothIsEnabled = _bluetoothController.IsBluetoothEnabled();

        SetScanEnabled(SettingsController.getInstance().IsBeaconScanEnabled());
        SetHandleBluetoothAutomatically(SettingsController.getInstance().HandleBluetoothAutomatically());
        SetBetweenScanPeriod(SettingsController.getInstance().GetTimeBetweenBeaconScansSec());
        SetScanPeriod(SettingsController.getInstance().GetTimeBeaconScansMsec());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _receiverController.Dispose();
        if (_beaconManager.isBound(PositioningService.this)) {
            _beaconManager.unbind(PositioningService.this);
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "onBeaconServiceConnect, _scanEnabled: %s", _scanEnabled));
        setActiveRegionList();
    }

    @Override
    public void didEnterRegion(Region region) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "didEnterRegion Region: %s", region.toString()));
        // TODO
        //calculatePosition();
    }

    @Override
    public void didExitRegion(Region region) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "didExitRegion Region: %s", region.toString()));
        // TODO
        //calculatePosition();
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "didDetermineStateForRegion Region: %s, State: %d", region.toString(), state));
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beaconList, Region region) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "RangeNotifier: BeaconList: %s | Region: %s", beaconList, region));

        _beaconList = beaconList;

        if (_beaconList.size() > 0) {
            for (int beaconIndex = 0; beaconIndex < _beaconList.size(); beaconIndex++) {
                Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "didRangeBeaconsInRegion: Found Beacon %s", _beaconList.toArray()[beaconIndex].toString()));
            }
        }

        calculatePosition();
    }

    @Override
    public void SetActiveActivityContext(@NonNull Context activeActivityContext) {
        _activeActivityContext = activeActivityContext;
        askForPermission();
    }

    @Override
    public Context GetActiveActivityContext() {
        return _activeActivityContext;
    }

    @Override
    public Position GetCurrentPosition() {
        return _currentPosition;
    }

    @Override
    public void SetHandleBluetoothAutomatically(boolean handleBluetoothAutomatically) {
        _handleBluetoothAutomatically = handleBluetoothAutomatically;
        if (_activeActivityContext == null) {
            Logger.getInstance().Error(Tag, "_activeActivityContext is null!");
            return;
        }
        if (_handleBluetoothAutomatically) {
            askForPermission();
        }
    }

    @Override
    public boolean GetHandleBluetoothAutomatically() {
        return _handleBluetoothAutomatically;
    }

    @Override
    public void SetScanEnabled(boolean scanEnabled) {
        _scanEnabled = scanEnabled;
        if (_scanEnabled && !_beaconManager.isBound(this)) {
            if (validateBluetooth()) {
                _beaconManager.bind(this);
            } else {
                Logger.getInstance().Warning(Tag, "Validating bluetooth failed!");
            }
        } else if (!_scanEnabled && _beaconManager.isBound(this)) {
            _beaconManager.unbind(this);
        }
    }

    @Override
    public boolean GetScanEnabled() {
        return _scanEnabled;
    }

    @Override
    public void SetBetweenScanPeriod(long betweenScanPeriod) {
        if (betweenScanPeriod < MinBetweenScanSec * 1000) {
            betweenScanPeriod = MinBetweenScanSec * 1000;
        }
        if (betweenScanPeriod > MaxBetweenScanSec * 1000) {
            betweenScanPeriod = MaxBetweenScanSec * 1000;
        }

        _beaconManager.setBackgroundBetweenScanPeriod(betweenScanPeriod);
        _beaconManager.setForegroundBetweenScanPeriod(betweenScanPeriod);
    }

    @Override
    public long GetBackgroundBetweenScanPeriod() {
        return _beaconManager.getBackgroundBetweenScanPeriod();
    }

    @Override
    public long GetForegroundBetweenScanPeriod() {
        return _beaconManager.getForegroundBetweenScanPeriod();
    }

    @Override
    public void SetScanPeriod(long scanPeriod) {
        if (scanPeriod < MinScanMSec) {
            scanPeriod = MinScanMSec;
        }
        if (scanPeriod > MaxScanMSec) {
            scanPeriod = MaxScanMSec;
        }

        _beaconManager.setBackgroundScanPeriod(scanPeriod);
        _beaconManager.setForegroundScanPeriod(scanPeriod);
    }

    @Override
    public long GetBackgroundScanPeriod() {
        return _beaconManager.getBackgroundScanPeriod();
    }

    @Override
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
        if (_beaconList == null) {
            Logger.getInstance().Error(Tag, "_beaconList is null!");
            return;
        }

        if (_beaconList.size() == 0) {
            Logger.getInstance().Error(Tag, "_beaconList has size 0!");
            return;
        }

        ArrayList<PuckJs> puckJsList = PuckJsService.getInstance().GetDataList();
        if (puckJsList == null || puckJsList.size() == 0) {
            Logger.getInstance().Error(Tag, "Invalid PuckJsList! Cannot calculate position!");
            return;
        }

        // TODO create calculation (ASYNC!?!?!)
        Logger.getInstance().Information(Tag, "Calculating... TODO");
        _currentPosition = new Position(new PuckJs(UUID.randomUUID(), UUID.randomUUID(), "", "", false, ILucaClass.LucaServerDbAction.Null), -1);

        _broadcastController.SendSerializableBroadcast(PositioningCalculationFinishedBroadcast, PositioningCalculationFinishedBundle, new PositionCalculationObject(true, _currentPosition));
    }

    private void askForPermission() {
        try {
            Dexter.withActivity((Activity) _activeActivityContext)
                    .withPermission(Manifest.permission.BLUETOOTH_ADMIN)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            _permissionGranted = true;
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
            Logger.getInstance().Error(Tag, exception.toString());
            Toasty.error(_context, exception.toString(), Toast.LENGTH_LONG).show();
        } finally {
            SetScanEnabled(_scanEnabled);
        }
    }

    private void setActiveRegionList() {
        _previousRegionList = _activeRegionList;
        _activeRegionList.clear();

        ArrayList<PuckJs> puckJsList = PuckJsService.getInstance().GetDataList();
        for (PuckJs puckJs : puckJsList) {
            Region region = new Region(puckJs.GetUuid().toString(), puckJs.GetMac());
            _activeRegionList.add(region);
        }

        setMonitorNotifiers();
        setRangeNotifiers();
    }

    private void setMonitorNotifiers() {
        // Remove previous notifier
        try {
            for (Region region : _previousRegionList) {
                Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "stopMonitoringBeaconsInRegion for region %s", region));
                _beaconManager.stopMonitoringBeaconsInRegion(region);
            }
        } catch (RemoteException remoteException) {
            Logger.getInstance().Error(Tag, remoteException.toString());
        }
        _beaconManager.removeAllMonitorNotifiers();

        // Add new notifier
        _beaconManager.addMonitorNotifier(this);
        try {
            for (Region region : _activeRegionList) {
                Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "startMonitoringBeaconsInRegion for region %s", region));
                _beaconManager.startMonitoringBeaconsInRegion(region);
            }
        } catch (RemoteException remoteException) {
            Logger.getInstance().Error(Tag, remoteException.toString());
        }
    }

    private void setRangeNotifiers() {
        // Remove previous notifier
        try {
            for (Region region : _previousRegionList) {
                Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "stopRangingBeaconsInRegion for region %s", region));
                _beaconManager.stopRangingBeaconsInRegion(region);
            }
        } catch (RemoteException remoteException) {
            Logger.getInstance().Error(Tag, remoteException.toString());
        }
        _beaconManager.removeAllRangeNotifiers();

        // Add new notifier
        _beaconManager.addRangeNotifier(this);
        try {
            for (Region region : _activeRegionList) {
                Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "startRangingBeaconsInRegion for region %s", region));
                _beaconManager.startRangingBeaconsInRegion(region);
            }
        } catch (RemoteException remoteException) {
            Logger.getInstance().Error(Tag, remoteException.toString());
        }
    }
}
