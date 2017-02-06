package guepardoapps.lucahome.customadapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.mediamirror.common.dto.YoutubeVideoDto;
import guepardoapps.toolset.controller.BroadcastController;

public class YoutubeVideoListAdapter extends BaseAdapter {

	private static final String TAG = YoutubeVideoListAdapter.class.getName();
	private LucaHomeLogger _logger;

	private ArrayList<YoutubeVideoDto> _youtubeVideoList;

	private Context _context;
	private BroadcastController _broadcastController;

	private static LayoutInflater _inflater = null;

	public YoutubeVideoListAdapter(Context context, ArrayList<YoutubeVideoDto> youtubeVideoList) {
		_logger = new LucaHomeLogger(TAG);

		_youtubeVideoList = youtubeVideoList;
		for (YoutubeVideoDto entry : _youtubeVideoList) {
			_logger.Debug(entry.toString());
		}

		_context = context;
		_broadcastController = new BroadcastController(_context);

		_inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return _youtubeVideoList.size();
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
		private TextView _title;
		private TextView _description;
		private TextView _id;
		private RelativeLayout _background;
	}

	@SuppressLint({ "InflateParams", "ViewHolder" })
	@Override
	public View getView(final int index, View convertView, ViewGroup parent) {
		Holder holder = new Holder();
		View rowView = _inflater.inflate(R.layout.list_youtube_video_item, null);

		holder._title = (TextView) rowView.findViewById(R.id.youtube_video_title);
		holder._title.setText(_youtubeVideoList.get(index).GetTitle());
		holder._title.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				_broadcastController.SendStringBroadcast(Constants.BROADCAST_YOUTUBE_ID, Constants.BUNDLE_YOUTUBE_ID,
						_youtubeVideoList.get(index).GetYoutubeId());
			}
		});

		holder._description = (TextView) rowView.findViewById(R.id.youtube_video_description);
		holder._description.setText(_youtubeVideoList.get(index).GetDescription());
		holder._description.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				_broadcastController.SendStringBroadcast(Constants.BROADCAST_YOUTUBE_ID, Constants.BUNDLE_YOUTUBE_ID,
						_youtubeVideoList.get(index).GetYoutubeId());
			}
		});

		holder._id = (TextView) rowView.findViewById(R.id.youtube_video_id);
		holder._id.setText(_youtubeVideoList.get(index).GetYoutubeId());
		holder._id.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				_broadcastController.SendStringBroadcast(Constants.BROADCAST_YOUTUBE_ID, Constants.BUNDLE_YOUTUBE_ID,
						_youtubeVideoList.get(index).GetYoutubeId());
			}
		});

		holder._background = (RelativeLayout) rowView.findViewById(R.id.youtube_video_background);
		holder._background.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				_broadcastController.SendStringBroadcast(Constants.BROADCAST_YOUTUBE_ID, Constants.BUNDLE_YOUTUBE_ID,
						_youtubeVideoList.get(index).GetYoutubeId());
			}
		});

		return rowView;
	}
}