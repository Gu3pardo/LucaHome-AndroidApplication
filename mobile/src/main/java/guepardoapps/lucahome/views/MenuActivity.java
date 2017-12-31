package guepardoapps.lucahome.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import de.mateware.snacky.Snacky;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.adapter.MenuListViewAdapter;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaMenu;
import guepardoapps.lucahome.common.service.MenuService;

public class MenuActivity extends AppCompatBaseActivity {
    /**
     * BroadcastReceiver to receive updates
     */
    private BroadcastReceiver _menuUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MenuService.MenuDownloadFinishedContent result = (MenuService.MenuDownloadFinishedContent) intent.getSerializableExtra(MenuService.MenuDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(MenuService.getInstance().GetLastUpdate().toString());
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

        TAG = MenuActivity.class.getSimpleName();

        setContentView(R.layout.activity_menu);

        Toolbar toolbar = findViewById(R.id.toolbar_menu);
        //setSupportActionBar(toolbar);

        _listView = findViewById(R.id.listView_menu);
        _progressBar = findViewById(R.id.progressBar_menu);
        _noDataFallback = findViewById(R.id.fallBackTextView_menu);
        _lastUpdateTextView = findViewById(R.id.lastUpdateTextView_menu);

        _searchField = findViewById(R.id.search_menu);
        _searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                SerializableList<LucaMenu> filteredMenuList = MenuService.getInstance().SearchDataList(charSequence.toString());
                _listView.setAdapter(new MenuListViewAdapter(_context, filteredMenuList));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _shareButton = findViewById(R.id.floating_action_button_share_menu);
        _shareButton.setOnClickListener(view -> MenuService.getInstance().ShareMenuList());

        _context = this;

        _receiverController = new ReceiverController(_context);

        _lastUpdateTextView.setText(MenuService.getInstance().GetLastUpdate().toString());

        updateList();

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar_menu);
        collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _drawerLayout = findViewById(R.id.drawer_layout_menu);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_menu);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_menu);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.INVISIBLE);
            _shareButton.setVisibility(View.GONE);
            MenuService.getInstance().LoadData();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_menuUpdateReceiver, new String[]{MenuService.MenuDownloadFinishedBroadcast});
        updateList();
    }

    @Override
    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(MenuActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void updateList() {
        SerializableList<LucaMenu> menuList = MenuService.getInstance().GetDataList();
        if (menuList.getSize() > 0) {
            _listView.setAdapter(new MenuListViewAdapter(_context, menuList));

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);
            _shareButton.setVisibility(View.VISIBLE);
        }
        _progressBar.setVisibility(View.GONE);
    }
}
