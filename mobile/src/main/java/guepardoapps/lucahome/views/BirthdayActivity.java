package guepardoapps.lucahome.views;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;

import de.mateware.snacky.Snacky;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.adapter.BirthdayListViewAdapter;
import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaBirthday;
import guepardoapps.lucahome.common.dto.BirthdayDto;
import guepardoapps.lucahome.common.service.BirthdayService;
import guepardoapps.lucahome.service.NavigationService;

public class BirthdayActivity extends AppCompatBaseActivity {
    /**
     * BroadcastReceiver to receive the event after download of birthdays has finished
     */
    private BroadcastReceiver _birthdayDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BirthdayService.BirthdayDownloadFinishedContent result = (BirthdayService.BirthdayDownloadFinishedContent) intent.getSerializableExtra(BirthdayService.BirthdayDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(BirthdayService.getInstance().GetLastUpdate().toString());
                updateList();
            } else {
                displayErrorSnackBar(Tools.DecompressByteArrayToString(result.Response));
                _noDataFallback.setVisibility(View.VISIBLE);
                _searchField.setVisibility(View.INVISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = BirthdayActivity.class.getSimpleName();

        setContentView(R.layout.activity_birthday);

        Toolbar toolbar = findViewById(R.id.toolbar_birthday);
        //setSupportActionBar(toolbar);
        _drawerLayout = findViewById(R.id.drawer_layout_birthday);

        _listView = findViewById(R.id.listView_birthday);
        _progressBar = findViewById(R.id.progressBar_birthday);
        _noDataFallback = findViewById(R.id.fallBackTextView_birthday);
        _lastUpdateTextView = findViewById(R.id.lastUpdateTextView_birthday);

        _searchField = findViewById(R.id.search_birthday);
        _searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                SerializableList<LucaBirthday> filteredBirthdayList = BirthdayService.getInstance().SearchDataList(charSequence.toString());
                _listView.setAdapter(new BirthdayListViewAdapter(_context, filteredBirthdayList));
                _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d birthdays", filteredBirthdayList.getSize()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _collapsingToolbar = findViewById(R.id.collapsing_toolbar_birthday);
        _collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        _collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _context = this;

        _receiverController = new ReceiverController(_context);

        _lastUpdateTextView.setText(BirthdayService.getInstance().GetLastUpdate().toString());

        FloatingActionButton addButton = findViewById(R.id.floating_action_button_add_birthday);
        addButton.setOnClickListener(view -> {
            Bundle data = new Bundle();
            data.putSerializable(BirthdayService.BirthdayIntent, new BirthdayDto(BirthdayService.getInstance().GetHighestId() + 1, "", new SerializableDate(), "", true, BirthdayDto.Action.Add));

            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().NavigateToActivityWithData(_context, BirthdayEditActivity.class, data);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
            }
        });

        _drawerLayout = findViewById(R.id.drawer_layout_birthday);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_birthday);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_birthday);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.INVISIBLE);
            BirthdayService.getInstance().LoadData();
        });

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_CONTACTS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Toasty.success(BirthdayActivity.this, "Permission READ_CONTACTS granted! Thanks!", Toast.LENGTH_LONG).show();
                        BirthdayService.getInstance().RetrieveContactPhotos();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        displayErrorSnackBar("Read contacts is necessary for birthday list!");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();

        updateList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_birthdayDownloadReceiver, new String[]{BirthdayService.BirthdayDownloadFinishedBroadcast});
        updateList();
    }

    @Override
    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(BirthdayActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void updateList() {
        SerializableList<LucaBirthday> birthdayList = BirthdayService.getInstance().GetDataList();
        if (birthdayList.getSize() > 0) {
            _listView.setAdapter(new BirthdayListViewAdapter(_context, birthdayList));

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d birthdays", birthdayList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);
    }
}
