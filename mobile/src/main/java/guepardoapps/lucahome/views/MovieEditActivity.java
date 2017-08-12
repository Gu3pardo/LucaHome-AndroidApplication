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
import android.widget.RatingBar;

import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.Movie;
import guepardoapps.lucahome.common.dto.MovieDto;
import guepardoapps.lucahome.common.service.MovieService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.service.NavigationService;

public class MovieEditActivity extends AppCompatActivity {
    private static final String TAG = MovieEditActivity.class.getSimpleName();
    private Logger _logger;

    private boolean _propertyChanged;
    private MovieDto _movieDto;

    private MovieService _movieService;
    private NavigationService _navigationService;

    private ReceiverController _receiverController;

    private com.rey.material.widget.Button _saveButton;

    private BroadcastReceiver _updateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_addFinishedReceiver onReceive");
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(MovieService.MovieUpdateFinishedBundle);
            if (result != null) {
                if (result.Success) {
                    navigateBack("Updated movie!");
                } else {
                    displayFailSnacky(Tools.DecompressByteArrayToString(result.Response));
                    _saveButton.setEnabled(true);
                }
            } else {
                displayFailSnacky("Failed to update movie!");
                _saveButton.setEnabled(true);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_edit);

        _logger = new Logger(TAG);
        _logger.Debug("onCreate");

        _movieDto = (MovieDto) getIntent().getSerializableExtra(MovieService.MovieIntent);

        _movieService = MovieService.getInstance();
        _navigationService = NavigationService.getInstance();

        _receiverController = new ReceiverController(this);

        final AutoCompleteTextView movieTitleTypeTextView = (AutoCompleteTextView) findViewById(R.id.movie_edit_title_textview);
        final AutoCompleteTextView movieGenreTypeTextView = (AutoCompleteTextView) findViewById(R.id.movie_edit_genre_textview);
        final AutoCompleteTextView movieDescriptionTypeTextView = (AutoCompleteTextView) findViewById(R.id.movie_edit_description_textview);
        final RatingBar movieRatingBar = (RatingBar) findViewById(R.id.movieRating_Bar);

        _saveButton = (com.rey.material.widget.Button) findViewById(R.id.save_movie_edit_button);

        TextWatcher sharedTextWatcher = new TextWatcher() {
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

        movieTitleTypeTextView.setAdapter(new ArrayAdapter<>(MovieEditActivity.this, android.R.layout.simple_dropdown_item_1line, _movieService.GetTitleList()));
        movieTitleTypeTextView.addTextChangedListener(sharedTextWatcher);

        movieGenreTypeTextView.setAdapter(new ArrayAdapter<>(MovieEditActivity.this, android.R.layout.simple_dropdown_item_1line, _movieService.GetGenreList()));
        movieGenreTypeTextView.addTextChangedListener(sharedTextWatcher);

        movieDescriptionTypeTextView.setAdapter(new ArrayAdapter<>(MovieEditActivity.this, android.R.layout.simple_dropdown_item_1line, _movieService.GetDescriptionList()));
        movieDescriptionTypeTextView.addTextChangedListener(sharedTextWatcher);


        if (_movieDto != null) {
            movieTitleTypeTextView.setText(_movieDto.GetTitle());
            movieGenreTypeTextView.setText(_movieDto.GetGenre());
            movieDescriptionTypeTextView.setText(_movieDto.GetDescription());
            movieRatingBar.setRating(_movieDto.GetRating());
        } else {
            displayFailSnacky("Cannot work with data! Is corrupt! Please try again!");
        }

        _saveButton.setEnabled(false);
        _saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movieTitleTypeTextView.setError(null);
                movieGenreTypeTextView.setError(null);
                movieDescriptionTypeTextView.setError(null);
                boolean cancel = false;
                View focusView = null;

                if (!_propertyChanged) {
                    movieTitleTypeTextView.setError(createErrorText(getString(R.string.error_nothing_changed)));
                    focusView = movieTitleTypeTextView;
                    cancel = true;
                }

                String title = movieTitleTypeTextView.getText().toString();

                if (TextUtils.isEmpty(title)) {
                    movieTitleTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                    focusView = movieTitleTypeTextView;
                    cancel = true;
                }

                String genre = movieGenreTypeTextView.getText().toString();

                if (TextUtils.isEmpty(genre)) {
                    movieGenreTypeTextView.setError(createErrorText(getString(R.string.error_field_required)));
                    focusView = movieGenreTypeTextView;
                    cancel = true;
                }

                String description = movieDescriptionTypeTextView.getText().toString();

                if (TextUtils.isEmpty(description)) {
                    movieDescriptionTypeTextView.setError(createErrorText(getString(R.string.error_nothing_changed)));
                    focusView = movieDescriptionTypeTextView;
                    cancel = true;
                }

                if (cancel) {
                    focusView.requestFocus();
                } else {
                    _movieService.UpdateMovie(new Movie(_movieDto.GetId(), title, genre, description, (int) movieRatingBar.getRating()/*, _movieDto.GetWatched()*/));
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
        _receiverController.RegisterReceiver(_updateFinishedReceiver, new String[]{MovieService.MovieUpdateFinishedBroadcast});
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
                .setActivty(MovieEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .error()
                .show();
    }

    private void navigateBack(@NonNull String message) {
        Snacky.builder()
                .setActivty(MovieEditActivity.this)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .success()
                .show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NavigationService.NavigationResult navigationResult = _navigationService.GoBack(MovieEditActivity.this);
                if (navigationResult != NavigationService.NavigationResult.SUCCESS) {
                    _logger.Error(String.format(Locale.getDefault(), "Navigation failed! navigationResult is %s!", navigationResult));
                    displayFailSnacky("Failed to navigate back! Please contact LucaHome support!");
                }
            }
        }, 1500);
    }
}
