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
import guepardoapps.lucahome.common.classes.mediaserver.YoutubeVideoData;
import guepardoapps.lucahome.common.enums.MediaServerAction;
import guepardoapps.lucahome.common.service.MediaServerService;

public class YoutubeVideoListAdapter extends BaseAdapter {
    private class Holder {
        private ImageView _image;
        private TextView _id;
        private TextView _title;
        private TextView _description;
    }

    private Context _context;
    private ArrayList<YoutubeVideoData> _youtubeVideoDataList;
    private Runnable _closeDialogRunnable;
    private static LayoutInflater _inflater = null;

    public YoutubeVideoListAdapter(
            @NonNull Context context,
            @NonNull ArrayList<YoutubeVideoData> youtubeVideoDataList,
            @NonNull Runnable closeDialogRunnable) {
        _context = context;
        _youtubeVideoDataList = youtubeVideoDataList;
        _closeDialogRunnable = closeDialogRunnable;
        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _youtubeVideoDataList.size();
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

        final YoutubeVideoData entry = _youtubeVideoDataList.get(index);

        View.OnClickListener sendYoutubeVideoOnClickListener = view -> {
            String youtubeId = entry.GetYoutubeId();
            MediaServerService.getInstance().SendCommand(MediaServerAction.YOUTUBE_PLAY.toString(), youtubeId);
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