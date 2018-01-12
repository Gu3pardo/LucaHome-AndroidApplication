package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;

import de.mateware.snacky.Snacky;

import es.dmoral.toasty.Toasty;
import guepardoapps.bixby.classes.BixbyPair;
import guepardoapps.bixby.classes.actions.BixbyAction;
import guepardoapps.bixby.classes.actions.NetworkAction;
import guepardoapps.bixby.classes.actions.WirelessSocketAction;
import guepardoapps.bixby.services.BixbyPairService;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

@SuppressWarnings("deprecation")
public class BixbyEditActivity extends AppCompatActivity {
    private static final String TAG = BixbyEditActivity.class.getSimpleName();

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _addFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(BixbyPairService.BixbyPairAddFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Added new bixby pair!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to add bixby pair!");
                _saveButton.setEnabled(true);
            }
        }
    };

    private BroadcastReceiver _updateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(BixbyPairService.BixbyPairUpdateFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Updated bixby pair!");
                } else {
                    displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayErrorSnackBar("Failed to update bixby pair!");
                _saveButton.setEnabled(true);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bixby_edit);
        _receiverController = new ReceiverController(this);
        _saveButton = findViewById(R.id.save_bixby_edit_button);

        // Bixby Action ui elements

        Spinner bixbyEditActionTypeSpinner = findViewById(R.id.bixby_edit_action_type_spinner);
        bixbyEditActionTypeSpinner.setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, BixbyAction.ActionType.values()));

        Spinner bixbyEditApplicationSpinner = findViewById(R.id.bixby_edit_application_spinner);
        bixbyEditApplicationSpinner.setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, BixbyPairService.getInstance().GetPackageNameList()));

        PercentRelativeLayout bixbyEditNetworkRelativeLayout = findViewById(R.id.bixby_edit_network_relative_layout);
        Spinner bixbyEditNetworkTypeSpinner = findViewById(R.id.bixby_edit_network_type_spinner);
        bixbyEditNetworkTypeSpinner.setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, NetworkAction.NetworkType.values()));
        Spinner bixbyEditNetworkActionSpinner = findViewById(R.id.bixby_edit_network_action_spinner);
        bixbyEditNetworkActionSpinner.setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, NetworkAction.StateType.values()));

        PercentRelativeLayout bixbyEditWirelessSocketRelativeLayout = findViewById(R.id.bixby_edit_wireless_socket_relative_layout);
        Spinner bixbyEditWirelessSocketSpinner = findViewById(R.id.bixby_edit_wireless_socket_spinner);
        bixbyEditWirelessSocketSpinner.setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSocketService.getInstance().GetNameList()));
        Spinner bixbyEditWirelessActionSpinner = findViewById(R.id.bixby_edit_wireless_action_spinner);
        bixbyEditWirelessActionSpinner.setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSocketAction.StateType.values()));

        bixbyEditActionTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BixbyAction.ActionType actionType = BixbyAction.ActionType.values()[position];
                switch (actionType) {
                    case Application:
                        bixbyEditApplicationSpinner.setVisibility(View.VISIBLE);
                        bixbyEditNetworkRelativeLayout.setVisibility(View.GONE);
                        bixbyEditWirelessSocketRelativeLayout.setVisibility(View.GONE);
                        break;
                    case Network:
                        bixbyEditApplicationSpinner.setVisibility(View.GONE);
                        bixbyEditNetworkRelativeLayout.setVisibility(View.VISIBLE);
                        bixbyEditWirelessSocketRelativeLayout.setVisibility(View.GONE);
                        break;
                    case WirelessSocket:
                        bixbyEditApplicationSpinner.setVisibility(View.GONE);
                        bixbyEditNetworkRelativeLayout.setVisibility(View.GONE);
                        bixbyEditWirelessSocketRelativeLayout.setVisibility(View.VISIBLE);
                        break;
                    case Null:
                    default:
                        bixbyEditApplicationSpinner.setVisibility(View.GONE);
                        bixbyEditNetworkRelativeLayout.setVisibility(View.GONE);
                        bixbyEditWirelessSocketRelativeLayout.setVisibility(View.GONE);
                        Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid action %s in bixbyEditActionTypeSpinner.setOnItemSelectedListener", actionType));
                        Toasty.error(BixbyEditActivity.this, "Invalid action selection!", Toast.LENGTH_LONG).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // TODO add requirement ui elements

        BixbyPair bixbyPair = (BixbyPair) getIntent().getSerializableExtra(BixbyPairService.BIXBY_PAIR_INTENT);
        if (bixbyPair != null) {
            // TODO add ui elements
        } else {
            displayErrorSnackBar("Cannot work with data! Is corrupt! Please try again!");
        }

        // TODO add ui elements

        _saveButton.setOnClickListener(view -> {
            _saveButton.setEnabled(false);
            // TODO add ui elements
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_addFinishedReceiver, new String[]{BixbyPairService.BixbyPairAddFinishedBroadcast});
        _receiverController.RegisterReceiver(_updateFinishedReceiver, new String[]{BixbyPairService.BixbyPairUpdateFinishedBroadcast});
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

    private void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(BixbyEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void navigateBack(@NonNull String message) {
        Snacky.builder()
                .setActivty(BixbyEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .success()
                .show();

        new Handler().postDelayed(() -> {
            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().GoBack(BixbyEditActivity.this);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate back! Please contact LucaHome support!");
            }
        }, 1500);
    }
}
