package guepardoapps.mediamirror.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;

import com.google.android.youtube.player.YouTubeBaseActivity;

import guepardoapps.lucahome.basic.controller.PermissionController;
import guepardoapps.lucahome.basic.controller.TTSController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.constants.Enables;
import guepardoapps.lucahome.common.controller.SettingsController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.constants.Constants;
import guepardoapps.mediamirror.controller.*;
import guepardoapps.mediamirror.interfaces.IViewController;
import guepardoapps.mediamirror.services.ControlServiceStateService;
import guepardoapps.mediamirror.services.MainService;

public class FullscreenActivity extends YouTubeBaseActivity {
    private static final String TAG = FullscreenActivity.class.getSimpleName();

    private IViewController _birthdayViewController;
    private IViewController _bottomButtonViewController;
    private IViewController _bottomInfoViewController;
    private IViewController _calendarViewController;
    private CenterViewController _centerViewController;
    private IViewController _dateTimeViewController;
    private RaspberryViewController _raspberryViewController;
    private RssViewController _rssViewController;
    private IViewController _weatherViewController;

    private ScreenController _screenController;
    private TTSController _ttsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsController.getInstance().Initialize(this);
        SettingsController.getInstance().SetUser(new LucaUser(Constants.USER, Constants.PASSWORD));

        checkPermissions();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_fullscreen);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        // | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        _birthdayViewController = new BirthdayViewController(this);
        _bottomButtonViewController = new BottomButtonViewController(this);
        _bottomInfoViewController = new BottomInfoViewController(this);
        _calendarViewController = new CalendarViewController(this);
        _centerViewController = CenterViewController.getInstance();
        _centerViewController.Initialize(this);
        _dateTimeViewController = new DateTimeViewController(this);
        _raspberryViewController = new RaspberryViewController(this);
        _rssViewController = RssViewController.getInstance();
        _rssViewController.Initialize(this);
        _weatherViewController = new WeatherViewController(this);

        _screenController = new ScreenController(this);
        _ttsController = new TTSController(this, Enables.TTS);

        _birthdayViewController.onCreate();
        _bottomButtonViewController.onCreate();
        _bottomInfoViewController.onCreate();
        _calendarViewController.onCreate();
        _centerViewController.onCreate();
        _dateTimeViewController.onCreate();
        _raspberryViewController.onCreate();
        _rssViewController.onCreate();
        _weatherViewController.onCreate();

        _screenController.onCreate();

        _ttsController.Init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        _birthdayViewController.onStart();
        _bottomButtonViewController.onStart();
        _bottomInfoViewController.onStart();
        _calendarViewController.onStart();
        _centerViewController.onStart();
        _dateTimeViewController.onStart();
        _raspberryViewController.onStart();
        _rssViewController.onStart();
        _weatherViewController.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        _birthdayViewController.onResume();
        _bottomButtonViewController.onResume();
        _bottomInfoViewController.onResume();
        _calendarViewController.onResume();
        _centerViewController.onResume();
        _dateTimeViewController.onResume();
        _raspberryViewController.onResume();
        _rssViewController.onResume();
        _weatherViewController.onResume();

        _screenController.onResume();

        startServices();
    }

    @Override
    protected void onPause() {
        super.onPause();

        _birthdayViewController.onPause();
        _bottomButtonViewController.onPause();
        _bottomInfoViewController.onPause();
        _calendarViewController.onPause();
        _centerViewController.onPause();
        _dateTimeViewController.onPause();
        _raspberryViewController.onPause();
        _rssViewController.onPause();
        _weatherViewController.onPause();

        _screenController.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        _birthdayViewController.onDestroy();
        _bottomButtonViewController.onDestroy();
        _bottomInfoViewController.onDestroy();
        _calendarViewController.onDestroy();
        _centerViewController.onDestroy();
        _dateTimeViewController.onDestroy();
        _raspberryViewController.onDestroy();
        _rssViewController.onDestroy();
        _weatherViewController.onDestroy();

        _screenController.onDestroy();
        _ttsController.Dispose();
    }

    @Override
    public void onRequestPermissionsResult(int callbackId, @NonNull String permissions[], @NonNull int[] grantResults) {
        int index = 0;
        for (String permission : permissions) {
            Logger.getInstance().Information(TAG, String.format("Permission %s has been granted: %s", permission, grantResults[index]));
            index++;
        }
    }

    public void ShowTemperatureGraph(View view) {
        _raspberryViewController.ShowTemperatureGraph(view);
    }

    private void startServices() {
        startService(new Intent(this, MainService.class));
        startService(new Intent(this, ControlServiceStateService.class));
    }

    private void checkPermissions() {
        PermissionController permissionController = new PermissionController(this);
        permissionController.CheckPermissions(
                Constants.PERMISSION_REQUEST_WRITE_SETTINGS_ID,
                Manifest.permission.WRITE_SETTINGS);
        permissionController.CheckPermissions(
                Constants.PERMISSION_REQUEST_READ_CALENDAR,
                Manifest.permission.READ_CALENDAR);
        permissionController.CheckPermissions(
                Constants.PERMISSION_REQUEST_READ_CONTACTS,
                Manifest.permission.READ_CONTACTS);
    }
}
