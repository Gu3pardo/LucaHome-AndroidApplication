package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.rey.material.widget.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

import de.mateware.snacky.Snacky;

import es.dmoral.toasty.Toasty;

import guepardoapps.bixby.classes.BixbyPair;
import guepardoapps.bixby.classes.actions.ApplicationAction;
import guepardoapps.bixby.classes.actions.BixbyAction;
import guepardoapps.bixby.classes.actions.NetworkAction;
import guepardoapps.bixby.classes.actions.WirelessSocketAction;
import guepardoapps.bixby.classes.requirements.BixbyRequirement;
import guepardoapps.bixby.classes.requirements.LightRequirement;
import guepardoapps.bixby.classes.requirements.NetworkRequirement;
import guepardoapps.bixby.classes.requirements.WirelessSocketRequirement;
import guepardoapps.bixby.services.BixbyPairService;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.service.PuckJsListService;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

@SuppressWarnings("deprecation")
public class BixbyEditActivity extends AppCompatActivity {
    private static final String TAG = BixbyEditActivity.class.getSimpleName();

    private Spinner _bixbyEditActionTypeSpinner;

    private Spinner _bixbyEditApplicationSpinner;

    private PercentRelativeLayout _bixbyEditNetworkRelativeLayout;
    private Spinner _bixbyEditNetworkTypeSpinner;
    private Spinner _bixbyEditNetworkActionSpinner;

    private PercentRelativeLayout _bixbyEditWirelessSocketRelativeLayout;
    private Spinner _bixbyEditWirelessSocketSpinner;
    private Spinner _bixbyEditWirelessActionSpinner;

    private static final int MIN_REQUIREMENT_COUNT = 1;
    private static final int MAX_REQUIREMENT_COUNT = 5;

    private int _currentRequirementIndex = MIN_REQUIREMENT_COUNT - 1;

    private LinearLayout _mainRequirementLinearLayout;

    private View[] _requirementContainerLinearLayout = new View[MAX_REQUIREMENT_COUNT];

    private Spinner[] _requirementTypeSpinnerArray = new Spinner[MAX_REQUIREMENT_COUNT];

    private Spinner[] _puckJsPositionSpinnerArray = new Spinner[MAX_REQUIREMENT_COUNT];

    private PercentRelativeLayout[] _lightRequirementPercentRelativeLayoutArray = new PercentRelativeLayout[MAX_REQUIREMENT_COUNT];
    private EditText[] _lightValueEditTextArray = new EditText[MAX_REQUIREMENT_COUNT];
    private EditText[] _lightToleranceEditTextArray = new EditText[MAX_REQUIREMENT_COUNT];
    private Spinner[] _lightCompareTypeSpinnerArray = new Spinner[MAX_REQUIREMENT_COUNT];
    private Spinner[] _lightAreaSpinnerArray = new Spinner[MAX_REQUIREMENT_COUNT];

    private PercentRelativeLayout[] _networkRequirementPercentRelativeLayoutArray = new PercentRelativeLayout[MAX_REQUIREMENT_COUNT];
    private Spinner[] _networkTypeSpinnerArray = new Spinner[MAX_REQUIREMENT_COUNT];
    private Spinner[] _networkStateSpinnerArray = new Spinner[MAX_REQUIREMENT_COUNT];
    private EditText[] _networkSsidEditTextArray = new EditText[MAX_REQUIREMENT_COUNT];

    private PercentRelativeLayout[] _wirelessSocketRequirementPercentRelativeLayoutArray = new PercentRelativeLayout[MAX_REQUIREMENT_COUNT];
    private Spinner[] _wirelessSocketNameSpinnerArray = new Spinner[MAX_REQUIREMENT_COUNT];
    private Spinner[] _wirelessSocketStateSpinnerArray = new Spinner[MAX_REQUIREMENT_COUNT];

    private com.rey.material.widget.Button _saveButton;

    private ReceiverController _receiverController;

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
        BixbyPair bixbyPair = (BixbyPair) getIntent().getSerializableExtra(BixbyPairService.BIXBY_PAIR_INTENT);
        // Bixby Action ui elements
        setUpActionUi(bixbyPair);
        // Bixby Requirements ui elements
        setUpRequirementsUi(bixbyPair);
        // Bixby Save ui elements
        setUpSaveUi(bixbyPair);
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

    private void setUpActionUi(BixbyPair bixbyPair) {
        _bixbyEditActionTypeSpinner = findViewById(R.id.bixby_edit_action_type_spinner);
        _bixbyEditActionTypeSpinner.setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, BixbyAction.ActionType.values()));

        _bixbyEditApplicationSpinner = findViewById(R.id.bixby_edit_application_spinner);
        _bixbyEditApplicationSpinner.setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, BixbyPairService.getInstance().GetPackageNameList()));

        _bixbyEditNetworkRelativeLayout = findViewById(R.id.bixby_edit_network_relative_layout);
        _bixbyEditNetworkTypeSpinner = findViewById(R.id.bixby_edit_network_type_spinner);
        _bixbyEditNetworkTypeSpinner.setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, NetworkAction.NetworkType.values()));
        _bixbyEditNetworkActionSpinner = findViewById(R.id.bixby_edit_network_action_spinner);
        _bixbyEditNetworkActionSpinner.setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, NetworkAction.StateType.values()));

        _bixbyEditWirelessSocketRelativeLayout = findViewById(R.id.bixby_edit_wireless_socket_relative_layout);
        _bixbyEditWirelessSocketSpinner = findViewById(R.id.bixby_edit_wireless_socket_spinner);
        _bixbyEditWirelessSocketSpinner.setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSocketService.getInstance().GetNameList()));
        _bixbyEditWirelessActionSpinner = findViewById(R.id.bixby_edit_wireless_action_spinner);
        _bixbyEditWirelessActionSpinner.setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSocketAction.StateType.values()));

        _bixbyEditActionTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BixbyAction.ActionType actionType = BixbyAction.ActionType.values()[position];
                switch (actionType) {
                    case Application:
                        _bixbyEditApplicationSpinner.setVisibility(View.VISIBLE);
                        _bixbyEditNetworkRelativeLayout.setVisibility(View.GONE);
                        _bixbyEditWirelessSocketRelativeLayout.setVisibility(View.GONE);
                        break;

                    case Network:
                        _bixbyEditApplicationSpinner.setVisibility(View.GONE);
                        _bixbyEditNetworkRelativeLayout.setVisibility(View.VISIBLE);
                        _bixbyEditWirelessSocketRelativeLayout.setVisibility(View.GONE);
                        break;

                    case WirelessSocket:
                        _bixbyEditApplicationSpinner.setVisibility(View.GONE);
                        _bixbyEditNetworkRelativeLayout.setVisibility(View.GONE);
                        _bixbyEditWirelessSocketRelativeLayout.setVisibility(View.VISIBLE);
                        break;

                    case Null:
                    default:
                        _bixbyEditApplicationSpinner.setVisibility(View.GONE);
                        _bixbyEditNetworkRelativeLayout.setVisibility(View.GONE);
                        _bixbyEditWirelessSocketRelativeLayout.setVisibility(View.GONE);
                        Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid action %s in bixbyEditActionTypeSpinner.setOnItemSelectedListener", actionType));
                        Toasty.error(BixbyEditActivity.this, "Invalid action selection!", Toast.LENGTH_LONG).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (bixbyPair != null) {
            BixbyAction bixbyAction = bixbyPair.GetAction();

            BixbyAction.ActionType actionType = bixbyAction.GetActionType();
            _bixbyEditActionTypeSpinner.setSelection(actionType.ordinal());

            switch (actionType) {
                case Application:
                    ApplicationAction applicationAction = bixbyAction.GetApplicationAction();
                    if (applicationAction != null) {
                        ArrayList<String> packageNameList = BixbyPairService.getInstance().GetPackageNameList();
                        _bixbyEditApplicationSpinner.setSelection(packageNameList.indexOf(applicationAction.GetPackageName()));
                    } else {
                        displayErrorSnackBar("Cannot work with data! ApplicationData is corrupted! Please try again!");
                    }
                    break;

                case Network:
                    NetworkAction networkAction = bixbyAction.GetNetworkAction();
                    if (networkAction != null) {
                        _bixbyEditNetworkTypeSpinner.setSelection(networkAction.GetNetworkType().ordinal());
                        _bixbyEditNetworkActionSpinner.setSelection(networkAction.GetStateType().ordinal());
                    } else {
                        displayErrorSnackBar("Cannot work with data! NetworkAction is corrupted! Please try again!");
                    }
                    break;

                case WirelessSocket:
                    WirelessSocketAction wirelessSocketAction = bixbyAction.GetWirelessSocketAction();
                    if (wirelessSocketAction != null) {
                        ArrayList<String> wirelessSocketNameList = WirelessSocketService.getInstance().GetNameList();
                        _bixbyEditWirelessSocketSpinner.setSelection(wirelessSocketNameList.indexOf(wirelessSocketAction.GetWirelessSocketName()));
                        _bixbyEditWirelessActionSpinner.setSelection(wirelessSocketAction.GetStateType().ordinal());
                    } else {
                        displayErrorSnackBar("Cannot work with data! WirelessSocketAction is corrupted! Please try again!");
                    }
                    break;

                case Null:
                default:
                    _bixbyEditApplicationSpinner.setVisibility(View.GONE);
                    _bixbyEditNetworkRelativeLayout.setVisibility(View.GONE);
                    _bixbyEditWirelessSocketRelativeLayout.setVisibility(View.GONE);
                    Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid action %s", actionType));
                    break;
            }
        }
    }

    private void setUpRequirementsUi(BixbyPair bixbyPair) {
        _mainRequirementLinearLayout = findViewById(R.id.bixby_edit_requirements_main_linear_layout);

        _requirementContainerLinearLayout[0] = findViewById(R.id.bixby_edit_requirements_linear_layout_1);

        _requirementTypeSpinnerArray[0] = findViewById(R.id.bixby_edit_requirement_type_spinner_1);
        _requirementTypeSpinnerArray[0].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, BixbyRequirement.RequirementType.values()));

        _puckJsPositionSpinnerArray[0] = findViewById(R.id.bixby_edit_puckjs_position_spinner_1);
        _puckJsPositionSpinnerArray[0].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, PuckJsListService.getInstance().GetPuckJsAreaList()));

        _lightRequirementPercentRelativeLayoutArray[0] = findViewById(R.id.bixby_edit_requirement_light_relative_layout_1);
        _lightValueEditTextArray[0] = findViewById(R.id.bixby_edit_requirement_light_value_edit_1);
        _lightToleranceEditTextArray[0] = findViewById(R.id.bixby_edit_requirement_light_tolerance_edit_1);
        _lightCompareTypeSpinnerArray[0] = findViewById(R.id.bixby_edit_requirement_light_compare_type_spinner_1);
        _lightCompareTypeSpinnerArray[0].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, LightRequirement.CompareType.values()));
        _lightAreaSpinnerArray[0] = findViewById(R.id.bixby_edit_requirement_light_area_spinner_1);
        _lightAreaSpinnerArray[0].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, PuckJsListService.getInstance().GetPuckJsAreaList()));

        _networkRequirementPercentRelativeLayoutArray[0] = findViewById(R.id.bixby_edit_requirement_network_relative_layout_1);
        _networkTypeSpinnerArray[0] = findViewById(R.id.bixby_edit_requirement_network_type_spinner_1);
        _networkTypeSpinnerArray[0].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, NetworkRequirement.NetworkType.values()));
        _networkStateSpinnerArray[0] = findViewById(R.id.bixby_edit_requirement_network_state_spinner_1);
        _networkStateSpinnerArray[0].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, NetworkRequirement.StateType.values()));
        _networkSsidEditTextArray[0] = findViewById(R.id.bixby_edit_requirement_network_ssid_edit_1);

        _wirelessSocketRequirementPercentRelativeLayoutArray[0] = findViewById(R.id.bixby_edit_requirement_wireless_socket_relative_layout_1);
        _wirelessSocketNameSpinnerArray[0] = findViewById(R.id.bixby_edit_requirement_wireless_socket_name_spinner_1);
        _wirelessSocketNameSpinnerArray[0].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSocketService.getInstance().GetNameList()));
        _wirelessSocketStateSpinnerArray[0] = findViewById(R.id.bixby_edit_requirement_wireless_socket_state_spinner_1);
        _wirelessSocketStateSpinnerArray[0].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSocketAction.StateType.values()));

        _requirementTypeSpinnerArray[0].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "_requirementTypeSpinnerArray[%d].onItemSelected", 0));

                BixbyRequirement.RequirementType requirementType = BixbyRequirement.RequirementType.values()[position];
                switch (requirementType) {
                    case Position:
                        _puckJsPositionSpinnerArray[0].setVisibility(View.VISIBLE);
                        _lightRequirementPercentRelativeLayoutArray[0].setVisibility(View.GONE);
                        _networkRequirementPercentRelativeLayoutArray[0].setVisibility(View.GONE);
                        _wirelessSocketRequirementPercentRelativeLayoutArray[0].setVisibility(View.GONE);
                        break;

                    case Light:
                        _puckJsPositionSpinnerArray[0].setVisibility(View.GONE);
                        _lightRequirementPercentRelativeLayoutArray[0].setVisibility(View.VISIBLE);
                        _networkRequirementPercentRelativeLayoutArray[0].setVisibility(View.GONE);
                        _wirelessSocketRequirementPercentRelativeLayoutArray[0].setVisibility(View.GONE);
                        break;

                    case Network:
                        _puckJsPositionSpinnerArray[0].setVisibility(View.GONE);
                        _lightRequirementPercentRelativeLayoutArray[0].setVisibility(View.GONE);
                        _networkRequirementPercentRelativeLayoutArray[0].setVisibility(View.VISIBLE);
                        _wirelessSocketRequirementPercentRelativeLayoutArray[0].setVisibility(View.GONE);
                        break;

                    case WirelessSocket:
                        _puckJsPositionSpinnerArray[0].setVisibility(View.GONE);
                        _lightRequirementPercentRelativeLayoutArray[0].setVisibility(View.GONE);
                        _networkRequirementPercentRelativeLayoutArray[0].setVisibility(View.GONE);
                        _wirelessSocketRequirementPercentRelativeLayoutArray[0].setVisibility(View.VISIBLE);
                        break;

                    case Null:
                    default:
                        _puckJsPositionSpinnerArray[0].setVisibility(View.GONE);
                        _lightRequirementPercentRelativeLayoutArray[0].setVisibility(View.GONE);
                        _networkRequirementPercentRelativeLayoutArray[0].setVisibility(View.GONE);
                        _wirelessSocketRequirementPercentRelativeLayoutArray[0].setVisibility(View.GONE);
                        Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid requirement %s in bixbyEditRequirementTypeSpinner1.setOnItemSelectedListener", requirementType));
                        Toasty.error(BixbyEditActivity.this, "Invalid requirement selection!", Toast.LENGTH_LONG).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        FloatingActionButton addRequirementFloatingActionButton = findViewById(R.id.bixby_edit_requirements_add_button);
        addRequirementFloatingActionButton.setOnClickListener(view -> {
            try {
                tryToAddRequirementView();
            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.toString());
            }
        });

        if (bixbyPair != null) {
            SerializableList<BixbyRequirement> bixbyRequirementList = bixbyPair.GetRequirements();
            int requirementListSize = bixbyRequirementList.getSize();

            if (requirementListSize >= MAX_REQUIREMENT_COUNT) {
                addRequirementFloatingActionButton.setEnabled(false);
                addRequirementFloatingActionButton.setVisibility(View.GONE);
            }

            for (int index = 0; ((index < requirementListSize) && (index <= MAX_REQUIREMENT_COUNT)); index++) {
                if (index > 0) {
                    try {
                        tryToAddRequirementView();
                    } catch (Exception exception) {
                        Logger.getInstance().Error(TAG, exception.toString());
                    }
                }

                BixbyRequirement bixbyRequirement = bixbyRequirementList.getValue(index);

                BixbyRequirement.RequirementType requirementType = bixbyRequirement.GetRequirementType();
                _requirementTypeSpinnerArray[index].setSelection(requirementType.ordinal());

                switch (requirementType) {
                    case Position:
                        String puckJsPosition = bixbyRequirement.GetPuckJsPosition();
                        ArrayList<String> puckJsPositionList = PuckJsListService.getInstance().GetPuckJsAreaList();
                        _puckJsPositionSpinnerArray[index].setSelection(puckJsPositionList.indexOf(puckJsPosition));
                        break;

                    case Light:
                        LightRequirement lightRequirement = bixbyRequirement.GetLightRequirement();
                        if (lightRequirement != null) {
                            _lightValueEditTextArray[index].setText(String.valueOf(lightRequirement.GetCompareValue()));
                            _lightToleranceEditTextArray[index].setText(String.valueOf(lightRequirement.GetToleranceInPercent()));
                            _lightCompareTypeSpinnerArray[index].setSelection(lightRequirement.GetCompareType().ordinal());
                            String lightArea = bixbyRequirement.GetLightRequirement().GetLightArea();
                            ArrayList<String> lightAreaList = PuckJsListService.getInstance().GetPuckJsAreaList();
                            _lightAreaSpinnerArray[index].setSelection(lightAreaList.indexOf(lightArea));
                        } else {
                            displayErrorSnackBar("Cannot work with data! LightRequirement is corrupted! Please try again!");
                        }
                        break;

                    case Network:
                        NetworkRequirement networkRequirement = bixbyRequirement.GetNetworkRequirement();
                        if (networkRequirement != null) {
                            _networkTypeSpinnerArray[index].setSelection(networkRequirement.GetNetworkType().ordinal());
                            _networkStateSpinnerArray[index].setSelection(networkRequirement.GetStateType().ordinal());
                            _networkSsidEditTextArray[index].setText(networkRequirement.GetWifiSsid());
                        } else {
                            displayErrorSnackBar("Cannot work with data! NetworkRequirement is corrupted! Please try again!");
                        }
                        break;

                    case WirelessSocket:
                        WirelessSocketRequirement wirelessSocketRequirement = bixbyRequirement.GetWirelessSocketRequirement();
                        if (wirelessSocketRequirement != null) {
                            ArrayList<String> wirelessSocketNameList = WirelessSocketService.getInstance().GetNameList();
                            _wirelessSocketNameSpinnerArray[index].setSelection(wirelessSocketNameList.indexOf(wirelessSocketRequirement.GetWirelessSocketName()));
                            _wirelessSocketStateSpinnerArray[index].setSelection(wirelessSocketRequirement.GetStateType().ordinal());
                        } else {
                            displayErrorSnackBar("Cannot work with data! WirelessSocketRequirement is corrupted! Please try again!");
                        }
                        break;

                    case Null:
                    default:
                        _puckJsPositionSpinnerArray[index].setVisibility(View.GONE);
                        _lightRequirementPercentRelativeLayoutArray[index].setVisibility(View.GONE);
                        _networkRequirementPercentRelativeLayoutArray[index].setVisibility(View.GONE);
                        _wirelessSocketRequirementPercentRelativeLayoutArray[index].setVisibility(View.GONE);
                        Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid requirement %s", requirementType));
                        break;
                }
            }
        }
    }

    private void tryToAddRequirementView() throws Exception {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "tryToAddRequirementView: _currentRequirementIndex: %d", _currentRequirementIndex));

        // Cancel creation if there are already MAX_REQUIREMENT_COUNT requirements
        if (_currentRequirementIndex >= MAX_REQUIREMENT_COUNT - 1) {
            return;
        }

        // Increase count for current requirements
        _currentRequirementIndex++;

        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater == null) {
            throw new NullPointerException("layoutInflater is null!");
        }
        _requirementContainerLinearLayout[_currentRequirementIndex] = layoutInflater.inflate(R.layout.layout_bixby_edit_requirement_view, null);

        // Create new Spinner to select requirement type and add it to the MainContainerLinearLayout
        _requirementTypeSpinnerArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_type_spinner);
        _requirementTypeSpinnerArray[_currentRequirementIndex].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, BixbyRequirement.RequirementType.values()));

        // Create new Spinner to select PuckJs position and add it to the MainContainerLinearLayout
        _puckJsPositionSpinnerArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_puckjs_position_spinner);
        _puckJsPositionSpinnerArray[_currentRequirementIndex].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, PuckJsListService.getInstance().GetPuckJsAreaList()));

        // Create new Container LinearLayout for new LightRequirement
        _lightRequirementPercentRelativeLayoutArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_light_relative_layout);

        // Create new EditText to enter light value for LightRequirement and add it to the LightRequirementContainer
        _lightValueEditTextArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_light_value_edit);

        // Create new EditText to enter tolerance value for LightRequirement and add it to the LightRequirementContainer
        _lightToleranceEditTextArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_light_tolerance_edit);

        // Create new Spinner to select compare type for LightRequirement and add it to the LightRequirementContainer
        _lightCompareTypeSpinnerArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_light_compare_type_spinner);
        _lightCompareTypeSpinnerArray[_currentRequirementIndex].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, LightRequirement.CompareType.values()));

        // Create new Spinner to select the area for LightRequirement and add it to the LightRequirementContainer
        _lightAreaSpinnerArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_light_area_spinner);
        _lightAreaSpinnerArray[_currentRequirementIndex].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, PuckJsListService.getInstance().GetPuckJsAreaList()));

        // Create new Container LinearLayout for new NetworkRequirement
        _networkRequirementPercentRelativeLayoutArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_network_relative_layout);

        // Create new Spinner to select network type for NetworkRequirement and add it to the NetworkRequirementContainer
        _networkTypeSpinnerArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_network_type_spinner);
        _networkTypeSpinnerArray[_currentRequirementIndex].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, NetworkRequirement.NetworkType.values()));

        // Create new Spinner to select network state for NetworkRequirement and add it to the NetworkRequirementContainer
        _networkStateSpinnerArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_network_state_spinner);
        _networkStateSpinnerArray[_currentRequirementIndex].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, NetworkRequirement.StateType.values()));

        // Create new EditText to enter the SSID for the NetworkRequirement and add it to the NetworkRequirementContainer
        _networkSsidEditTextArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_network_ssid_edit);

        // Create new Container LinearLayout for new WirelessSocketRequirement
        _wirelessSocketRequirementPercentRelativeLayoutArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_wireless_socket_relative_layout);

        // Create new Spinner to select a wirelessSocket for WirelessSocketRequirement and add it to the WirelessSocketRequirementContainer
        _wirelessSocketNameSpinnerArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_wireless_socket_name_spinner);
        _wirelessSocketNameSpinnerArray[_currentRequirementIndex].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSocketService.getInstance().GetNameList()));

        // Create new Spinner to select the wirelessSocket state for WirelessSocketRequirement and add it to the WirelessSocketRequirementContainer
        _wirelessSocketStateSpinnerArray[_currentRequirementIndex] = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_wireless_socket_state_spinner);
        _wirelessSocketStateSpinnerArray[_currentRequirementIndex].setAdapter(new ArrayAdapter<>(BixbyEditActivity.this, android.R.layout.simple_dropdown_item_1line, WirelessSocketAction.StateType.values()));

        // Handle selection of requirement type
        _requirementTypeSpinnerArray[_currentRequirementIndex].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "_requirementTypeSpinnerArray[%d].onItemSelected", _currentRequirementIndex));

                BixbyRequirement.RequirementType requirementType = BixbyRequirement.RequirementType.values()[position];
                switch (requirementType) {
                    case Position:
                        _puckJsPositionSpinnerArray[_currentRequirementIndex].setVisibility(View.VISIBLE);
                        _lightRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.GONE);
                        _networkRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.GONE);
                        _wirelessSocketRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.GONE);
                        break;

                    case Light:
                        _puckJsPositionSpinnerArray[_currentRequirementIndex].setVisibility(View.GONE);
                        _lightRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.VISIBLE);
                        _networkRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.GONE);
                        _wirelessSocketRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.GONE);
                        break;

                    case Network:
                        _puckJsPositionSpinnerArray[_currentRequirementIndex].setVisibility(View.GONE);
                        _lightRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.GONE);
                        _networkRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.VISIBLE);
                        _wirelessSocketRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.GONE);
                        break;

                    case WirelessSocket:
                        _puckJsPositionSpinnerArray[_currentRequirementIndex].setVisibility(View.GONE);
                        _lightRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.GONE);
                        _networkRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.GONE);
                        _wirelessSocketRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.VISIBLE);
                        break;

                    case Null:
                    default:
                        _puckJsPositionSpinnerArray[_currentRequirementIndex].setVisibility(View.GONE);
                        _lightRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.GONE);
                        _networkRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.GONE);
                        _wirelessSocketRequirementPercentRelativeLayoutArray[_currentRequirementIndex].setVisibility(View.GONE);
                        Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid requirement %s in bixbyEditRequirementTypeSpinner1.setOnItemSelectedListener", requirementType));
                        Toasty.error(BixbyEditActivity.this, "Invalid requirement selection!", Toast.LENGTH_LONG).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Create new button to delete the MainContainerLinearLayout (remove the requirement) and add it to the MainContainerLinearLayout
        FloatingActionButton removeRequirementViewButton = _requirementContainerLinearLayout[_currentRequirementIndex].findViewById(R.id.bixby_edit_requirement_remove_button);
        removeRequirementViewButton.setOnClickListener(view -> {
            Logger.getInstance().Debug(TAG, "removeButton.setOnClickListener");

            _mainRequirementLinearLayout.removeView(_requirementContainerLinearLayout[_currentRequirementIndex]);

            _requirementContainerLinearLayout[_currentRequirementIndex] = null;

            _requirementTypeSpinnerArray[_currentRequirementIndex] = null;
            _puckJsPositionSpinnerArray[_currentRequirementIndex] = null;

            _lightRequirementPercentRelativeLayoutArray[_currentRequirementIndex] = null;
            _lightValueEditTextArray[_currentRequirementIndex] = null;
            _lightToleranceEditTextArray[_currentRequirementIndex] = null;
            _lightCompareTypeSpinnerArray[_currentRequirementIndex] = null;
            _lightAreaSpinnerArray[_currentRequirementIndex] = null;

            _networkRequirementPercentRelativeLayoutArray[_currentRequirementIndex] = null;
            _networkTypeSpinnerArray[_currentRequirementIndex] = null;
            _networkStateSpinnerArray[_currentRequirementIndex] = null;
            _networkSsidEditTextArray[_currentRequirementIndex] = null;

            _wirelessSocketRequirementPercentRelativeLayoutArray[_currentRequirementIndex] = null;
            _wirelessSocketNameSpinnerArray[_currentRequirementIndex] = null;
            _wirelessSocketStateSpinnerArray[_currentRequirementIndex] = null;

            _currentRequirementIndex--;
        });

        // Add MainContainerLinearLayout to main view
        _mainRequirementLinearLayout.addView(_requirementContainerLinearLayout[_currentRequirementIndex]);
    }

    private void setUpSaveUi(BixbyPair bixbyPair) {
        _saveButton = findViewById(R.id.save_bixby_edit_button);
        _saveButton.setOnClickListener(view -> {
            _saveButton.setEnabled(false);

            int actionId;
            int bixbyActionId;

            BixbyPair.DatabaseAction databaseAction = BixbyPair.DatabaseAction.Add;
            if (bixbyPair != null) {
                databaseAction = bixbyPair.GetDatabaseAction();
            }

            switch (databaseAction) {
                case Add:
                    actionId = BixbyPairService.getInstance().GetHighestId();
                    bixbyActionId = BixbyPairService.getInstance().GetHighestActionId();
                    break;

                case Update:
                    actionId = bixbyPair.GetActionId();
                    if (bixbyPair.GetAction() != null) {
                        bixbyActionId = bixbyPair.GetAction().GetId();
                    } else {
                        bixbyActionId = BixbyPairService.getInstance().GetHighestActionId();
                    }
                    break;

                case Delete:
                case Null:
                default:
                    Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid databaseAction %s! Canceling!", databaseAction));
                    navigateBack(String.format(Locale.getDefault(), "Invalid databaseAction %s! Canceling!", databaseAction));
                    return;
            }

            try {
                BixbyAction newBixbyAction = buildNewBixbyAction(actionId, bixbyActionId);
                SerializableList<BixbyRequirement> newBixbyRequirementList = buildNewBixbyRequirementList(actionId, bixbyPair);
                BixbyPair newBixbyPair = new BixbyPair(actionId, newBixbyAction, newBixbyRequirementList, databaseAction);

                if (databaseAction == BixbyPair.DatabaseAction.Add) {
                    Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "Adding new BixbyPair %s", newBixbyPair));
                    BixbyPairService.getInstance().AddBixbyPair(newBixbyPair);
                } else if (databaseAction == BixbyPair.DatabaseAction.Update) {
                    Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "Updating BixbyPair %s", newBixbyPair));
                    BixbyPairService.getInstance().UpdateBixbyPair(newBixbyPair);
                } else {
                    Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid databaseAction %s! Canceling!", databaseAction));
                    navigateBack(String.format(Locale.getDefault(), "Invalid databaseAction %s! Canceling!", databaseAction));
                }
            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.toString());
                displayErrorSnackBar(exception.toString());
                _saveButton.setEnabled(true);
            }
        });
    }

    private BixbyAction buildNewBixbyAction(int actionId, int bixbyActionId) throws Exception {
        BixbyAction.ActionType newBixbyActionType = BixbyAction.ActionType.values()[_bixbyEditActionTypeSpinner.getSelectedItemPosition()];
        switch (newBixbyActionType) {
            case Application:
                String applicationPackageName = _bixbyEditApplicationSpinner.getSelectedItem().toString();
                ApplicationAction newApplicationAction = new ApplicationAction(applicationPackageName);
                return new BixbyAction(bixbyActionId, actionId, newBixbyActionType, newApplicationAction, new NetworkAction(), new WirelessSocketAction());

            case Network:
                NetworkAction.NetworkType networkType = NetworkAction.NetworkType.values()[_bixbyEditNetworkTypeSpinner.getSelectedItemPosition()];
                NetworkAction.StateType networkStateType = NetworkAction.StateType.values()[_bixbyEditNetworkActionSpinner.getSelectedItemPosition()];
                NetworkAction newNetworkAction = new NetworkAction(networkType, networkStateType);
                return new BixbyAction(bixbyActionId, actionId, newBixbyActionType, new ApplicationAction(), newNetworkAction, new WirelessSocketAction());

            case WirelessSocket:
                WirelessSocketAction.StateType wirelessSocketStateType = WirelessSocketAction.StateType.values()[_bixbyEditWirelessActionSpinner.getSelectedItemPosition()];
                String wirelessSocketName = _bixbyEditWirelessSocketSpinner.getSelectedItem().toString();
                WirelessSocketAction newWirelessSocketAction = new WirelessSocketAction(wirelessSocketStateType, wirelessSocketName);
                return new BixbyAction(bixbyActionId, actionId, newBixbyActionType, new ApplicationAction(), new NetworkAction(), newWirelessSocketAction);

            case Null:
            default:
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid actionType %s", newBixbyActionType));
                throw new NullPointerException(String.format(Locale.getDefault(), "Invalid actionType %s", newBixbyActionType));
        }
    }

    private SerializableList<BixbyRequirement> buildNewBixbyRequirementList(int actionId, BixbyPair bixbyPair) throws Exception {
        SerializableList<BixbyRequirement> newBixbyRequirementList = new SerializableList<>();
        SerializableList<BixbyRequirement> oldBixbyRequirementList;
        if (bixbyPair != null) {
            oldBixbyRequirementList = bixbyPair.GetRequirements();
        } else {
            oldBixbyRequirementList = new SerializableList<>();
        }

        for (int requirementIndex = 0; requirementIndex < _currentRequirementIndex + 1; requirementIndex++) {
            BixbyRequirement newBixbyRequirement;
            int bixbyRequirementId = BixbyPairService.getInstance().GetHighestRequirementId() + requirementIndex;

            if (oldBixbyRequirementList.getSize() - 1 > requirementIndex) {
                BixbyRequirement oldBixbyRequirement = oldBixbyRequirementList.getValue(requirementIndex);
                bixbyRequirementId = oldBixbyRequirement.GetId();
            }

            BixbyRequirement.RequirementType newRequirementType = BixbyRequirement.RequirementType.values()[_requirementTypeSpinnerArray[requirementIndex].getSelectedItemPosition()];
            switch (newRequirementType) {
                case Position:
                    String puckJsPosition = _puckJsPositionSpinnerArray[requirementIndex].getSelectedItem().toString();
                    newBixbyRequirement = new BixbyRequirement(bixbyRequirementId, actionId, newRequirementType, puckJsPosition, new LightRequirement(), new NetworkRequirement(), new WirelessSocketRequirement());
                    break;

                case Light:
                    LightRequirement.CompareType newCompareType = LightRequirement.CompareType.values()[_lightCompareTypeSpinnerArray[requirementIndex].getSelectedItemPosition()];
                    double newCompareValue = Double.parseDouble(_lightValueEditTextArray[requirementIndex].getText().toString());
                    double newToleranceValue = Double.parseDouble(_lightToleranceEditTextArray[requirementIndex].getText().toString());
                    String lightArea = _lightAreaSpinnerArray[requirementIndex].getSelectedItem().toString();
                    LightRequirement newLightRequirement = new LightRequirement(newCompareType, newCompareValue, newToleranceValue, lightArea);
                    newBixbyRequirement = new BixbyRequirement(bixbyRequirementId, actionId, newRequirementType, "", newLightRequirement, new NetworkRequirement(), new WirelessSocketRequirement());
                    break;

                case Network:
                    NetworkRequirement.NetworkType newNetworkType = NetworkRequirement.NetworkType.values()[_networkTypeSpinnerArray[requirementIndex].getSelectedItemPosition()];
                    NetworkRequirement.StateType newNetworkStateType = NetworkRequirement.StateType.values()[_networkStateSpinnerArray[requirementIndex].getSelectedItemPosition()];
                    String newWifiSsid = _networkSsidEditTextArray[requirementIndex].getText().toString();
                    NetworkRequirement newNetworkRequirement = new NetworkRequirement(newNetworkType, newNetworkStateType, newWifiSsid);
                    newBixbyRequirement = new BixbyRequirement(bixbyRequirementId, actionId, newRequirementType, "", new LightRequirement(), newNetworkRequirement, new WirelessSocketRequirement());
                    break;

                case WirelessSocket:
                    WirelessSocketRequirement.StateType newWirelessSocketStateType = WirelessSocketRequirement.StateType.values()[_wirelessSocketStateSpinnerArray[requirementIndex].getSelectedItemPosition()];
                    String newWirelessSocketName = _wirelessSocketNameSpinnerArray[requirementIndex].getSelectedItem().toString();
                    WirelessSocketRequirement newWirelessSocketRequirement = new WirelessSocketRequirement(newWirelessSocketStateType, newWirelessSocketName);
                    newBixbyRequirement = new BixbyRequirement(bixbyRequirementId, actionId, newRequirementType, "", new LightRequirement(), new NetworkRequirement(), newWirelessSocketRequirement);
                    break;

                case Null:
                default:
                    Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid requirementType %s", newRequirementType));
                    throw new NullPointerException(String.format(Locale.getDefault(), "Invalid requirementType %s", newRequirementType));
            }

            newBixbyRequirementList.addValue(newBixbyRequirement);
        }

        if (newBixbyRequirementList.getSize() == 0) {
            throw new IndexOutOfBoundsException(String.format(Locale.getDefault(), "Invalid size %d of requirementList!", newBixbyRequirementList.getSize()));
        }

        return newBixbyRequirementList;
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
