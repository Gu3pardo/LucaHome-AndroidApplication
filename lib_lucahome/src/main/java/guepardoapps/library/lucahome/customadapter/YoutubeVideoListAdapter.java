package guepardoapps.library.lucahome.customadapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.dto.YoutubeVideoDto;
import guepardoapps.library.lucahome.common.enums.MediaMirrorSelection;
import guepardoapps.library.lucahome.common.enums.ServerAction;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.controller.MediaMirrorController;

public class YoutubeVideoListAdapter extends BaseAdapter {

    private ArrayList<YoutubeVideoDto> _youtubeVideoList;

    private LucaDialogController _lucaDialogController;
    private MediaMirrorController _mediaMirrorController;

    private String _ip;
    private boolean _playOnAllMirror = false;

    private Context _context;
    private static LayoutInflater _inflater = null;

    public YoutubeVideoListAdapter(@NonNull Context context,
                                   @NonNull ArrayList<YoutubeVideoDto> youtubeVideoList,
                                   @NonNull LucaDialogController lucaDialogController,
                                   @NonNull MediaMirrorController mediaMirrorController,
                                   @NonNull String ip) {
        _youtubeVideoList = youtubeVideoList;

        _lucaDialogController = lucaDialogController;
        _mediaMirrorController = mediaMirrorController;

        _ip = ip;

        _context = context;
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

    public void SetPlayOnAllMirror(boolean playOnAllMirror) {
        _playOnAllMirror = playOnAllMirror;
    }

    private class Holder {
        private ImageView _image;
        private TextView _id;
        private TextView _title;
        private TextView _description;
        private RelativeLayout _background;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_youtube_video_item, null);

        final YoutubeVideoDto entry = _youtubeVideoList.get(index);

        holder._image = (ImageView) rowView.findViewById(R.id.youtube_video_image);
        holder._image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String youtubeId = entry.GetYoutubeId();
                sendYoutubeId(youtubeId);
            }
        });
        Picasso.with(_context).load(entry.GetMediumImageUrl()).into(holder._image);

        holder._id = (TextView) rowView.findViewById(R.id.youtube_video_id);
        holder._id.setText(entry.GetYoutubeId());
        holder._id.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String youtubeId = entry.GetYoutubeId();
                sendYoutubeId(youtubeId);
            }
        });

        holder._title = (TextView) rowView.findViewById(R.id.youtube_video_title);
        holder._title.setText(entry.GetTitle());
        holder._title.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String youtubeId = entry.GetYoutubeId();
                sendYoutubeId(youtubeId);
            }
        });

        holder._description = (TextView) rowView.findViewById(R.id.youtube_video_description);
        holder._description.setText(entry.GetDescription());
        holder._description.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String youtubeId = entry.GetYoutubeId();
                sendYoutubeId(youtubeId);
            }
        });

        holder._background = (RelativeLayout) rowView.findViewById(R.id.youtube_video_background);
        holder._background.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String youtubeId = entry.GetYoutubeId();
                sendYoutubeId(youtubeId);
            }
        });

        return rowView;
    }

    private void sendYoutubeId(String youtubeId) {
        if (_playOnAllMirror) {
            for (MediaMirrorSelection entry : MediaMirrorSelection.values()) {
                if (entry.GetId() > 0) {
                    _mediaMirrorController.SendCommand(entry.GetIp(), ServerAction.PLAY_YOUTUBE_VIDEO.toString(),
                            youtubeId);
                }
            }
        } else {
            _mediaMirrorController.SendCommand(_ip, ServerAction.PLAY_YOUTUBE_VIDEO.toString(), youtubeId);
        }
        _lucaDialogController.CloseDialogCallback.run();
    }
}