package guepardoapps.library.lucahome.customadapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import de.mateware.snacky.Snacky;
import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.Keys;
import guepardoapps.library.lucahome.common.dto.MovieDto;
import guepardoapps.library.lucahome.common.dto.YoutubeVideoDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.controller.MovieController;

import guepardoapps.library.lucahome.tasks.DownloadYoutubeVideoTask;
import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.NetworkController;
import guepardoapps.library.toolset.controller.ReceiverController;

public class MovieListAdapter extends BaseAdapter {

    private static final String TAG = MovieListAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final int MAX_MOVIE_RATING = 5;

    private SerializableList<MovieDto> _movieList;

    private Context _context;

    private MovieController _movieController;
    private LucaDialogController _dialogController;
    private NetworkController _networkController;

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
        _networkController = new NetworkController(_context, _dialogController);

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
        private ImageView _image;
        private Button _title;

        private TextView _genre;
        private TextView _watched;
        private TextView _rating;

        private Button _play;

        private ImageButton _youtubeTrailer;
        private ImageButton _imdb;
        private ImageButton _wikipedia;

        private ReceiverController _receiverController;
        private BroadcastReceiver _youtubeInformationReceiver;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        final Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_movie_item, null);

        holder._image = (ImageView) rowView.findViewById(R.id.movie_item_image);

        holder._title = (Button) rowView.findViewById(R.id.movie_item_title);
        holder._title.setText(_movieList.getValue(index).GetTitle());
        holder._title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
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
        holder._play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        holder._youtubeTrailer = (ImageButton) rowView.findViewById(R.id.movie_item_button_trailer);
        holder._youtubeTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.format(
                        Locale.getDefault(),
                        "https://www.youtube.com/results?search_query=%s+trailer",
                        _movieList.getValue(index).GetTitle()).replace(" ", "+");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                _context.startActivity(intent);
            }
        });

        holder._imdb = (ImageButton) rowView.findViewById(R.id.movie_item_button_imdb);
        holder._imdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.format(
                        Locale.getDefault(),
                        "http://www.imdb.com/find?ref_=nv_sr_fn&q=%s&s=all",
                        _movieList.getValue(index).GetTitle()).replace(" ", "+");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                _context.startActivity(intent);
            }
        });

        holder._wikipedia = (ImageButton) rowView.findViewById(R.id.movie_item_button_wikipedia);
        holder._wikipedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.format(
                        Locale.getDefault(),
                        "https://de.wikipedia.org/wiki/%s",
                        _movieList.getValue(index).GetTitle()).replace(" ", "_");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                _context.startActivity(intent);
            }
        });

        holder._receiverController = new ReceiverController(_context);
        holder._youtubeInformationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _logger.Debug("_currentYoutubeVideoReceiver");
                YoutubeVideoDto currentYoutubeId = (YoutubeVideoDto) intent.getSerializableExtra(Bundles.YOUTUBE_VIDEO);
                if (currentYoutubeId != null) {
                    Picasso.with(context).load(currentYoutubeId.GetMediumImageUrl()).into(holder._image);
                } else {
                    _logger.Warn("currentYoutubeId is null");
                }
            }
        };

        if (!_networkController.IsWifiConnected()) {
            _logger.Warn("We are not in a wifi area! Not downloading images!");
            return rowView;
        }

        String broadcast = String.format(
                Locale.getDefault(),
                "%s_%s",
                Broadcasts.YOUTUBE_VIDEO, _movieList.getValue(index).GetTitle());
        _logger.Debug(String.format(Locale.getDefault(), "Alternative broadcast is %s", broadcast));

        holder._receiverController.RegisterReceiver(
                holder._youtubeInformationReceiver,
                new String[]{broadcast});

        String url = String.format(Locale.getDefault(), Constants.YOUTUBE_SEARCH, 1, _movieList.getValue(index).GetTitle() + "+movie", Keys.YOUTUBE_API_KEY);
        DownloadYoutubeVideoTask task = new DownloadYoutubeVideoTask(
                _context,
                new BroadcastController(_context));
        task.SetSendFirstEntry(true);
        task.SetAlternativeBroadcast(broadcast);
        task.execute(url);

        return rowView;
    }
}