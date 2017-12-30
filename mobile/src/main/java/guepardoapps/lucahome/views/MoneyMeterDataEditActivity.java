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
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.MoneyMeterData;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.MoneyMeterListService;
import guepardoapps.lucahome.common.service.UserService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

public class MoneyMeterDataEditActivity extends AppCompatActivity {
    private static final String TAG = MoneyMeterDataEditActivity.class.getSimpleName();

    private boolean _propertyChanged;
    private MoneyMeterData _moneyMeterData;

    private MoneyMeterListService _moneyMeterListService;
    private NavigationService _navigationService;

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _addFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(MoneyMeterListService.MoneyMeterDataListAddFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Added new money meter data!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to add money meter data!");
                _saveButton.setEnabled(true);
            }
        }
    };

    private BroadcastReceiver _updateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(MoneyMeterListService.MoneyMeterDataListUpdateFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Updated money meter data!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to update money meter data!");
                _saveButton.setEnabled(true);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moneymeterdata_edit);

        _moneyMeterData = (MoneyMeterData) getIntent().getSerializableExtra(MoneyMeterListService.MoneyMeterDataIntent);

        _moneyMeterListService = MoneyMeterListService.getInstance();
        _navigationService = NavigationService.getInstance();

        _receiverController = new ReceiverController(this);

        final AutoCompleteTextView bankTextView = findViewById(R.id.moneymeterdata_edit_bank_textview);
        final AutoCompleteTextView planTextView = findViewById(R.id.moneymeterdata_edit_plan_textview);
        final EditText amountEditText = findViewById(R.id.moneymeterdata_edit_amount_textview);
        final AutoCompleteTextView unitTextView = findViewById(R.id.moneymeterdata_edit_unit_textview);
        final DatePicker editDatePicker = findViewById(R.id.moneymeterdata_edit_datepicker);

        _saveButton = findViewById(R.id.save_moneymeterdata_edit_button);

        if (_moneyMeterData != null) {
            bankTextView.setText(_moneyMeterData.GetBank());
            planTextView.setText(_moneyMeterData.GetPlan());
            amountEditText.setText(String.valueOf(_moneyMeterData.GetAmount()));
            unitTextView.setText(_moneyMeterData.GetUnit());
            editDatePicker.updateDate(_moneyMeterData.GetSaveDate().Year(), _moneyMeterData.GetSaveDate().Month(), _moneyMeterData.GetSaveDate().DayOfMonth());
        } else {
            displayErrorSnackBar("Cannot work with data! Is corrupt! Please try again!");
        }

        bankTextView.setAdapter(new ArrayAdapter<>(MoneyMeterDataEditActivity.this, android.R.layout.simple_dropdown_item_1line, _moneyMeterListService.GetMoneyMeterBankList()));
        bankTextView.addTextChangedListener(new TextWatcher() {
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
        });

        planTextView.setAdapter(new ArrayAdapter<>(MoneyMeterDataEditActivity.this, android.R.layout.simple_dropdown_item_1line, _moneyMeterListService.GetMoneyMeterPlanList()));
        planTextView.addTextChangedListener(new TextWatcher() {
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
        });

        unitTextView.setAdapter(new ArrayAdapter<>(MoneyMeterDataEditActivity.this, android.R.layout.simple_dropdown_item_1line, _moneyMeterListService.GetMoneyMeterUnitList()));
        unitTextView.addTextChangedListener(new TextWatcher() {
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
        });

        editDatePicker.setOnClickListener(view -> {
            _propertyChanged = true;
            _saveButton.setEnabled(true);
        });

        _saveButton.setEnabled(false);
        _saveButton.setOnClickListener(view -> {
            bankTextView.setError(null);
            boolean cancel = false;
            View focusView = null;

            if (!_propertyChanged) {
                bankTextView.setError(createErrorText(getString(R.string.error_nothing_changed)));
                focusView = bankTextView;
                cancel = true;
            }

            String bank = bankTextView.getText().toString();
            if (TextUtils.isEmpty(bank)) {
                bankTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = bankTextView;
                cancel = true;
            }

            String plan = planTextView.getText().toString();
            if (TextUtils.isEmpty(plan)) {
                planTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = planTextView;
                cancel = true;
            }

            String unit = unitTextView.getText().toString();
            if (TextUtils.isEmpty(unit)) {
                unitTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = unitTextView;
                cancel = true;
            }

            String amountString = amountEditText.getText().toString();
            double amount = Double.parseDouble(amountString);

            int dayOfMonth = editDatePicker.getDayOfMonth();
            int month = editDatePicker.getMonth();
            int year = editDatePicker.getYear();

            SerializableDate saveDate = new SerializableDate(year, month, dayOfMonth);
            if (saveDate.isAfterNow()) {
                bankTextView.setError(createErrorText(getString(R.string.error_field_invalid_date)));
                focusView = editDatePicker;
                cancel = true;
            }

            if (cancel) {
                focusView.requestFocus();
            } else {
                if (_moneyMeterData.GetServerAction() == MoneyMeterData.ServerAction.Add) {
                    int newHighestId = _moneyMeterListService.GetHighestId() + 1;
                    int newHighestTypeId = _moneyMeterListService.GetHighestTypeId(bank, plan) + 1;
                    MoneyMeterData newMoneyMeterData = new MoneyMeterData(newHighestId, newHighestTypeId, bank, plan, amount, unit, saveDate, UserService.getInstance().GetUser().GetName());
                    newMoneyMeterData.SetServerAction(MoneyMeterData.ServerAction.Add);
                    newMoneyMeterData.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);
                    _moneyMeterListService.AddMoneyMeterData(newMoneyMeterData);
                    _saveButton.setEnabled(false);
                } else if (_moneyMeterData.GetServerAction() == MoneyMeterData.ServerAction.Update) {
                    MoneyMeterData newMoneyMeterData = new MoneyMeterData(_moneyMeterData.GetId(), _moneyMeterData.GetTypeId(), bank, plan, amount, unit, saveDate, UserService.getInstance().GetUser().GetName());
                    newMoneyMeterData.SetServerAction(MoneyMeterData.ServerAction.Add);
                    newMoneyMeterData.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);
                    _moneyMeterListService.UpdateMoneyMeterData(newMoneyMeterData);
                    _saveButton.setEnabled(false);
                } else {
                    bankTextView.setError(createErrorText(String.format(Locale.getDefault(), "Invalid action %s", _moneyMeterData.GetServerAction())));
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
        _receiverController.RegisterReceiver(_addFinishedReceiver, new String[]{MoneyMeterListService.MoneyMeterDataListAddFinishedBroadcast});
        _receiverController.RegisterReceiver(_updateFinishedReceiver, new String[]{MoneyMeterListService.MoneyMeterDataListUpdateFinishedBroadcast});
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
        _navigationService.GoBack(this);
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
                .setActivty(MoneyMeterDataEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void navigateBack(@NonNull String message) {
        Snacky.builder()
                .setActivty(MoneyMeterDataEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .success()
                .show();

        new Handler().postDelayed(() -> {
            NavigationService.NavigationResult navigationResult = _navigationService.GoBack(MoneyMeterDataEditActivity.this);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate back! Please contact LucaHome support!");
            }
        }, 1500);
    }
}
