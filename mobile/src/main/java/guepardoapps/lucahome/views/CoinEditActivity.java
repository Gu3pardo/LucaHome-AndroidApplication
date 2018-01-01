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
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.Coin;
import guepardoapps.lucahome.common.dto.CoinDto;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.CoinService;
import guepardoapps.lucahome.common.service.UserService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

public class CoinEditActivity extends AppCompatActivity {
    private static final String TAG = CoinEditActivity.class.getSimpleName();

    private boolean _propertyChanged;
    private CoinDto _coinDto;

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _addFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(CoinService.CoinAddFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Added new coin!");
                } else {
                    displayFailSnacky(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayFailSnacky("Failed to add coin!");
                _saveButton.setEnabled(true);
            }
        }
    };

    private BroadcastReceiver _updateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(CoinService.CoinUpdateFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Updated coin!");
                } else {
                    displayFailSnacky(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayFailSnacky("Failed to update coin!");
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
        setContentView(R.layout.activity_coin_edit);

        _coinDto = (CoinDto) getIntent().getSerializableExtra(CoinService.CoinIntent);

        _receiverController = new ReceiverController(this);

        final AutoCompleteTextView coinEditUserTextView = findViewById(R.id.coin_edit_user_textview);
        final AutoCompleteTextView coinEditTypeTextView = findViewById(R.id.coin_edit_type_textview);
        final EditText coinAmountEditText = findViewById(R.id.coin_edit_amount_textview);

        _saveButton = findViewById(R.id.save_coin_edit_button);

        coinEditUserTextView.addTextChangedListener(_textWatcher);
        if (UserService.getInstance().IsAnUserSaved()) {
            List<String> userList = new ArrayList<>();
            userList.add(UserService.getInstance().GetUser().GetName());
            coinEditUserTextView.setAdapter(new ArrayAdapter<>(CoinEditActivity.this, android.R.layout.simple_dropdown_item_1line, userList));
        }

        coinEditUserTextView.setAdapter(new ArrayAdapter<>(CoinEditActivity.this, android.R.layout.simple_dropdown_item_1line, CoinService.getInstance().GetTypeList()));
        coinEditTypeTextView.addTextChangedListener(_textWatcher);

        coinAmountEditText.addTextChangedListener(_textWatcher);

        if (_coinDto != null) {
            coinEditUserTextView.setText(_coinDto.GetUser());
            coinEditTypeTextView.setText(_coinDto.GetType());
            coinAmountEditText.setText(String.valueOf(_coinDto.GetAmount()));
        } else {
            displayFailSnacky("Cannot work with data! Is corrupt! Please try again!");
        }

        _saveButton.setEnabled(false);
        _saveButton.setOnClickListener(view -> {
            coinEditUserTextView.setError(null);
            boolean cancel = false;
            View focusView = null;

            if (!_propertyChanged) {
                coinEditUserTextView.setError(createErrorText(getString(R.string.error_nothing_changed)));
                focusView = coinEditUserTextView;
                cancel = true;
            }

            String userName = coinEditUserTextView.getText().toString();

            if (TextUtils.isEmpty(userName)) {
                coinEditUserTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = coinEditUserTextView;
                cancel = true;
            }

            String coinType = coinEditTypeTextView.getText().toString();

            if (TextUtils.isEmpty(coinType)) {
                coinEditTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = coinEditTypeTextView;
                cancel = true;
            }

            String coinAmountString = coinAmountEditText.getText().toString();

            if (TextUtils.isEmpty(coinAmountString)) {
                coinAmountEditText.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = coinAmountEditText;
                cancel = true;
            }

            if (cancel) {
                focusView.requestFocus();
            } else {
                if (_coinDto.GetAction() == CoinDto.Action.Add) {
                    int highestId = CoinService.getInstance().GetHighestId();
                    CoinService.getInstance().AddCoin(new Coin(highestId + 1, userName, coinType, Double.parseDouble(coinAmountString), -1, Coin.Trend.NULL, -1, false, ILucaClass.LucaServerDbAction.Add));
                    _saveButton.setEnabled(false);
                } else if (_coinDto.GetAction() == CoinDto.Action.Update) {
                    CoinService.getInstance().UpdateCoin(new Coin(_coinDto.GetId(), userName, coinType, Double.parseDouble(coinAmountString), -1, Coin.Trend.NULL, -1, false, ILucaClass.LucaServerDbAction.Update));
                    _saveButton.setEnabled(false);
                } else {
                    coinEditUserTextView.setError(createErrorText(String.format(Locale.getDefault(), "Invalid action %s", _coinDto.GetAction())));
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
        _receiverController.RegisterReceiver(_addFinishedReceiver, new String[]{CoinService.CoinAddFinishedBroadcast});
        _receiverController.RegisterReceiver(_updateFinishedReceiver, new String[]{CoinService.CoinUpdateFinishedBroadcast});
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

    private void displayFailSnacky(@NonNull String message) {
        Snacky.builder()
                .setActivty(CoinEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void navigateBack(@NonNull String message) {
        Snacky.builder()
                .setActivty(CoinEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .success()
                .show();

        new Handler().postDelayed(() -> {
            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().GoBack(CoinEditActivity.this);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayFailSnacky("Failed to navigate back! Please contact LucaHome support!");
            }
        }, 1500);
    }
}
