package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.TextView;

import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.ShoppingEntry;
import guepardoapps.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.lucahome.common.service.ShoppingListService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

public class ShoppingListEditActivity extends AppCompatActivity {
    private static final String TAG = ShoppingListEditActivity.class.getSimpleName();
    private Logger _logger;

    private boolean _propertyChanged;
    private ShoppingEntryDto _shoppingEntryDto;
    private int _quantity = 1;

    private NavigationService _navigationService;
    private ShoppingListService _shoppingListService;

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _addFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_addFinishedReceiver onReceive");
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(ShoppingListService.ShoppingListAddFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Added new entry!");
                } else {
                    displayFailSnacky(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayFailSnacky("Failed to add entry!");
                _saveButton.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_edit);

        _logger = new Logger(TAG);
        _logger.Debug("onCreate");

        _shoppingEntryDto = (ShoppingEntryDto) getIntent().getSerializableExtra(ShoppingListService.ShoppingIntent);

        _navigationService = NavigationService.getInstance();
        _shoppingListService = ShoppingListService.getInstance();

        _receiverController = new ReceiverController(this);

        final Spinner entryGroupSelect = (Spinner) findViewById(R.id.shopping_entry_group_select);
        final TextView quantityTextView = (TextView) findViewById(R.id.shopping_entry_quantity_textview);
        FloatingActionButton increaseQuantityButton = (FloatingActionButton) findViewById(R.id.floating_action_button_increase_quantity);
        FloatingActionButton decreaseQuantityButton = (FloatingActionButton) findViewById(R.id.floating_action_button_decrease_quantity);
        final AutoCompleteTextView shoppingNameEditTextView = (AutoCompleteTextView) findViewById(R.id.shopping_edit_name_textview);

        _saveButton = (com.rey.material.widget.Button) findViewById(R.id.save_shopping_edit_button);

        shoppingNameEditTextView.setAdapter(new ArrayAdapter<>(ShoppingListEditActivity.this, android.R.layout.simple_dropdown_item_1line, _shoppingListService.GetShoppingNameList()));
        shoppingNameEditTextView.addTextChangedListener(new TextWatcher() {
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

        ArrayAdapter<String> groupDataAdapter = new ArrayAdapter<>(ShoppingListEditActivity.this, android.R.layout.simple_spinner_item, _shoppingListService.GetShoppingGroupList());
        groupDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        entryGroupSelect.setAdapter(groupDataAdapter);

        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _quantity++;
                quantityTextView.setText(String.valueOf(_quantity));
            }
        });

        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _quantity--;
                if (_quantity < 1) {
                    _quantity = 1;
                }
                quantityTextView.setText(String.valueOf(_quantity));
            }
        });

        if (_shoppingEntryDto != null) {
            shoppingNameEditTextView.setText(_shoppingEntryDto.GetName());
            entryGroupSelect.setSelection(_shoppingEntryDto.GetGroup().GetInt());
            _quantity = _shoppingEntryDto.GetQuantity();
            quantityTextView.setText(String.valueOf(_quantity));
        } else {
            displayFailSnacky("Cannot work with data! Is corrupt! Please try again!");
        }

        _saveButton.setEnabled(false);
        _saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shoppingNameEditTextView.setError(null);
                boolean cancel = false;
                View focusView = null;

                if (!_propertyChanged) {
                    shoppingNameEditTextView.setError(createErrorText(getString(R.string.error_nothing_changed)));
                    focusView = shoppingNameEditTextView;
                    cancel = true;
                }

                String entryName = shoppingNameEditTextView.getText().toString();

                if (TextUtils.isEmpty(entryName)) {
                    shoppingNameEditTextView.setError(createErrorText(getString(R.string.error_field_required)));
                    focusView = shoppingNameEditTextView;
                    cancel = true;
                }

                int entryGroupId = entryGroupSelect.getSelectedItemPosition();
                ShoppingEntryGroup entryGroup = ShoppingEntryGroup.GetById(entryGroupId);

                if (cancel) {
                    focusView.requestFocus();
                } else {
                    _shoppingListService.AddShoppingEntry(new ShoppingEntry(-1, entryName, entryGroup, _quantity));
                    _saveButton.setEnabled(false);
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
        _receiverController.RegisterReceiver(_addFinishedReceiver, new String[]{ShoppingListService.ShoppingListAddFinishedBroadcast});
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

    private void displayFailSnacky(@NonNull String message) {
        Snacky.builder()
                .setActivty(ShoppingListEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void navigateBack(@NonNull String message) {
        Snacky.builder()
                .setActivty(ShoppingListEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .success()
                .show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NavigationService.NavigationResult navigationResult = _navigationService.GoBack(ShoppingListEditActivity.this);
                if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                    _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                    displayFailSnacky("Failed to navigate back! Please contact LucaHome support!");
                }
            }
        }, 1500);
    }
}
