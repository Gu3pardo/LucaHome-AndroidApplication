package guepardoapps.library.lucahome.customadapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.dto.MovieDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.controller.MovieController;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class MovieListAdapter extends BaseAdapter {

    private static final String TAG = MovieListAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final int MAX_MOVIE_RATING = 5;

    private SerializableList<MovieDto> _movieList;

    private Context _context;

    private MovieController _movieController;
    private LucaDialogController _dialogController;

    private static LayoutInflater _inflater = null;

    public MovieListAdapter(
            @NonNull Context context,
            @NonNull SerializableList<MovieDto> movieList) {
        _logger = new LucaHomeLogger(TAG);

        _movieList = movieList;
        for (int index = 0; index < _movieList.getSize(); index++) {
            _logger.Debug(_movieList.getValue(index).toString());
        }

        _context = context;

        _movieController = new MovieController(_context);
        _dialogController = new LucaDialogController(_context);

        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _movieList.getSize();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder {
        private Button _title;
        private TextView _genre;
        private TextView _watched;
        private TextView _rating;
        private Button _play;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_movie_item, null);

        holder._title = (Button) rowView.findViewById(R.id.movie_item_title);
        holder._title.setText(_movieList.getValue(index).GetTitle());
        holder._title.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                _logger.Debug("onLongClick _title button: " + _movieList.getValue(index).GetTitle());
                _dialogController.ShowUpdateMovieDialog(_movieList.getValue(index));
                return true;
            }
        });

        holder._genre = (TextView) rowView.findViewById(R.id.movie_item_genre);
        holder._genre.setText(_movieList.getValue(index).GetGenre());

        holder._watched = (TextView) rowView.findViewById(R.id.movie_item_watched);
        holder._watched.setText(String.valueOf(_movieList.getValue(index).GetWatched()));

        holder._rating = (TextView) rowView.findViewById(R.id.movie_item_rating);
        holder._rating.setText(String.format(Locale.GERMAN, "%d/%d", _movieList.getValue(index).GetRating(), MAX_MOVIE_RATING));

        holder._play = (Button) rowView.findViewById(R.id.movie_item_play);
        holder._play.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                _movieController.StartMovie(_movieList.getValue(index));

                Snacky.builder()
                        .setActivty((Activity) _context)
                        .setText(String.format(Locale.GERMAN,
                                "Trying to start movie %s",
                                _movieList.getValue(index).GetTitle()))
                        .setDuration(Snacky.LENGTH_LONG)
                        .setActionText(_context.getResources().getString(android.R.string.ok))
                        .info()
                        .show();
            }
        });

        return rowView;
    }
}