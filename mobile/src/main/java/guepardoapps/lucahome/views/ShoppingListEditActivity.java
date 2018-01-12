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
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.ShoppingEntry;
import guepardoapps.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.ShoppingListService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

public class ShoppingListEditActivity extends AppCompatActivity {
    private static final String TAG = ShoppingListEditActivity.class.getSimpleName();

    private boolean _propertyChanged;

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _addFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(ShoppingListService.ShoppingListAddFinishedBundle);
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
        setContentView(R.layout.activity_shopping_edit);

        ShoppingEntryDto shoppingEntryDto = (ShoppingEntryDto) getIntent().getSerializableExtra(ShoppingListService.ShoppingIntent);

        _receiverController = new ReceiverController(this);

        final AutoCompleteTextView shoppingNameEditTextView = findViewById(R.id.shopping_edit_name_textview);
        final Spinner entryGroupSelect = findViewById(R.id.shopping_entry_group_select);
        final EditText quantityEditText = findViewById(R.id.shopping_entry_quantity_edittext);
        final AutoCompleteTextView shoppingUnitEditTextView = findViewById(R.id.shopping_edit_unit_textview);
        FloatingActionButton increaseQuantityButton = findViewById(R.id.floating_action_button_increase_quantity);
        FloatingActionButton decreaseQuantityButton = findViewById(R.id.floating_action_button_decrease_quantity);

        _saveButton = findViewById(R.id.save_shopping_edit_button);

        ArrayAdapter<String> shoppingNameAdapter = new ArrayAdapter<>(ShoppingListEditActivity.this, android.R.layout.simple_dropdown_item_1line, ShoppingListService.getInstance().GetShoppingNameList());
        shoppingNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shoppingNameEditTextView.setAdapter(shoppingNameAdapter);
        shoppingNameEditTextView.addTextChangedListener(_textWatcher);

        ArrayAdapter<String> groupDataAdapter = new ArrayAdapter<>(ShoppingListEditActivity.this, android.R.layout.simple_spinner_item, ShoppingListService.getInstance().GetShoppingGroupList());
        groupDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        entryGroupSelect.setAdapter(groupDataAdapter);

        ArrayAdapter<String> shoppingUnitAdapter = new ArrayAdapter<>(ShoppingListEditActivity.this, android.R.layout.simple_dropdown_item_1line, ShoppingListService.getInstance().GetShoppingUnitList());
        shoppingUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shoppingUnitEditTextView.setAdapter(shoppingUnitAdapter);
        shoppingUnitEditTextView.addTextChangedListener(_textWatcher);

        increaseQuantityButton.setOnClickListener(view -> {
            String quantityString = quantityEditText.getText().toString();
            int quantity = Integer.parseInt(quantityString);
            quantity++;
            quantityEditText.setText(String.valueOf(quantity));
        });

        decreaseQuantityButton.setOnClickListener(view -> {
            String quantityString = quantityEditText.getText().toString();
            int quantity = Integer.parseInt(quantityString);
            quantity--;
            if (quantity < 1) {
                quantity = 1;
            }
            quantityEditText.setText(String.valueOf(quantity));
        });

        if (shoppingEntryDto != null) {
            shoppingNameEditTextView.setText(shoppingEntryDto.GetName());
            entryGroupSelect.setSelection(shoppingEntryDto.GetGroup().GetInt());
            int quantity = shoppingEntryDto.GetQuantity();
            quantityEditText.setText(String.valueOf(quantity));
            shoppingUnitEditTextView.setText(shoppingEntryDto.GetUnit());
        } else {
            displayErrorSnackBar("Cannot work with data! Is corrupt! Please try again!");
        }

        _saveButton.setEnabled(false);
        _saveButton.setOnClickListener(view -> {
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

            String entryQuantity = quantityEditText.getText().toString();
            if (TextUtils.isEmpty(entryQuantity)) {
                quantityEditText.setError(createErrorText(getString(R.string.error_field_required)));
                focusView = quantityEditText;
                cancel = true;
            }

            String entryUnit = shoppingUnitEditTextView.getText().toString();

            if (cancel) {
                focusView.requestFocus();
            } else {
                int quantity = Integer.parseInt(entryQuantity);
                int highestId = ShoppingListService.getInstance().GetHighestId();
                ShoppingListService.getInstance().AddShoppingEntry(new ShoppingEntry(highestId + 1, entryName, entryGroup, quantity, entryUnit, false, ILucaClass.LucaServerDbAction.Add));
                _saveButton.setEnabled(false);
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
        _receiverController.RegisterReceiver(_addFinishedReceiver, new String[]{ShoppingListService.ShoppingListAddFinishedBroadcast});
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

        new Handler().postDelayed(() -> {
            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().GoBack(ShoppingListEditActivity.this);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate back! Please contact LucaHome support!");
            }
        }, 1500);
    }
}
