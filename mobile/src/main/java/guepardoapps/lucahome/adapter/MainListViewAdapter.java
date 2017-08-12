package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.classes.MainListViewItem;

public class MainListViewAdapter extends BaseAdapter {
    private class Holder {
        private CardView _cardView;
        private TextView _titleText;
        private TextView _descriptionText;
        private ImageView _cardImage;
        private FloatingActionButton _addButton;
    }

    private static final String TAG = MainListViewAdapter.class.getSimpleName();
    private Logger _logger;

    private static LayoutInflater _inflater = null;

    private SerializableList<MainListViewItem> _listViewCardItems;

    public MainListViewAdapter(@NonNull Context context, @NonNull SerializableList<MainListViewItem> listViewCardItems) {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");

        _listViewCardItems = listViewCardItems;

        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        _logger.Debug(String.format(Locale.getDefault(), "getCount: %d", _listViewCardItems.getSize()));
        return _listViewCardItems.getSize();
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

        View rowView = _inflater.inflate(R.layout.listview_card_main, null);

        holder._cardView = rowView.findViewById(R.id.mainCardView);
        holder._titleText = rowView.findViewById(R.id.mainCardTitleText);
        holder._descriptionText = rowView.findViewById(R.id.mainCardDescriptionText);
        holder._cardImage = rowView.findViewById(R.id.mainCardImage);
        holder._addButton = rowView.findViewById(R.id.mainCardAddButton);

        final MainListViewItem mainListViewItem = _listViewCardItems.getValue(index);

        holder._titleText.setText(mainListViewItem.GetTitle());
        holder._descriptionText.setText(mainListViewItem.GetDescription());
        holder._cardImage.setImageResource(mainListViewItem.GetImageResource());

        holder._cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_cardView onClick");
                mainListViewItem.GetMainTouchRunnable().run();
            }
        });
        holder._titleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_titleText onClick");
                mainListViewItem.GetMainTouchRunnable().run();
            }
        });
        holder._descriptionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_descriptionText onClick");
                mainListViewItem.GetMainTouchRunnable().run();
            }
        });
        holder._cardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_cardImage onClick");
                mainListViewItem.GetMainTouchRunnable().run();
            }
        });

        holder._addButton.setVisibility((mainListViewItem.IsAddVisibility() ? View.VISIBLE : View.INVISIBLE));
        if (mainListViewItem.IsAddVisibility()) {
            holder._addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    _logger.Debug("_addButton onClick");
                    mainListViewItem.GetAddTouchRunnable().run();
                }
            });
        }

        return rowView;
    }
}
