package guepardoapps.lucahome.views;

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
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.mateware.snacky.Snacky;

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
    private Logger _logger;

    /**
     * ReceiverController to register and unregister from broadcasts of the UserService
     */
    private ReceiverController _receiverController;

    /**
     * NavigationService manages navigation between activities
     */
    private NavigationService _navigationService;

    /**
     * UserService manages the handles for the user: validation, etc.
     */
    private UserService _userService;

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
            _logger.Debug("_userCheckReceiver");
            showProgress(false);

            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(UserService.UserCheckedFinishedBundle);

            if (!result.Success) {
                _logger.Error(String.format(Locale.getDefault(), "Login failed: %s!", result.Response));

                displayFailSnacky(Tools.DecompressByteArrayToString(result.Response));

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

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        NavigationService.NavigationResult navigationResult = _navigationService.GoBack(LoginActivity.this);
                        if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                            _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                            displayFailSnacky("Failed to navigate back to BootActivity! Please contact LucaHome support!");
                        }
                    }
                }, Snacky.LENGTH_LONG);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _logger = new Logger(TAG);
        _logger.Debug("onCreate");

        _receiverController = new ReceiverController(this);
        _navigationService = NavigationService.getInstance();
        _userService = UserService.getInstance();

        if (_userService.IsAnUserSaved()) {
            List<String> userList = new ArrayList<>();
            userList.add(_userService.GetUser().GetName());
            _userView.setAdapter(new ArrayAdapter<>(LoginActivity.this, android.R.layout.simple_dropdown_item_1line, userList));
        }

        // Set up the login form.
        _userView = (AutoCompleteTextView) findViewById(R.id.user);

        _passwordView = (EditText) findViewById(R.id.password);
        _passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        com.rey.material.widget.Button userSignInButton = (com.rey.material.widget.Button) findViewById(R.id.user_sign_in_button);
        userSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        _loginFormView = findViewById(R.id.login_form);
        _progressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onStart() {
        super.onStart();
        _logger.Debug("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        _logger.Debug("onResume");
        _receiverController.RegisterReceiver(_userCheckReceiver, new String[]{UserService.UserCheckedFinishedBroadcast});
    }

    @Override
    protected void onPause() {
        super.onPause();
        _logger.Debug("onPause");
        _receiverController.Dispose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        _logger.Debug(String.format("onKeyDown: keyCode: %s | event: %s", keyCode, event));

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            _navigationService.ClearCurrentActivity();
            _navigationService.ClearGoBackList();
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
        if (_userService.ValidatingUser()) {
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
        if (!TextUtils.isEmpty(password) && !_userService.IsEnteredPasswordValid(password)) {
            _passwordView.setError(createErrorText(getString(R.string.error_invalid_password)));
            focusView = _passwordView;
            cancel = true;
        }

        // Check for a valid user
        if (TextUtils.isEmpty(user)) {
            _userView.setError(createErrorText(getString(R.string.error_field_required)));
            focusView = _userView;
            cancel = true;
        } else if (!_userService.IsEnteredUserValid(user)) {
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
            _userService.ValidateUser(new LucaUser(user, password));
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

    private void displayFailSnacky(@NonNull String message) {
        Snacky.builder()
                .setActivty(LoginActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }
}

