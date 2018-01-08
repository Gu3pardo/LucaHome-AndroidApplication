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
import guepardoapps.lucahome.adapter.MovieListViewAdapter;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.Movie;
import guepardoapps.lucahome.common.service.MovieService;

public class MovieActivity extends AppCompatBaseActivity {
    /**
     * BroadcastReceiver to receive the event after download of movies has finished
     */
    private BroadcastReceiver _movieDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MovieService.MovieListDownloadFinishedContent result = (MovieService.MovieListDownloadFinishedContent) intent.getSerializableExtra(MovieService.MovieDownloadFinishedBundle);

            _progressBar.setVisibility(View.GONE);
            _searchField.setText("");
            _pullRefreshLayout.setRefreshing(false);

            if (result.Success) {
                _lastUpdateTextView.setText(MovieService.getInstance().GetLastUpdate().toString());
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

        TAG = MovieActivity.class.getSimpleName();

        setContentView(R.layout.activity_movie);

        Toolbar toolbar = findViewById(R.id.toolbar_movie);
        //setSupportActionBar(toolbar);

        _listView = findViewById(R.id.listView_movie);
        _progressBar = findViewById(R.id.progressBar_movie);
        _noDataFallback = findViewById(R.id.fallBackTextView_movie);
        _lastUpdateTextView = findViewById(R.id.lastUpdateTextView_movie);

        _searchField = findViewById(R.id.search_movie);
        _searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                SerializableList<Movie> filteredMovieList = MovieService.getInstance().SearchDataList(charSequence.toString());
                _listView.setAdapter(new MovieListViewAdapter(_context, filteredMovieList));
                _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d movies", filteredMovieList.getSize()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        _collapsingToolbar = findViewById(R.id.collapsing_toolbar_movie);
        _collapsingToolbar.setExpandedTitleColor(android.graphics.Color.argb(0, 0, 0, 0));
        _collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.TextIcon));

        _context = this;

        _receiverController = new ReceiverController(_context);

        _lastUpdateTextView.setText(MovieService.getInstance().GetLastUpdate().toString());
        updateList();

        _drawerLayout = findViewById(R.id.drawer_layout_movie);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_movie);
        navigationView.setNavigationItemSelectedListener(this);

        _pullRefreshLayout = findViewById(R.id.pullRefreshLayout_movie);
        _pullRefreshLayout.setOnRefreshListener(() -> {
            _listView.setVisibility(View.GONE);
            _progressBar.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.INVISIBLE);
            MovieService.getInstance().LoadData();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_movieDownloadReceiver, new String[]{MovieService.MovieDownloadFinishedBroadcast});
        updateList();
    }

    @Override
    protected void displayErrorSnackBar(@NonNull String message) {
        Snacky.builder()
                .setActivty(MovieActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void updateList() {
        SerializableList<Movie> movieList = MovieService.getInstance().GetDataList();
        if (movieList.getSize() > 0) {
            _listView.setAdapter(new MovieListViewAdapter(_context, movieList));

            _noDataFallback.setVisibility(View.GONE);
            _listView.setVisibility(View.VISIBLE);
            _searchField.setVisibility(View.VISIBLE);

            _collapsingToolbar.setTitle(String.format(Locale.getDefault(), "%d movies", movieList.getSize()));
        }
        _progressBar.setVisibility(View.GONE);
    }
}
