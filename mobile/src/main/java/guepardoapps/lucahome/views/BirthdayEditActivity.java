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

import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaBirthday;
import guepardoapps.lucahome.common.dto.BirthdayDto;
import guepardoapps.lucahome.common.service.BirthdayService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

public class BirthdayEditActivity extends AppCompatActivity {
    private static final String TAG = BirthdayEditActivity.class.getSimpleName();
    private Logger _logger;

    private boolean _propertyChanged;
    private BirthdayDto _birthdayDto;

    private BirthdayService _birthdayService;
    private NavigationService _navigationService;

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _addFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_addFinishedReceiver onReceive");
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(BirthdayService.BirthdayAddFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Added new birthday!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to add birthday!");
                _saveButton.setEnabled(true);
            }
        }
    };

    private BroadcastReceiver _updateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_addFinishedReceiver onReceive");
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(BirthdayService.BirthdayUpdateFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Updated birthday!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to update birthday!");
                _saveButton.setEnabled(true);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birthday_edit);

        _logger = new Logger(TAG);
        _logger.Debug("onCreate");

        _birthdayDto = (BirthdayDto) getIntent().getSerializableExtra(BirthdayService.BirthdayIntent);

        _birthdayService = BirthdayService.getInstance();
        _navigationService = NavigationService.getInstance();

        _receiverController = new ReceiverController(this);

        final AutoCompleteTextView birthdayEditTextView = findViewById(R.id.birthday_edit_textview);
        final DatePicker birthdayEditDatePicker = findViewById(R.id.birthday_edit_datePicker);

        _saveButton = findViewById(R.id.save_birthday_edit_button);

        birthdayEditTextView.setAdapter(new ArrayAdapter<>(BirthdayEditActivity.this, android.R.layout.simple_dropdown_item_1line, _birthdayService.GetBirthdayNameList()));
        birthdayEditTextView.addTextChangedListener(new TextWatcher() {
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

        birthdayEditDatePicker.setOnClickListener(view -> {
            _propertyChanged = true;
            _saveButton.setEnabled(true);
        });

        if (_birthdayDto != null) {
            birthdayEditTextView.setText(_birthdayDto.GetName());
            birthdayEditDatePicker.updateDate(_birthdayDto.GetDate().Year(), _birthdayDto.GetDate().Month(), _birthdayDto.GetDate().DayOfMonth());
        } else {
            displayErrorSnackBar("Cannot work with data! Is corrupt! Please try again!");
        }

        _saveButton.setEnabled(false);
        _saveButton.setOnClickListener(view -> {
            birthdayEditTextView.setError(null);
            boolean cancel = false;
            View focusView = null;

            if (!_propertyChanged) {
                birthdayEditTextView.setError(createErrorText(getString(R.string.error_nothing_changed)));
                focusView = birthdayEditTextView;
                cancel = true;
            }

            String birthdayName = birthdayEditTextView.getText().toString();

            if (TextUtils.isEmpty(birthdayName)) {
                birthdayEditTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = birthdayEditTextView;
                cancel = true;
            }

            int dayOfMonth = birthdayEditDatePicker.getDayOfMonth();
            int month = birthdayEditDatePicker.getMonth();
            int year = birthdayEditDatePicker.getYear();

            SerializableDate birthdayDate = new SerializableDate(year, month, dayOfMonth);
            if (birthdayDate.isAfterNow()) {
                birthdayEditTextView.setError(createErrorText(getString(R.string.error_field_invalid_date)));
                focusView = birthdayEditDatePicker;
                cancel = true;
            }

            if (cancel) {
                focusView.requestFocus();
            } else {
                if (_birthdayDto.GetAction() == BirthdayDto.Action.Add) {
                    int lastHighestId = 0;

                    int dataListSize = _birthdayService.GetDataList().getSize();
                    if (dataListSize > 0) {
                        lastHighestId = _birthdayService.GetDataList().getValue(dataListSize - 1).GetId() + 1;
                    }

                    _birthdayService.AddBirthday(new LucaBirthday(lastHighestId, birthdayName, birthdayDate, null));
                    _saveButton.setEnabled(false);
                } else if (_birthdayDto.GetAction() == BirthdayDto.Action.Update) {
                    _birthdayService.UpdateBirthday(new LucaBirthday(_birthdayDto.GetId(), birthdayName, birthdayDate, null));
                    _saveButton.setEnabled(false);
                } else {
                    birthdayEditTextView.setError(createErrorText(String.format(Locale.getDefault(), "Invalid action %s", _birthdayDto.GetAction())));
                }
            }
        });
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
        _receiverController.RegisterReceiver(_addFinishedReceiver, new String[]{BirthdayService.BirthdayAddFinishedBroadcast});
        _receiverController.RegisterReceiver(_updateFinishedReceiver, new String[]{BirthdayService.BirthdayUpdateFinishedBroadcast});
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
                .setActivty(BirthdayEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void navigateBack(@NonNull String message) {
        Snacky.builder()
                .setActivty(BirthdayEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .success()
                .show();

        new Handler().postDelayed(() -> {
            NavigationService.NavigationResult navigationResult = _navigationService.GoBack(BirthdayEditActivity.this);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate back! Please contact LucaHome support!");
            }
        }, 1500);
    }
}
