package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.rey.material.widget.FloatingActionButton;
import com.rey.material.widget.ImageView;
import com.rey.material.widget.TextView;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.classes.MainListViewItem;

public class MainListViewAdapter extends BaseAdapter {
    private class Holder {
        private CardView _cardView;
        private TextView _titleText;
        private TextView _descriptionText;
        private ImageView _cardImage;
        private FloatingActionButton _addButton;
        private View.OnClickListener _mainClickListener;
    }

    private static LayoutInflater _inflater = null;
    private SerializableList<MainListViewItem> _listViewCardItems;

    public MainListViewAdapter(@NonNull Context context, @NonNull SerializableList<MainListViewItem> listViewCardItems) {
        _listViewCardItems = listViewCardItems;
        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _listViewCardItems.getSize();
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

        holder._mainClickListener = view -> mainListViewItem.GetMainTouchRunnable().run();

        holder._cardView.setOnClickListener(holder._mainClickListener);
        holder._titleText.setOnClickListener(holder._mainClickListener);
        holder._descriptionText.setOnClickListener(holder._mainClickListener);
        holder._cardImage.setOnClickListener(holder._mainClickListener);

        holder._addButton.setVisibility((mainListViewItem.IsAddVisibility() ? View.VISIBLE : View.INVISIBLE));
        if (mainListViewItem.IsAddVisibility()) {
            holder._addButton.setOnClickListener(view -> mainListViewItem.GetAddTouchRunnable().run());
        }

        return rowView;
    }
}
