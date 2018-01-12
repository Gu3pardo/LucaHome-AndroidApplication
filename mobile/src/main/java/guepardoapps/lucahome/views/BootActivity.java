package guepardoapps.lucahome.views;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;

import java.util.List;
import java.util.Locale;

import de.mateware.snacky.Snacky;

import es.dmoral.toasty.Toasty;

import guepardoapps.bixby.services.BixbyService;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.AndroidSystemController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.service.PositioningService;
import guepardoapps.lucahome.common.service.UserService;
import guepardoapps.lucahome.service.MainService;
import guepardoapps.lucahome.service.NavigationService;

public class BootActivity extends AppCompatActivity {
    private static final String TAG = BootActivity.class.getSimpleName();

    /**
     * All permissions needed in the current package
     */
    private static final String[] PERMISSIONS_TO_REQUEST = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.MODIFY_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * Flag if download of all data from server has been finished
     */
    private boolean _downloadFinished;

    /**
     * Flag if user needs to  login
     */
    private boolean _loginAttempt;

    /**
     * Flag if check of AccessibilityService has been finished
     */
    private boolean _checkAccessibilityServiceFinished;

    /**
     * Flag if check of permissions has been finished
     */
    private boolean _checkPermissionsFinished;

    /**
     * Initiate UI
     */
    private ProgressBar _percentProgressBar;
    private TextView _percentProgressTextView;

    /**
     * AndroidSystemController to check for AccessibilityService
     */
    private AndroidSystemController _androidSystemController;

    /**
     * ReceiverController to register and unregister from broadcasts of the UserService
     */
    private ReceiverController _receiverController;

    /**
     * Binder for MainService
     */
    private MainService _mainServiceBinder;

    /**
     * ServiceConnection for MainServiceBinder
     */
    private ServiceConnection _mainServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Logger.getInstance().Debug(TAG, "_mainServiceConnection onServiceConnected");
            _mainServiceBinder = ((MainService.MainServiceBinder) binder).getService();
            if (!UserService.getInstance().IsAnUserSaved()) {
                _loginAttempt = true;
                NavigationService.NavigationResult navigationResult = NavigationService.getInstance().NavigateToActivity(BootActivity.this, LoginActivity.class);
                if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                    Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                    displayErrorSnackBar("Failed to navigate to LoginActivity! Please contact LucaHome support!");
                }
            } else {
                _mainServiceBinder.StartDownloadAll("_mainServiceConnection onServiceConnected");
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Logger.getInstance().Debug(TAG, "_mainServiceConnection onServiceDisconnected");
            _mainServiceBinder = null;
        }
    };

    /**
     * Binder for PositioningService
     */
    private PositioningService _positioningServiceBinder;

    /**
     * ServiceConnection for PositioningServiceBinder
     */
    private ServiceConnection _positioningServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Logger.getInstance().Information(TAG, "onServiceConnected PositioningService");
            _positioningServiceBinder = ((PositioningService.PositioningServiceBinder) binder).getService();
            _positioningServiceBinder.SetActiveActivityContext(BootActivity.this);
        }

        public void onServiceDisconnected(ComponentName className) {
            Logger.getInstance().Information(TAG, "onServiceDisconnected PositioningService");
            _positioningServiceBinder = null;
        }
    };

    /**
     * BroadcastReceiver to receive progress and success state of initial app download
     */
    private BroadcastReceiver _mainServiceDownloadProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.getInstance().Debug(TAG, "_mainServiceDownloadProgressReceiver");
            MainService.MainServiceDownloadCountContent progress = (MainService.MainServiceDownloadCountContent) intent.getSerializableExtra(MainService.MainServiceDownloadCountBundle);
            if (progress != null) {
                _percentProgressBar.setProgress((int) progress.DownloadProgress);
                _percentProgressTextView.setText(String.format(Locale.getDefault(), "%.0f %%", progress.DownloadProgress));
                _downloadFinished = progress.DownloadFinished;
                checkNavigateToMain();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.getInstance().Debug(TAG, "onCreate");

        setContentView(R.layout.activity_boot);

        _percentProgressBar = findViewById(R.id.percentProgressBar);
        _percentProgressBar.setProgress(0);
        _percentProgressTextView = findViewById(R.id.percentProgressTextView);
        _percentProgressTextView.setText("0%");

        _androidSystemController = new AndroidSystemController(this);
        _receiverController = new ReceiverController(this);

        checkAccessibilityService();
        checkPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.getInstance().Debug(TAG, "onStart");
        startService(new Intent(BootActivity.this, MainService.class));
        startService(new Intent(BootActivity.this, PositioningService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.getInstance().Debug(TAG, "onResume");

        if (_mainServiceBinder == null) {
            bindService(new Intent(this, MainService.class), _mainServiceConnection, Context.BIND_AUTO_CREATE);
        }

        if (_positioningServiceBinder == null) {
            bindService(new Intent(this, PositioningService.class), _positioningServiceConnection, Context.BIND_AUTO_CREATE);
        }

        _receiverController.RegisterReceiver(_mainServiceDownloadProgressReceiver, new String[]{MainService.MainServiceDownloadCountBroadcast});

        if (_loginAttempt) {
            _loginAttempt = false;
            _mainServiceBinder.StartDownloadAll("onResume after _loginAttempt");
        }

        checkNavigateToMain();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.getInstance().Debug(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.getInstance().Debug(TAG, "onDestroy");
        _receiverController.Dispose();
        unbindServices();
        _loginAttempt = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "onKeyDown with KeyCode %d and KeyEvent %s", keyCode, keyEvent));
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Snacky.builder()
                    .setActivty(BootActivity.this)
                    .setText("Do you really want to exit during boot?")
                    .setDuration(Snacky.LENGTH_LONG)
                    .setActionText(android.R.string.ok)
                    .setActionClickListener(view -> {
                        if (_mainServiceBinder != null) {
                            _mainServiceBinder.Cancel();
                        }
                        finish();
                    })
                    .warning()
                    .show();
            return true;
        }

        return super.onKeyDown(keyCode, keyEvent);
    }

    private void checkNavigateToMain() {
        Logger.getInstance().Debug(TAG, "checkNavigateToMain");
        if (_downloadFinished && _checkAccessibilityServiceFinished && _checkPermissionsFinished) {
            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().NavigateToActivity(BootActivity.this, MainActivity.class);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate back to MainActivity! Please contact LucaHome support!");
            } else {
                finish();
            }
        }
    }

    private void checkAccessibilityService() {
        Logger.getInstance().Debug(TAG, "checkAccessibilityService");
        if (!_androidSystemController.IsAccessibilityServiceEnabled(BixbyService.SERVICE_ID)) {
            Logger.getInstance().Warning(TAG, "AccessibilityService is NOT enabled! Prompt for enabling!");
            displayPromptForAccessibilityDialog();
        } else {
            Logger.getInstance().Information(TAG, "AccessibilityService is enabled!");
            _checkAccessibilityServiceFinished = true;
        }
    }

    private void checkPermissions() {
        Logger.getInstance().Debug(TAG, "checkPermissions");
        Dexter.withActivity(this)
                .withPermissions(PERMISSIONS_TO_REQUEST)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Toasty.success(BootActivity.this, "All permissions granted! Thanks!", Toast.LENGTH_LONG).show();
                        }
                        _checkPermissionsFinished = true;
                        checkNavigateToMain();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                    }
                }).check();
    }

    private void displayErrorSnackBar(@NonNull String message) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "displayErrorSnackBar with message %s", message));
        Snacky.builder()
                .setActivty(BootActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void displayPromptForAccessibilityDialog() {
        Logger.getInstance().Debug(TAG, "displayPromptForAccessibilityDialog");
        boolean isLightTheme = ThemeManager.getInstance().getCurrentTheme() == 0;

        final Dialog dialog = new Dialog(this);
        dialog
                .title("Activate AccessibilityService?")
                .positiveAction("Yes")
                .negativeAction("No")
                .applyStyle(isLightTheme ? guepardoapps.lucahome.common.R.style.SimpleDialogLight : guepardoapps.lucahome.common.R.style.SimpleDialog)
                .setCancelable(true);

        dialog.positiveActionClickListener(view -> {
            Logger.getInstance().Debug(TAG, "Pressed on yes. Navigating to settings!");

            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            _checkAccessibilityServiceFinished = true;
            dialog.dismiss();
            checkNavigateToMain();
        });

        dialog.negativeActionClickListener(view -> {
            _checkAccessibilityServiceFinished = true;
            dialog.dismiss();
            checkNavigateToMain();
        });

        dialog.show();
    }

    private void unbindServices() {
        Logger.getInstance().Debug(TAG, "unbindServices");

        if (_mainServiceBinder != null) {
            try {
                unbindService(_mainServiceConnection);
            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.getMessage());
            } finally {
                _mainServiceBinder = null;
            }
        }

        if (_positioningServiceBinder != null) {
            try {
                unbindService(_positioningServiceConnection);
            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.getMessage());
            } finally {
                _positioningServiceBinder = null;
            }
        }
    }
}
