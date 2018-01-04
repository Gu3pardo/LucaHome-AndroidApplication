package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.Locale;

import de.mateware.snacky.Snacky;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.PuckJs;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.PuckJsListService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

public class PuckJsEditActivity extends AppCompatActivity {
    private static final String TAG = PuckJsEditActivity.class.getSimpleName();

    private boolean _propertyChanged;
    private PuckJs _puckJs;

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _addFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(PuckJsListService.PuckJsListAddFinishedBroadcast);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Added puckjs!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to add puckjs!");
                _saveButton.setEnabled(true);
            }
        }
    };

    private BroadcastReceiver _updateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(PuckJsListService.PuckJsListUpdateFinishedBroadcast);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Updated puckjs!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to update puckjs!");
                _saveButton.setEnabled(true);
            }
        }
    };

    private TextWatcher _textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            _propertyChanged = true;
            _saveButton.setEnabled(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puckjs_edit);

        _puckJs = (PuckJs) getIntent().getSerializableExtra(PuckJsListService.PuckJsIntent);

        _receiverController = new ReceiverController(this);

        final AutoCompleteTextView puckJsNameTypeTextView = findViewById(R.id.puckjs_edit_name_textview);
        final AutoCompleteTextView puckJsAreaTypeTextView = findViewById(R.id.puckjs_edit_area_textview);
        final AutoCompleteTextView puckJsMacTypeTextView = findViewById(R.id.puckjs_edit_mac_textview);

        _saveButton = findViewById(R.id.save_puckjs_edit_button);

        puckJsNameTypeTextView.setAdapter(new ArrayAdapter<>(PuckJsEditActivity.this, android.R.layout.simple_dropdown_item_1line, PuckJsListService.getInstance().GetPuckJsNameList()));
        puckJsNameTypeTextView.addTextChangedListener(_textWatcher);

        puckJsAreaTypeTextView.setAdapter(new ArrayAdapter<>(PuckJsEditActivity.this, android.R.layout.simple_dropdown_item_1line, PuckJsListService.getInstance().GetPuckJsAreaList()));
        puckJsAreaTypeTextView.addTextChangedListener(_textWatcher);

        puckJsMacTypeTextView.setAdapter(new ArrayAdapter<>(PuckJsEditActivity.this, android.R.layout.simple_dropdown_item_1line, PuckJsListService.getInstance().GetPuckJsMacList()));
        puckJsMacTypeTextView.addTextChangedListener(_textWatcher);

        if (_puckJs != null) {
            puckJsNameTypeTextView.setText(_puckJs.GetName());
            puckJsAreaTypeTextView.setText(_puckJs.GetArea());
            puckJsMacTypeTextView.setText(_puckJs.GetMac());
        } else {
            displayErrorSnackBar("Cannot work with data! Is corrupt! Please try again!");
        }

        _saveButton.setEnabled(false);
        _saveButton.setOnClickListener(view -> {
            puckJsNameTypeTextView.setError(null);
            puckJsAreaTypeTextView.setError(null);
            puckJsMacTypeTextView.setError(null);
            boolean cancel = false;
            View focusView = null;

            if (!_propertyChanged) {
                puckJsNameTypeTextView.setError(createErrorText(getString(R.string.error_nothing_changed)));
                focusView = puckJsNameTypeTextView;
                cancel = true;
            }

            String name = puckJsNameTypeTextView.getText().toString();

            if (TextUtils.isEmpty(name)) {
                puckJsNameTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = puckJsNameTypeTextView;
                cancel = true;
            }

            String area = puckJsAreaTypeTextView.getText().toString();

            if (TextUtils.isEmpty(area)) {
                puckJsAreaTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = puckJsAreaTypeTextView;
                cancel = true;
            }

            String mac = puckJsMacTypeTextView.getText().toString();

            if (TextUtils.isEmpty(mac)) {
                puckJsMacTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = puckJsMacTypeTextView;
                cancel = true;
            }

            if (cancel) {
                focusView.requestFocus();
            } else {
                if (_puckJs.GetServerDbAction() == PuckJs.LucaServerDbAction.Add) {
                    int highestId = PuckJsListService.getInstance().GetHighestId();
                    PuckJsListService.getInstance().AddPuckJs(new PuckJs(highestId + 1, name, area, mac, false, ILucaClass.LucaServerDbAction.Add));
                    _saveButton.setEnabled(false);
                } else if (_puckJs.GetServerDbAction() == PuckJs.LucaServerDbAction.Update) {
                    PuckJsListService.getInstance().UpdatePuckJs(new PuckJs(_puckJs.GetId(), name, area, mac, false, ILucaClass.LucaServerDbAction.Update));
                    _saveButton.setEnabled(false);
                } else {
                    puckJsNameTypeTextView.setError(createErrorText(String.format(Locale.getDefault(), "Invalid action %s", _puckJs.GetServerDbAction())));
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_addFinishedReceiver, new String[]{PuckJsListService.PuckJsListAddFinishedBroadcast});
        _receiverController.RegisterReceiver(_updateFinishedReceiver, new String[]{PuckJsListService.PuckJsListUpdateFinishedBroadcast});
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
    public void onBackPressed() {
        NavigationService.getInstance().GoBack(this);
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
                .setActivty(PuckJsEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void navigateBack(@NonNull String message) {
        Snacky.builder()
                .setActivty(PuckJsEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .success()
                .show();

        new Handler().postDelayed(() -> {
            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().GoBack(PuckJsEditActivity.this);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate back! Please contact LucaHome support!");
            }
        }, 1500);
    }
}
