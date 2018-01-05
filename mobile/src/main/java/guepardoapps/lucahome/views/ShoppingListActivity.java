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

import java.util.Locale;

import de.mateware.snacky.Snacky;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.adapter.ShoppingListViewAdapter;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.ShoppingEntry;
import guepardoapps.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.lucahome.common.service.ShoppingListService;
import guepardoapps.lucahome.service.NavigationService;

public class ShoppingListActivity extends AppCompatBaseActivity {
    /**
     * BroadcastReceiver to receive updates
     */
    private BroadcastReceiver _shoppingListUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ShoppingListService.ShoppingListDownloadFinishedContent result = (ShoppingListService.ShoppingListDownloadFinishedContent) intent.getSerializableExtra(ShoppingListService.ShoppingListDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);
            _addButton.setVisibility(View.VISIBLE);

            if (result.Success) {
                _lastUpdateTextView.setText(ShoppingListService.getInstance().GetLastUpdate().toString());
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

        TAG = ShoppingListActivity.class.getSimpleName();

        setContentView(R.layout.activity_shopping);

        Toolbar toolbar = findViewById(R.id.toolbar_shoppingList);
        //setSupportActionBar(toolbar);

        _listView = findViewById(R.id.listView_shoppingList);
        _progressBar = findViewById(R.id.progressBar_shoppingList);
        _noDataFallback = findViewById(R.id.fallBackTextView_shoppingList);
        _lastUpdateTextView = findViewById(R.id.lastUpdateTextView_shoppingList);

        _searchField = findViewById(R.id.search_shoppingList);
        _searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                SerializableList<ShoppingEntry> filteredShoppingList = ShoppingListService.getInstance().SearchDataList(charSequence.toString());
                _listView.setAdapter(new ShoppingListViewAdapter(_context, filteredShoppingList));
                _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d entries", filteredShoppingList.getSize()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _collapsingToolbar = findViewById(R.id.collapsing_toolbar_shoppingList);
        _collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        _collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _shareButton = findViewById(R.id.floating_action_button_share_shoppingList);
        _shareButton.setOnClickListener(view -> ShoppingListService.getInstance().ShareShoppingList());

        _context = this;

        _receiverController = new ReceiverController(_context);

        _lastUpdateTextView.setText(ShoppingListService.getInstance().GetLastUpdate().toString());
        updateList();

        _addButton = findViewById(R.id.floating_action_button_add_shoppingList);
        _addButton.setOnClickListener(view -> {
            Bundle data = new Bundle();
            data.putSerializable(ShoppingListService.ShoppingIntent, new ShoppingEntryDto(-1, "", ShoppingEntryGroup.OTHER, 1, "e"));

            NavigationService.NavigationResult navigationResult = NavigationService.getInstance().NavigateToActivityWithData(_context, ShoppingListEditActivity.class, data);
            if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                displayErrorSnackBar("Failed to navigate! Please contact LucaHome support!");
            }
        });

        _drawerLayout = findViewById(R.id.drawer_layout_shopping);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_shopping);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_shoppingList);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.INVISIBLE);
            _addButton.setVisibility(View.GONE);
            _shareButton.setVisibility(View.GONE);
            ShoppingListService.getInstance().LoadData();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_shoppingListUpdateReceiver, new String[]{ShoppingListService.ShoppingListDownloadFinishedBroadcast});
        updateList();
    }

    @Override
    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(ShoppingListActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void updateList() {
        SerializableList<ShoppingEntry> shoppingList = ShoppingListService.getInstance().GetDataList();
        if (shoppingList.getSize() > 0) {
            _listView.setAdapter(new ShoppingListViewAdapter(_context, shoppingList));

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);
            _shareButton.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d entries", shoppingList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);
    }
}
