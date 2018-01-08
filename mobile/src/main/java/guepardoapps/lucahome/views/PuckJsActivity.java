package guepardoapps.lucahome.views;

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

import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;

import de.mateware.snacky.Snacky;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.adapter.PuckJsListViewAdapter;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.PuckJs;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.PuckJsListService;
import guepardoapps.lucahome.service.NavigationService;

public class PuckJsActivity extends AppCompatBaseActivity {
    /**
     * BroadcastReceiver to receive the event after download of puckjs has finished
     */
    private BroadcastReceiver _puckJsDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PuckJsListService.PuckJsListDownloadFinishedContent result = (PuckJsListService.PuckJsListDownloadFinishedContent) intent.getSerializableExtra(PuckJsListService.PuckJsListDownloadFinishedBroadcast);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(PuckJsListService.getInstance().GetLastUpdate().toString());
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

        TAG = PuckJsActivity.class.getSimpleName();

        setContentView(R.layout.activity_puckjs);

        Toolbar toolbar = findViewById(R.id.toolbar_puckjs);
        //setSupportActionBar(toolbar);
        _drawerLayout = findViewById(R.id.drawer_layout_puckjs);

        _listView = findViewById(R.id.listView_puckjs);
        _progressBar = findViewById(R.id.progressBar_puckjs);
        _noDataFallback = findViewById(R.id.fallBackTextView_puckjs);
        _lastUpdateTextView = findViewById(R.id.lastUpdateTextView_puckjs);

        _searchField = findViewById(R.id.search_puckjs);
        _searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                SerializableList<PuckJs> filteredPuckJsList = PuckJsListService.getInstance().SearchDataList(charSequence.toString());
                _listView.setAdapter(new PuckJsListViewAdapter(_context, filteredPuckJsList));
                _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d puckjs", filteredPuckJsList.getSize()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _collapsingToolbar = findViewById(R.id.collapsing_toolbar_puckjs);
        _collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        _collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _context = this;

        _receiverController = new ReceiverController(_context);

        _lastUpdateTextView.setText(PuckJsListService.getInstance().GetLastUpdate().toString());

        FloatingActionButton addButton = findViewById(R.id.floating_action_button_add_puckjs);
        addButton.setOnClickListener(view -> {
            Bundle data = new Bundle();
            data.putSerializable(PuckJsListService.PuckJsIntent, new PuckJs(PuckJsListService.getInstance().GetDataList().getSize(), "", "", "", false, ILucaClass.LucaServerDbAction.Add));

            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().NavigateToActivityWithData(_context, PuckJsEditActivity.class, data);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
            }
        });

        _drawerLayout = findViewById(R.id.drawer_layout_puckjs);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_puckjs);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_puckjs);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.INVISIBLE);
            PuckJsListService.getInstance().LoadData();
        });

        updateList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_puckJsDownloadReceiver, new String[]{PuckJsListService.PuckJsListDownloadFinishedBroadcast});
        updateList();
    }

    @Override
    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(PuckJsActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void updateList() {
        SerializableList<PuckJs> puckJsList = PuckJsListService.getInstance().GetDataList();
        if (puckJsList.getSize() > 0) {
            _listView.setAdapter(new PuckJsListViewAdapter(_context, puckJsList));

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d puckjs", puckJsList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);
    }
}
