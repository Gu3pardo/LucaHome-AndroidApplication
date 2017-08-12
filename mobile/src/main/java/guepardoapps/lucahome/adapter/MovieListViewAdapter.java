package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.Movie;
import guepardoapps.lucahome.common.dto.MovieDto;
import guepardoapps.lucahome.common.service.MovieService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.MovieEditActivity;

public class MovieListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _titleText;
        private TextView _genreText;
        private TextView _descriptionText;
        private TextView _ratingText;
        private TextView _watchedText;
        private FloatingActionButton _updateButton;
    }

    private static final String TAG = MovieListViewAdapter.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    private NavigationService _navigationService;

    private static LayoutInflater _inflater = null;

    private SerializableList<Movie> _listViewItems;

    public MovieListViewAdapter(@NonNull Context context, @NonNull SerializableList<Movie> listViewItems) {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");

        _context = context;

        _listViewItems = listViewItems;

        _navigationService = NavigationService.getInstance();

        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        _logger.Debug(String.format(Locale.getDefault(), "getCount: %d", _listViewItems.getSize()));
        return _listViewItems.getSize();
    }

    @Override
    public Object getItem(int position) {
        _logger.Debug(String.format(Locale.getDefault(), "getItem: %d", position));
        return position;
    }

    @Override
    public long getItemId(int position) {
        _logger.Debug(String.format(Locale.getDefault(), "getItemId: %d", position));
        return position;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();

        View rowView = _inflater.inflate(R.layout.listview_card_movie, null);

        holder._titleText = rowView.findViewById(R.id.movieCardTitleText);
        holder._genreText = rowView.findViewById(R.id.movieGenreText);
        holder._descriptionText = rowView.findViewById(R.id.movieDescriptionText);
        holder._ratingText = rowView.findViewById(R.id.movieRatingText);
        holder._watchedText = rowView.findViewById(R.id.movieWatchedText);
        holder._updateButton = rowView.findViewById(R.id.movieCardUpdateButton);

        holder._watchedText.setVisibility(View.GONE);

        final Movie movie = _listViewItems.getValue(index);

        holder._titleText.setText(movie.GetTitle());
        holder._genreText.setText(movie.GetGenre());
        holder._descriptionText.setText(movie.GetDescription());
        holder._ratingText.setText(movie.GetRatingString());
        //holder._watchedText.setText(String.format(Locale.getDefault(), "%d / 5", movie.GetWatched()));

        holder._updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("setOnClickListener onClick");
                Bundle data = new Bundle();
                data.putSerializable(MovieService.MovieIntent, new MovieDto(movie.GetId(), movie.GetTitle(), movie.GetGenre(), movie.GetDescription(), movie.GetRating()/*, movie.GetWatched()*/));
                _navigationService.NavigateToActivityWithData(_context, MovieEditActivity.class, data);
            }
        });

        return rowView;
    }
}
