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
    private Logger _logger;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private TemperatureService _temperatureService;

    private boolean _isRunning;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver onReceive");
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
                _logger.Error("result is null!");
            }
        }
    };

    private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_performUpdateReceiver onReceive");
            DownloadTemperature();
        }
    };

    public TemperatureUpdater(@NonNull Context context) {
        _logger = new Logger(TAG);

        _broadcastController = new BroadcastController(context);
        _receiverController = new ReceiverController(context);

        _temperatureService = TemperatureService.getInstance();
        _temperatureService.Initialize(context, null, false, true, 15);
    }

    public void Start() {
        _logger.Debug("Initialize");

        if (_isRunning) {
            _logger.Warning("Already running!");
            return;
        }

        _receiverController.RegisterReceiver(_updateReceiver, new String[]{TemperatureService.TemperatureDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_TEMPERATURE_UPDATE});

        _isRunning = true;
        DownloadTemperature();
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void DownloadTemperature() {
        _logger.Debug("startDownloadTemperature");
        _temperatureService.LoadData();
    }
}
