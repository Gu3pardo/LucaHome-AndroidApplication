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
import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.dto.WirelessSwitchDto;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.WirelessSwitchService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

public class WirelessSwitchEditActivity extends AppCompatActivity {
    private static final String TAG = WirelessSwitchEditActivity.class.getSimpleName();

    private boolean _propertyChanged;
    private WirelessSwitchDto _wirelessSwitchDto;

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _addFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(WirelessSwitchService.WirelessSwitchAddFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Added Switch!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to add Switch!");
                _saveButton.setEnabled(true);
            }
        }
    };

    private BroadcastReceiver _updateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(WirelessSwitchService.WirelessSwitchUpdateFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Updated Switch!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to update Switch!");
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
        setContentView(R.layout.activity_wireless_switch_edit);

        _wirelessSwitchDto = (WirelessSwitchDto) getIntent().getSerializableExtra(WirelessSwitchService.WirelessSwitchIntent);

        _receiverController = new ReceiverController(this);

        final AutoCompleteTextView switchNameTypeTextView = findViewById(R.id.switch_edit_name_textview);
        final AutoCompleteTextView switchAreaTypeTextView = findViewById(R.id.switch_edit_area_textview);
        final AutoCompleteTextView switchRemoteIdTypeTextView = findViewById(R.id.switch_edit_remoteid_textview);
        final AutoCompleteTextView switchKeyCodeTypeTextView = findViewById(R.id.switch_edit_keycode_textview);

        _saveButton = findViewById(R.id.save_switch_edit_button);

        switchNameTypeTextView.setAdapter(new ArrayAdapter<>(WirelessSwitchEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSwitchService.getInstance().GetNameList()));
        switchNameTypeTextView.addTextChangedListener(_textWatcher);

        switchAreaTypeTextView.setAdapter(new ArrayAdapter<>(WirelessSwitchEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSwitchService.getInstance().GetAreaList()));
        switchAreaTypeTextView.addTextChangedListener(_textWatcher);

        switchRemoteIdTypeTextView.setAdapter(new ArrayAdapter<>(WirelessSwitchEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSwitchService.getInstance().GetRemoteIdList()));
        switchRemoteIdTypeTextView.addTextChangedListener(_textWatcher);

        switchKeyCodeTypeTextView.setAdapter(new ArrayAdapter<>(WirelessSwitchEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSwitchService.getInstance().GetKeyCodeList()));
        switchKeyCodeTypeTextView.addTextChangedListener(_textWatcher);

        if (_wirelessSwitchDto != null) {
            switchNameTypeTextView.setText(_wirelessSwitchDto.GetName());
            switchAreaTypeTextView.setText(_wirelessSwitchDto.GetArea());
            switchRemoteIdTypeTextView.setText(_wirelessSwitchDto.GetRemoteId());
            switchKeyCodeTypeTextView.setText(_wirelessSwitchDto.GetKeyCode());
        } else {
            displayErrorSnackBar("Cannot work with data! Is corrupt! Please try again!");
        }

        _saveButton.setEnabled(false);
        _saveButton.setOnClickListener(view -> {
            switchNameTypeTextView.setError(null);
            switchAreaTypeTextView.setError(null);
            switchRemoteIdTypeTextView.setError(null);
            switchKeyCodeTypeTextView.setError(null);
            boolean cancel = false;
            View focusView = null;

            if (!_propertyChanged) {
                switchNameTypeTextView.setError(createErrorText(getString(R.string.error_nothing_changed)));
                focusView = switchNameTypeTextView;
                cancel = true;
            }

            String name = switchNameTypeTextView.getText().toString();

            if (TextUtils.isEmpty(name)) {
                switchNameTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = switchNameTypeTextView;
                cancel = true;
            }

            String area = switchAreaTypeTextView.getText().toString();

            if (TextUtils.isEmpty(area)) {
                switchAreaTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = switchAreaTypeTextView;
                cancel = true;
            }

            String remoteIdString = switchRemoteIdTypeTextView.getText().toString();

            if (TextUtils.isEmpty(remoteIdString)) {
                switchRemoteIdTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = switchRemoteIdTypeTextView;
                cancel = true;
            }

            String keyCodeString = switchKeyCodeTypeTextView.getText().toString();

            if (TextUtils.isEmpty(keyCodeString)) {
                switchKeyCodeTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = switchKeyCodeTypeTextView;
                cancel = true;
            }

            if (cancel) {
                focusView.requestFocus();
            } else {
                if (_wirelessSwitchDto.GetAction() == WirelessSwitchDto.Action.Add) {
                    int lastHighestId = 0;

                    int dataListSize = WirelessSwitchService.getInstance().GetDataList().getSize();
                    if (dataListSize > 0) {
                        lastHighestId = WirelessSwitchService.getInstance().GetDataList().getValue(dataListSize - 1).GetId() + 1;
                    }

                    WirelessSwitchService.getInstance().AddWirelessSwitch(new WirelessSwitch(lastHighestId, name, area, Integer.parseInt(remoteIdString), keyCodeString.charAt(0), true, false, new SerializableDate(), new SerializableTime(), "", false, ILucaClass.LucaServerDbAction.Add));
                    _saveButton.setEnabled(false);
                } else if (_wirelessSwitchDto.GetAction() == WirelessSwitchDto.Action.Update) {
                    WirelessSwitchService.getInstance().UpdateWirelessSwitch(new WirelessSwitch(_wirelessSwitchDto.GetId(), name, area, Integer.parseInt(remoteIdString), keyCodeString.charAt(0), true, false, new SerializableDate(), new SerializableTime(), "", false, ILucaClass.LucaServerDbAction.Update));
                    _saveButton.setEnabled(false);
                } else {
                    switchNameTypeTextView.setError(createErrorText(String.format(Locale.getDefault(), "Invalid action %s", _wirelessSwitchDto.GetAction())));
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
        _receiverController.RegisterReceiver(_addFinishedReceiver, new String[]{WirelessSwitchService.WirelessSwitchAddFinishedBroadcast});
        _receiverController.RegisterReceiver(_updateFinishedReceiver, new String[]{WirelessSwitchService.WirelessSwitchUpdateFinishedBroadcast});
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
                .setActivty(WirelessSwitchEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void navigateBack(@NonNull String message) {
        Snacky.builder()
                .setActivty(WirelessSwitchEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .success()
                .show();

        new Handler().postDelayed(() -> {
            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().GoBack(WirelessSwitchEditActivity.this);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate back! Please contact LucaHome support!");
            }
        }, 1500);
    }
}
