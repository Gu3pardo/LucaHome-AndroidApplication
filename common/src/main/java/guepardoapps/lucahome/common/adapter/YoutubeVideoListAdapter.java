package guepardoapps.lucahome.common.adapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.YoutubeVideo;
import guepardoapps.lucahome.common.enums.MediaServerAction;
import guepardoapps.lucahome.common.enums.MediaServerSelection;
import guepardoapps.lucahome.common.service.MediaServerService;

public class YoutubeVideoListAdapter extends BaseAdapter {
    private class Holder {
        private ImageView _image;
        private TextView _id;
        private TextView _title;
        private TextView _description;
    }

    private ArrayList<YoutubeVideo> _youtubeVideoList;

    private String _serverIp;
    private boolean _playOnAllMirror = false;
    private Runnable _closeDialogRunnable;

    private Context _context;
    private static LayoutInflater _inflater = null;

    public YoutubeVideoListAdapter(
            @NonNull Context context,
            @NonNull ArrayList<YoutubeVideo> youtubeVideoList,
            @NonNull String serverIp,
            @NonNull Runnable closeDialogRunnable) {
        _context = context;
        _youtubeVideoList = youtubeVideoList;
        _serverIp = serverIp;
        _closeDialogRunnable = closeDialogRunnable;

        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void SetPlayOnAllMirror(boolean playOnAllMirror) {
        _playOnAllMirror = playOnAllMirror;
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

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_youtube_video_item, null);

        final YoutubeVideo entry = _youtubeVideoList.get(index);

        View.OnClickListener sendYoutubeVideoOnClickListener = view -> {
            String youtubeId = entry.GetYoutubeId();
            if (_playOnAllMirror) {
                for (MediaServerSelection entry1 : MediaServerSelection.values()) {
                    if (entry1.GetId() > 0) {
                        MediaServerService.getInstance().SendCommand(entry1.GetIp(), MediaServerAction.PLAY_YOUTUBE_VIDEO.toString(), youtubeId);
                    }
                }
            } else {
                MediaServerService mediaMirrorService = MediaServerService.getInstance();
                mediaMirrorService.Initialize(_context, false, -1);
                mediaMirrorService.SendCommand(_serverIp, MediaServerAction.PLAY_YOUTUBE_VIDEO.toString(), youtubeId);
                mediaMirrorService.Dispose();
            }

            _closeDialogRunnable.run();
        };

        holder._image = rowView.findViewById(R.id.youtube_video_image);
        holder._id = rowView.findViewById(R.id.youtube_video_id);
        holder._title = rowView.findViewById(R.id.youtube_video_title);
        holder._description = rowView.findViewById(R.id.youtube_video_description);

        Picasso.with(_context).load(entry.GetMediumImageUrl()).into(holder._image);

        holder._id.setText(entry.GetYoutubeId());
        holder._title.setText(entry.GetTitle());
        holder._description.setText(entry.GetDescription());

        holder._image.setOnClickListener(sendYoutubeVideoOnClickListener);
        holder._id.setOnClickListener(sendYoutubeVideoOnClickListener);
        holder._title.setOnClickListener(sendYoutubeVideoOnClickListener);
        holder._description.setOnClickListener(sendYoutubeVideoOnClickListener);

        return rowView;
    }
}