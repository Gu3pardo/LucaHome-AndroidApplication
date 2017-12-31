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
import android.widget.TimePicker;

import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.MeterData;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.MeterListService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

public class MeterDataEditActivity extends AppCompatActivity {
    private static final String TAG = MeterDataEditActivity.class.getSimpleName();

    private boolean _propertyChanged;
    private MeterData _meterData;

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _addFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(MeterListService.MeterDataListAddFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Added new meter data!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to add meter data!");
                _saveButton.setEnabled(true);
            }
        }
    };

    private BroadcastReceiver _updateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(MeterListService.MeterDataListUpdateFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Updated meter data!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to update meter data!");
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
        setContentView(R.layout.activity_meterdata_edit);

        _meterData = (MeterData) getIntent().getSerializableExtra(MeterListService.MeterDataIntent);

        _receiverController = new ReceiverController(this);

        final AutoCompleteTextView editTypeTextView = findViewById(R.id.meterdata_edit_type_textview);
        final AutoCompleteTextView meterIdTextView = findViewById(R.id.meterdata_edit_meterid_textview);
        final AutoCompleteTextView areaTextView = findViewById(R.id.meterdata_edit_area_textview);
        final AutoCompleteTextView imageNameTextView = findViewById(R.id.meterdata_edit_imagename_textview);
        final EditText valueEditText = findViewById(R.id.meterdata_edit_value_textview);
        final DatePicker editDatePicker = findViewById(R.id.meterdata_edit_datepicker);
        final TimePicker editTimePicker = findViewById(R.id.meterdata_edit_timepicker);

        _saveButton = findViewById(R.id.save_meterdata_edit_button);

        if (_meterData != null) {
            editTypeTextView.setText(_meterData.GetType());
            meterIdTextView.setText(_meterData.GetMeterId());
            areaTextView.setText(_meterData.GetArea());
            imageNameTextView.setText(_meterData.GetImageName());
            valueEditText.setText(String.valueOf(_meterData.GetValue()));
            editDatePicker.updateDate(_meterData.GetSaveDate().Year(), _meterData.GetSaveDate().Month(), _meterData.GetSaveDate().DayOfMonth());
            editTimePicker.setIs24HourView(true);
            editTimePicker.setCurrentHour(_meterData.GetSaveTime().Hour());
            editTimePicker.setCurrentMinute(_meterData.GetSaveTime().Minute());
        } else {
            displayErrorSnackBar("Cannot work with data! Is corrupt! Please try again!");
        }

        editTypeTextView.setAdapter(new ArrayAdapter<>(MeterDataEditActivity.this, android.R.layout.simple_dropdown_item_1line, MeterListService.getInstance().GetMeterTypeList()));
        editTypeTextView.addTextChangedListener(_textWatcher);

        meterIdTextView.setAdapter(new ArrayAdapter<>(MeterDataEditActivity.this, android.R.layout.simple_dropdown_item_1line, MeterListService.getInstance().GetMeterIdList()));
        meterIdTextView.addTextChangedListener(_textWatcher);

        areaTextView.setAdapter(new ArrayAdapter<>(MeterDataEditActivity.this, android.R.layout.simple_dropdown_item_1line, MeterListService.getInstance().GetAreaList()));
        areaTextView.addTextChangedListener(_textWatcher);

        imageNameTextView.setAdapter(new ArrayAdapter<>(MeterDataEditActivity.this, android.R.layout.simple_dropdown_item_1line, MeterListService.getInstance().GetImageNameList()));
        imageNameTextView.addTextChangedListener(_textWatcher);

        editDatePicker.setOnClickListener(view -> {
            _propertyChanged = true;
            _saveButton.setEnabled(true);
        });

        editTimePicker.setOnClickListener(view -> {
            _propertyChanged = true;
            _saveButton.setEnabled(true);
        });

        _saveButton.setEnabled(false);
        _saveButton.setOnClickListener(view -> {
            editTypeTextView.setError(null);
            boolean cancel = false;
            View focusView = null;

            if (!_propertyChanged) {
                editTypeTextView.setError(createErrorText(getString(R.string.error_nothing_changed)));
                focusView = editTypeTextView;
                cancel = true;
            }

            String type = editTypeTextView.getText().toString();
            if (TextUtils.isEmpty(type)) {
                editTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = editTypeTextView;
                cancel = true;
            }

            String meterId = meterIdTextView.getText().toString();
            if (TextUtils.isEmpty(meterId)) {
                meterIdTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = meterIdTextView;
                cancel = true;
            }

            String area = areaTextView.getText().toString();
            if (TextUtils.isEmpty(area)) {
                areaTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = areaTextView;
                cancel = true;
            }

            String imageName = imageNameTextView.getText().toString();
            if (TextUtils.isEmpty(imageName)) {
                imageNameTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = imageNameTextView;
                cancel = true;
            }

            String valueString = valueEditText.getText().toString();
            double value = Double.parseDouble(valueString);

            int dayOfMonth = editDatePicker.getDayOfMonth();
            int month = editDatePicker.getMonth();
            int year = editDatePicker.getYear();

            SerializableDate saveDate = new SerializableDate(year, month, dayOfMonth);
            if (saveDate.isAfterNow()) {
                editTypeTextView.setError(createErrorText(getString(R.string.error_field_invalid_date)));
                focusView = editDatePicker;
                cancel = true;
            }

            int hour = editTimePicker.getCurrentHour();
            int minute = editTimePicker.getCurrentMinute();

            SerializableTime saveTime = new SerializableTime(hour, minute, 0, 0);

            if (cancel) {
                focusView.requestFocus();
            } else {
                if (_meterData.GetServerAction() == MeterData.ServerAction.Add) {
                    int newHighestId = MeterListService.getInstance().GetHighestId() + 1;
                    int newHighestTypeId = MeterListService.getInstance().GetHighestTypeId(type) + 1;
                    MeterData newMeterData = new MeterData(newHighestId, type, newHighestTypeId, saveDate, saveTime, meterId, area, value, imageName);
                    newMeterData.SetServerAction(MeterData.ServerAction.Add);
                    newMeterData.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);
                    MeterListService.getInstance().AddMeterData(newMeterData);
                    _saveButton.setEnabled(false);
                } else if (_meterData.GetServerAction() == MeterData.ServerAction.Update) {
                    MeterData newMeterData = new MeterData(_meterData.GetId(), type, _meterData.GetTypeId(), saveDate, saveTime, meterId, area, value, imageName);
                    newMeterData.SetServerAction(MeterData.ServerAction.Update);
                    newMeterData.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);
                    MeterListService.getInstance().UpdateMeterData(newMeterData);
                    _saveButton.setEnabled(false);
                } else {
                    editTypeTextView.setError(createErrorText(String.format(Locale.getDefault(), "Invalid action %s", _meterData.GetServerAction())));
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
        _receiverController.RegisterReceiver(_addFinishedReceiver, new String[]{MeterListService.MeterDataListAddFinishedBroadcast});
        _receiverController.RegisterReceiver(_updateFinishedReceiver, new String[]{MeterListService.MeterDataListUpdateFinishedBroadcast});
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
                .setActivty(MeterDataEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void navigateBack(@NonNull String message) {
        Snacky.builder()
                .setActivty(MeterDataEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .success()
                .show();

        new Handler().postDelayed(() -> {
            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().GoBack(MeterDataEditActivity.this);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate back! Please contact LucaHome support!");
            }
        }, 1500);
    }
}
