package guepardoapps.lucahome.view.customadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.common.dto.MovieDto;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.helper.DialogService;
import guepardoapps.lucahome.view.controller.MovieController;

public class MovieListAdapter extends BaseAdapter {

	private static final String TAG = MovieListAdapter.class.getName();
	private LucaHomeLogger _logger;

	private SerializableList<MovieDto> _movieList;

	private Context _context;

	private MovieController _movieController;
	private DialogService _dialogService;

	private static LayoutInflater _inflater = null;

	public MovieListAdapter(Context context, SerializableList<MovieDto> movieList) {
		_logger = new LucaHomeLogger(TAG);

		_movieList = movieList;
		for (int index = 0; index < _movieList.getSize(); index++) {
			_logger.Debug(_movieList.getValue(index).toString());
		}

		_context = context;

		_movieController = new MovieController(_context);
		_dialogService = new DialogService(_context);

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

	@SuppressLint({ "InflateParams", "ViewHolder" })
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
				_dialogService.ShowUpdateMovieDialog(_movieList.getValue(index));
				return true;
			}
		});

		holder._genre = (TextView) rowView.findViewById(R.id.movie_item_genre);
		holder._genre.setText(_movieList.getValue(index).GetGenre());

		holder._watched = (TextView) rowView.findViewById(R.id.movie_item_watched);
		holder._watched.setText(String.valueOf(_movieList.getValue(index).GetWatched()));

		holder._rating = (TextView) rowView.findViewById(R.id.movie_item_rating);
		holder._rating.setText(String.valueOf(_movieList.getValue(index).GetRating()) + "/5");

		holder._play = (Button) rowView.findViewById(R.id.movie_item_play);
		holder._play.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_movieController.StartMovie(_movieList.getValue(index));
			}
		});

		return rowView;
	}
}