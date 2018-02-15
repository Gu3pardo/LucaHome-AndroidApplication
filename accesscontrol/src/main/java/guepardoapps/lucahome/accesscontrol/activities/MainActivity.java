package guepardoapps.lucahome.accesscontrol.activities;

import android.os.Bundle;
import android.view.WindowManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import guepardoapps.lucahome.accesscontrol.R;
import guepardoapps.lucahome.accesscontrol.controller.*;
import guepardoapps.lucahome.accesscontrol.services.*;

public class MainActivity extends Activity {
    private AlarmStateViewController _alarmStateViewController;
    private BatteryViewController _batteryViewController;
    private CenterViewController _centerViewController;
    private CountdownViewController _countdownViewController;
    private IpAddressViewController _ipAddressViewController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        _alarmStateViewController = new AlarmStateViewController(this);
        _batteryViewController = new BatteryViewController(this);
        _centerViewController = new CenterViewController(this);
        _countdownViewController = new CountdownViewController(this);
        _ipAddressViewController = new IpAddressViewController(this);

        _alarmStateViewController.onCreate();
        _batteryViewController.onCreate();
        _centerViewController.onCreate();
        _countdownViewController.onCreate();
        _ipAddressViewController.onCreate();

        startService(new Intent(this, MainService.class));
        startService(new Intent(this, ControlServiceStateService.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        _alarmStateViewController.onResume();
        _batteryViewController.onResume();
        _centerViewController.onResume();
        _countdownViewController.onResume();
        _ipAddressViewController.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        _alarmStateViewController.onPause();
        _batteryViewController.onPause();
        _centerViewController.onPause();
        _countdownViewController.onPause();
        _ipAddressViewController.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _alarmStateViewController.onDestroy();
        _batteryViewController.onDestroy();
        _centerViewController.onDestroy();
        _countdownViewController.onDestroy();
        _ipAddressViewController.onDestroy();
    }
}