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
import guepardoapps.lucahome.common.classes.LucaTimer;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.enums.SocketAction;
import guepardoapps.lucahome.common.enums.Weekday;
import guepardoapps.lucahome.common.service.ScheduleService;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

public class TimerEditActivity extends AppCompatActivity {
    private static final String TAG = TimerEditActivity.class.getSimpleName();
    private Logger _logger;

    private boolean _propertyChanged;

    private NavigationService _navigationService;
    private ScheduleService _scheduleService;
    private WirelessSocketService _wirelessSocketService;

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _addFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_addFinishedReceiver onReceive");
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(ScheduleService.TimerAddFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Added new timer!");
                    _wirelessSocketService.LoadData();
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to add timer!");
                _saveButton.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_edit);

        _logger = new Logger(TAG);
        _logger.Debug("onCreate");

        _navigationService = NavigationService.getInstance();
        _scheduleService = ScheduleService.getInstance();
        _wirelessSocketService = WirelessSocketService.getInstance();

        _receiverController = new ReceiverController(this);

        final AutoCompleteTextView timerNameEditTextView = findViewById(R.id.timer_edit_name_textview);
        final Spinner timerSocketSelect = findViewById(R.id.timer_socket_select);
        final Spinner timerCountdownSelect = findViewById(R.id.timer_countdown_select);

        _saveButton = findViewById(R.id.save_timer_edit_button);

        timerNameEditTextView.setAdapter(new ArrayAdapter<>(TimerEditActivity.this, android.R.layout.simple_dropdown_item_1line, _scheduleService.GetTimerNameList()));
        timerNameEditTextView.addTextChangedListener(new TextWatcher() {
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

        ArrayAdapter<String> socketDataAdapter = new ArrayAdapter<>(TimerEditActivity.this, android.R.layout.simple_spinner_item, _wirelessSocketService.GetNameList());
        socketDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timerSocketSelect.setAdapter(socketDataAdapter);

        List<String> countdownList = new ArrayList<>();
        countdownList.add("00:15");
        countdownList.add("00:30");
        countdownList.add("01:00");
        countdownList.add("03:00");
        countdownList.add("06:00");
        countdownList.add("12:00");
        ArrayAdapter<String> countdownDataAdapter = new ArrayAdapter<>(TimerEditActivity.this, android.R.layout.simple_spinner_item, countdownList);
        countdownDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timerCountdownSelect.setAdapter(countdownDataAdapter);

        _saveButton.setEnabled(false);
        _saveButton.setOnClickListener(view -> {
            timerNameEditTextView.setError(null);
            boolean cancel = false;
            View focusView = null;

            if (!_propertyChanged) {
                timerNameEditTextView.setError(createErrorText(getString(R.string.error_nothing_changed)));
                focusView = timerNameEditTextView;
                cancel = true;
            }

            String timerName = timerNameEditTextView.getText().toString();

            if (TextUtils.isEmpty(timerName)) {
                timerNameEditTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = timerNameEditTextView;
                cancel = true;
            }

            int socketId = timerSocketSelect.getSelectedItemPosition();
            WirelessSocket wirelessSocket = _wirelessSocketService.GetDataList().getValue(socketId);

            int countdownId = timerCountdownSelect.getSelectedItemPosition();

            Calendar calendarNow = Calendar.getInstance();
            int currentWeekday = calendarNow.get(Calendar.DAY_OF_WEEK);
            int hour = calendarNow.get(Calendar.HOUR_OF_DAY);
            int minute = calendarNow.get(Calendar.MINUTE);

            switch (countdownId) {
                case 0:
                    minute += 15;
                    break;
                case 1:
                    minute += 30;
                    break;
                case 2:
                    hour += 1;
                    break;
                case 3:
                    hour += 3;
                    break;
                case 4:
                    hour += 6;
                    break;
                case 5:
                    hour += 12;
                    break;
                default:
                    minute += 30;
                    break;
            }

            while (minute > 60) {
                minute -= 60;
                hour++;
            }
            while (hour > 24) {
                hour -= 24;
                currentWeekday++;
            }

            while (currentWeekday > 7) {
                currentWeekday -= 7;
            }
            Weekday weekday = Weekday.GetById(currentWeekday);

            if (cancel) {
                focusView.requestFocus();
            } else {
                int lastHighestId = 0;

                int dataListSize = _scheduleService.GetTimerList().getSize();
                if (dataListSize > 0) {
                    lastHighestId = _scheduleService.GetTimerList().getValue(dataListSize - 1).GetId() + 1;
                }

                _scheduleService.AddTimer(new LucaTimer(lastHighestId, timerName, wirelessSocket, weekday, new SerializableTime(hour, minute, 0, 0), SocketAction.Activate, true));
                _saveButton.setEnabled(false);
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
        _receiverController.RegisterReceiver(_addFinishedReceiver, new String[]{ScheduleService.TimerAddFinishedBroadcast});
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
                .setActivty(TimerEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void navigateBack(@NonNull String message) {
        Snacky.builder()
                .setActivty(TimerEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .success()
                .show();

        new Handler().postDelayed(() -> {
            NavigationService.NavigationResult navigationResult = _navigationService.GoBack(TimerEditActivity.this);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate back! Please contact LucaHome support!");
            }
        }, 1500);
    }
}
