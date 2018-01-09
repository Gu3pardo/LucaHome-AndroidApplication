package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.service.TemperatureService;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.models.RaspberryModel;

public class TemperatureUpdater {
    private static final String TAG = TemperatureUpdater.class.getSimpleName();

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private boolean _isRunning;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TemperatureService.TemperatureDownloadFinishedContent result = (TemperatureService.TemperatureDownloadFinishedContent) intent.getSerializableExtra(TemperatureService.TemperatureDownloadFinishedBundle);
            if (result != null) {
                RaspberryModel model = null;

                if (result.TemperatureList != null) {
                    if (result.TemperatureList.getSize() > 0) {
                        model = new RaspberryModel(
                                result.TemperatureList.getValue(0).GetArea(),
                                result.TemperatureList.getValue(0).GetTemperatureString(),
                                result.TemperatureList.getValue(0).GetGraphPath());

                    }
                }

                if (model == null) {
                    model = new RaspberryModel("not found", "", "");
                }

                _broadcastController.SendSerializableBroadcast(
                        Broadcasts.SHOW_RASPBERRY_DATA_MODEL,
                        Bundles.RASPBERRY_DATA_MODEL,
                        model);
            } else {
                Logger.getInstance().Error(TAG, "result is null!");
            }
        }
    };

    private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadTemperature();
        }
    };

    public TemperatureUpdater(@NonNull Context context) {
        _broadcastController = new BroadcastController(context);
        _receiverController = new ReceiverController(context);
        TemperatureService.getInstance().Initialize(context, null, false, true, 15);
    }

    public void Start() {
        if (_isRunning) {
            Logger.getInstance().Warning(TAG, "Already running!");
            return;
        }
        _receiverController.RegisterReceiver(_updateReceiver, new String[]{TemperatureService.TemperatureDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_TEMPERATURE_UPDATE});
        _isRunning = true;
        DownloadTemperature();
    }

    public void Dispose() {
        TemperatureService.getInstance().Dispose();
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void DownloadTemperature() {
        TemperatureService.getInstance().LoadData();
    }
}
