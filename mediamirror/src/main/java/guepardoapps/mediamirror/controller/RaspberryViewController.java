package guepardoapps.mediamirror.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.models.RaspberryModel;
import guepardoapps.mediamirror.helper.RaspberryTemperatureHelper;
import guepardoapps.mediamirror.interfaces.IViewController;

public class RaspberryViewController implements IViewController {
    private static final String TAG = RaspberryViewController.class.getSimpleName();

    private boolean _isInitialized;
    private boolean _screenEnabled;

    private RaspberryModel _raspberryModel;

    private Context _context;
    private DialogController _dialogController;
    private ReceiverController _receiverController;

    private RaspberryTemperatureHelper _raspberryTemperatureHelper;

    private View _temperatureRaspberryAlarm;
    private TextView _temperatureRaspberryName;
    private TextView _temperatureRaspberryValue;

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = false;
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = true;
        }
    };

    private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                return;
            }

            RaspberryModel model = (RaspberryModel) intent.getSerializableExtra(Bundles.RASPBERRY_DATA_MODEL);

            if (model != null) {
                _raspberryModel = model;
                _temperatureRaspberryAlarm.setBackgroundResource(_raspberryTemperatureHelper.GetIcon(_raspberryModel.GetRaspberryTemperature()));
                _temperatureRaspberryName.setText(_raspberryModel.GetRaspberryName());
                _temperatureRaspberryValue.setText(_raspberryModel.GetRaspberryTemperature());
            } else {
                Logger.getInstance().Warning(TAG, "model is null!");
            }
        }
    };

    public RaspberryViewController(@NonNull Context context) {
        _context = context;
        _dialogController = new DialogController(_context);
        _receiverController = new ReceiverController(_context);
        _raspberryTemperatureHelper = new RaspberryTemperatureHelper();
    }

    @Override
    public void onCreate() {
        _screenEnabled = true;
        _temperatureRaspberryAlarm = ((Activity) _context).findViewById(R.id.temperatureRaspberryAlarm);
        _temperatureRaspberryName = ((Activity) _context).findViewById(R.id.temperatureRaspberryName);
        _temperatureRaspberryValue = ((Activity) _context).findViewById(R.id.temperatureRaspberryValue);
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_updateViewReceiver, new String[]{Broadcasts.SHOW_RASPBERRY_DATA_MODEL});
            _isInitialized = true;
        } else {
            Logger.getInstance().Warning(TAG, "Is ALREADY initialized!");
        }
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public void ShowTemperatureGraph(View view) {
        if (_raspberryModel == null) {
            Toasty.error(_context, "No link!", Toast.LENGTH_LONG).show();
            return;
        }

        String url = _raspberryModel.GetRaspberryTemperatureGraphUrl();
        if (url.length() > 0) {
            _dialogController.DisplayTemperatureDialog(_raspberryModel.GetRaspberryTemperatureGraphUrl());
        } else {
            Logger.getInstance().Warning(TAG, "invalid URL!");
            Toasty.warning(_context, "Invalid URL!", Toast.LENGTH_LONG).show();
        }
    }
}
