package guepardoapps.lucahome.views;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.mateware.snacky.Snacky;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.service.UserService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

/**
 * A login screen that offers login via user/password.
 */
public class LoginActivity extends AppCompatActivity {
    private static String TAG = LoginActivity.class.getSimpleName();

    private static final String[] PERMISSIONS_TO_REQUEST = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.MODIFY_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * ReceiverController to register and unregister from broadcasts of the UserService
     */
    private ReceiverController _receiverController;

    // UI references.
    private AutoCompleteTextView _userView;
    private EditText _passwordView;
    private View _loginFormView;
    private View _progressView;

    /**
     * Receiver for the check result of user validation
     */
    private BroadcastReceiver _userCheckReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showProgress(false);

            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(UserService.UserCheckedFinishedBundle);

            if (!result.Success) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Login failed: %s!", Tools.DecompressByteArrayToString(result.Response)));

                displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));

                _passwordView.setError(createErrorText(getString(R.string.error_invalid_password)));
                _userView.setError(createErrorText(getString(R.string.error_invalid_user)));
                _userView.requestFocus();
            } else {
                Snacky.builder()
                        .setActivty(LoginActivity.this)
                        .setText("Successfully logged in!")
                        .setDuration(Snacky.LENGTH_LONG)
                        .success()
                        .show();

                new Handler().postDelayed(() -> {
                    NavigationService.NavigationResult navigationResult = NavigationService.getInstance().GoBack(LoginActivity.this);
                    if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                        Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                        displayErrorSnackBar("Failed to navigate back to BootActivity! Please contact LucaHome support!");
                    }
                }, Snacky.LENGTH_LONG);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _receiverController = new ReceiverController(this);

        if (UserService.getInstance().IsAnUserSaved()) {
            List<String> userList = new ArrayList<>();
            userList.add(UserService.getInstance().GetUser().GetName());
            _userView.setAdapter(new ArrayAdapter<>(LoginActivity.this, android.R.layout.simple_dropdown_item_1line, userList));
        }

        // Set up the login form.
        _userView = findViewById(R.id.user);

        _passwordView = findViewById(R.id.password);
        _passwordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        com.rey.material.widget.Button userSignInButton = findViewById(R.id.user_sign_in_button);
        userSignInButton.setOnClickListener(view -> attemptLogin());

        _loginFormView = findViewById(R.id.login_form);
        _progressView = findViewById(R.id.login_progress);

        Dexter.withActivity(this)
                .withPermissions(PERMISSIONS_TO_REQUEST)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Toasty.success(LoginActivity.this, "All permissions granted! Thanks!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                    }
                }).check();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_userCheckReceiver, new String[]{UserService.UserCheckedFinishedBroadcast});
    }

    @Override
    protected void onPause() {
        super.onPause();
        _receiverController.Dispose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _receiverController.Dispose();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            NavigationService.getInstance().ClearCurrentActivity();
            NavigationService.getInstance().ClearGoBackList();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (UserService.getInstance().ValidatingUser()) {
            return;
        }

        // Reset errors.
        _userView.setError(null);
        _passwordView.setError(null);

        // Store values at the time of the login attempt.
        String user = _userView.getText().toString();
        String password = _passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !UserService.getInstance().IsEnteredPasswordValid(password)) {
            _passwordView.setError(createErrorText(getString(R.string.error_invalid_password)));
            focusView = _passwordView;
            cancel = true;
        }

        // Check for a valid user
        if (TextUtils.isEmpty(user)) {
            _userView.setError(createErrorText(getString(R.string.error_field_required)));
            focusView = _userView;
            cancel = true;
        } else if (!UserService.getInstance().IsEnteredUserValid(user)) {
            _userView.setError(createErrorText(getString(R.string.error_invalid_user)));
            focusView = _userView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to perform the user login attempt.
            showProgress(true);
            UserService.getInstance().ValidateUser(new LucaUser(user, password));
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        // The ViewPropertyAnimator APIs are not available, so simply show and hide the relevant UI components.
        _loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        _progressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Build a custom error text
     */
    private SpannableStringBuilder createErrorText(@NonNull String errorString) {
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(errorString);
        spannableStringBuilder.setSpan(foregroundColorSpan, 0, errorString.length(), 0);
        return spannableStringBuilder;
    }

    private void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(LoginActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }
}

