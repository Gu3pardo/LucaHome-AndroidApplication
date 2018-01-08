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
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.Schedule;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.dto.ScheduleDto;
import guepardoapps.lucahome.common.enums.SocketAction;
import guepardoapps.lucahome.common.enums.Weekday;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.ScheduleService;
import guepardoapps.lucahome.common.service.TimerService;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.lucahome.common.service.WirelessSwitchService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

@SuppressWarnings("deprecation")
public class ScheduleEditActivity extends AppCompatActivity {
    private static final String TAG = ScheduleEditActivity.class.getSimpleName();

    private boolean _propertyChanged;
    private ScheduleDto _scheduleDto;

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _addFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(ScheduleService.ScheduleAddFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Added new entry!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to add entry!");
                _saveButton.setEnabled(true);
            }
        }
    };

    private BroadcastReceiver _updateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(ScheduleService.ScheduleUpdateFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Updated entry!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to update entry!");
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
        setContentView(R.layout.activity_schedule_edit);

        _scheduleDto = (ScheduleDto) getIntent().getSerializableExtra(ScheduleService.ScheduleIntent);

        _receiverController = new ReceiverController(this);

        final AutoCompleteTextView scheduleNameEditTextView = findViewById(R.id.schedule_edit_name_textview);
        final Spinner scheduleSocketSelect = findViewById(R.id.schedule_socket_select);
        final Spinner scheduleSwitchSelect = findViewById(R.id.schedule_switch_select);
        final Spinner scheduleWeekdaySelect = findViewById(R.id.schedule_weekday_select);
        final TimePicker scheduleTimePicker = findViewById(R.id.schedule_timePicker);
        final Spinner scheduleActionSelect = findViewById(R.id.schedule_action_select);

        _saveButton = findViewById(R.id.save_schedule_edit_button);

        scheduleNameEditTextView.setAdapter(new ArrayAdapter<>(ScheduleEditActivity.this, android.R.layout.simple_dropdown_item_1line, ScheduleService.getInstance().GetScheduleNameList()));
        scheduleNameEditTextView.addTextChangedListener(_textWatcher);

        ArrayAdapter<String> socketDataAdapter = new ArrayAdapter<>(ScheduleEditActivity.this, android.R.layout.simple_spinner_item, WirelessSocketService.getInstance().GetNameList());
        socketDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSocketSelect.setAdapter(socketDataAdapter);

        ArrayAdapter<String> switchDataAdapter = new ArrayAdapter<>(ScheduleEditActivity.this, android.R.layout.simple_spinner_item, WirelessSwitchService.getInstance().GetNameList());
        switchDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSwitchSelect.setAdapter(switchDataAdapter);

        List<String> weekdays = new ArrayList<>();
        for (Weekday weekday : Weekday.values()) {
            if (weekday.GetInt() > -1) {
                weekdays.add(weekday.GetEnglishDay());
            }
        }
        ArrayAdapter<String> weekdayDataAdapter = new ArrayAdapter<>(ScheduleEditActivity.this, android.R.layout.simple_spinner_item, weekdays);
        weekdayDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleWeekdaySelect.setAdapter(weekdayDataAdapter);

        final Calendar calendarNow = Calendar.getInstance();
        int hour = calendarNow.get(Calendar.HOUR_OF_DAY);
        int minute = calendarNow.get(Calendar.MINUTE);
        scheduleTimePicker.setIs24HourView(true);
        scheduleTimePicker.setCurrentHour(hour);
        scheduleTimePicker.setCurrentMinute(minute);

        List<String> socketActions = new ArrayList<>();
        for (SocketAction socketAction : SocketAction.values()) {
            if (socketAction.GetId() > -1) {
                socketActions.add(socketAction.GetAction());
            }
        }
        ArrayAdapter<String> socketActionDataAdapter = new ArrayAdapter<>(ScheduleEditActivity.this, android.R.layout.simple_spinner_item, socketActions);
        socketActionDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleActionSelect.setAdapter(socketActionDataAdapter);

        if (_scheduleDto != null) {
            scheduleNameEditTextView.setText(_scheduleDto.GetName());
            if (_scheduleDto.GetWirelessSocket() != null) {
                for (int socketIndex = 0; socketIndex < WirelessSocketService.getInstance().GetNameList().size(); socketIndex++) {
                    if (WirelessSocketService.getInstance().GetNameList().get(socketIndex).contentEquals(_scheduleDto.GetWirelessSocket().GetName())) {
                        scheduleSocketSelect.setSelection(socketIndex);
                        break;
                    }
                }
            }
            if (_scheduleDto.GetWirelessSwitch() != null) {
                for (int switchIndex = 0; switchIndex < WirelessSwitchService.getInstance().GetNameList().size(); switchIndex++) {
                    if (WirelessSwitchService.getInstance().GetNameList().get(switchIndex).contentEquals(_scheduleDto.GetWirelessSwitch().GetName())) {
                        scheduleSwitchSelect.setSelection(switchIndex);
                        break;
                    }
                }
            }
            if (_scheduleDto.GetWeekday() != null) {
                scheduleWeekdaySelect.setSelection(_scheduleDto.GetWeekday().GetInt());
            }
            scheduleTimePicker.setCurrentHour(_scheduleDto.GetTime().Hour());
            scheduleTimePicker.setCurrentMinute(_scheduleDto.GetTime().Minute());
            scheduleActionSelect.setSelection(_scheduleDto.GetSocketAction().GetId());
        } else {
            displayErrorSnackBar("Cannot work with data! Is corrupt! Please try again!");
        }

        _saveButton.setEnabled(false);
        _saveButton.setOnClickListener(view -> {
            scheduleNameEditTextView.setError(null);
            boolean cancel = false;
            View focusView = null;

            if (!_propertyChanged) {
                scheduleNameEditTextView.setError(createErrorText(getString(R.string.error_nothing_changed)));
                focusView = scheduleNameEditTextView;
                cancel = true;
            }

            String scheduleName = scheduleNameEditTextView.getText().toString();

            if (TextUtils.isEmpty(scheduleName)) {
                scheduleNameEditTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = scheduleNameEditTextView;
                cancel = true;
            }

            int socketId = scheduleSocketSelect.getSelectedItemPosition();
            WirelessSocket wirelessSocket = WirelessSocketService.getInstance().GetDataList().getValue(socketId);

            int switchId = scheduleSwitchSelect.getSelectedItemPosition();
            WirelessSwitch wirelessSwitch = WirelessSwitchService.getInstance().GetDataList().getValue(switchId);

            int weekdayId = scheduleWeekdaySelect.getSelectedItemPosition();
            Weekday weekday = Weekday.GetById(weekdayId);

            SerializableTime time = new SerializableTime(scheduleTimePicker.getCurrentHour(), scheduleTimePicker.getCurrentMinute(), 0, 0);

            int socketActionId = scheduleActionSelect.getSelectedItemPosition();
            SocketAction socketAction = SocketAction.GetById(socketActionId);

            if (cancel) {
                focusView.requestFocus();
            } else {
                if (_scheduleDto.GetAction() == ScheduleDto.Action.Add) {
                    int highestId;

                    int highestScheduleId = ScheduleService.getInstance().GetHighestId();
                    int highestTimerId = TimerService.getInstance().GetHighestId();
                    if (highestScheduleId > highestTimerId) {
                        highestId = highestScheduleId;
                    } else {
                        highestId = highestTimerId;
                    }

                    ScheduleService.getInstance().AddSchedule(new Schedule(highestId + 1, scheduleName, wirelessSocket, wirelessSwitch, weekday, time, socketAction, true, false, ILucaClass.LucaServerDbAction.Add));
                    _saveButton.setEnabled(false);
                } else if (_scheduleDto.GetAction() == ScheduleDto.Action.Update) {
                    ScheduleService.getInstance().UpdateSchedule(new Schedule(_scheduleDto.GetId(), scheduleName, wirelessSocket, wirelessSwitch, weekday, time, socketAction, true, false, ILucaClass.LucaServerDbAction.Update));
                    _saveButton.setEnabled(false);
                } else {
                    scheduleNameEditTextView.setError(createErrorText(String.format(Locale.getDefault(), "Invalid action %s", _scheduleDto.GetAction())));
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
        _receiverController.RegisterReceiver(_addFinishedReceiver, new String[]{ScheduleService.ScheduleAddFinishedBroadcast});
        _receiverController.RegisterReceiver(_updateFinishedReceiver, new String[]{ScheduleService.ScheduleUpdateFinishedBroadcast});
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
                .setActivty(ScheduleEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void navigateBack(@NonNull String message) {
        Snacky.builder()
                .setActivty(ScheduleEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .success()
                .show();

        new Handler().postDelayed(() -> {
            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().GoBack(ScheduleEditActivity.this);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate back! Please contact LucaHome support!");
            }
        }, 1500);
    }
}
