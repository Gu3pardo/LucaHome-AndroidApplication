package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.controller.UserInformationController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.models.IpAddressModel;

public class IpAddressViewUpdater {
    private static final String TAG = IpAddressViewUpdater.class.getSimpleName();

    private Handler _updater;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;
    private UserInformationController _userInformationController;

    private int _updateTime;
    private boolean _isRunning;

    private Runnable _updateRunnable = new Runnable() {
        public void run() {
            GetCurrentLocalIpAddress();
            _updater.postDelayed(_updateRunnable, _updateTime);
        }
    };

    private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            GetCurrentLocalIpAddress();
        }
    };

    public IpAddressViewUpdater(@NonNull Context context) {
        _updater = new Handler();
        _broadcastController = new BroadcastController(context);
        _receiverController = new ReceiverController(context);
        _userInformationController = new UserInformationController(context);
    }

    public void Start(int updateTime) {
        if (_isRunning) {
            Logger.getInstance().Warning(TAG, "Already running!");
            return;
        }

        _updateTime = updateTime;
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_IP_ADDRESS_UPDATE});
        _updateRunnable.run();

        _isRunning = true;
        GetCurrentLocalIpAddress();
    }

    public void Dispose() {
        _updater.removeCallbacks(_updateRunnable);
        _receiverController.Dispose();
        _isRunning = false;
    }

    public IpAddressModel GetCurrentLocalIpAddress() {
        String ip = _userInformationController.GetIp();
        IpAddressModel model = new IpAddressModel(true, ip);
        _broadcastController.SendSerializableBroadcast(
                Broadcasts.SHOW_IP_ADDRESS_MODEL,
                Bundles.IP_ADDRESS_MODEL,
                model);
        return model;
    }
}
