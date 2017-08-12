package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.MagazinDir;

public class LibraryListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _titleText;
        private ImageView _iconView;
        private ListView _contentListView;
    }

    private static final String TAG = LibraryListViewAdapter.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    private static LayoutInflater _inflater = null;

    private SerializableList<MagazinDir> _listViewItems;

    public LibraryListViewAdapter(@NonNull Context context, @NonNull SerializableList<MagazinDir> listViewItems) {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");

        _context = context;

        _listViewItems = listViewItems;

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

        View rowView = _inflater.inflate(R.layout.listview_card_library, null);

        holder._titleText = rowView.findViewById(R.id.libraryCardTitleText);
        holder._iconView = rowView.findViewById(R.id.libraryCardImage);
        holder._contentListView = rowView.findViewById(R.id.libraryCardListView);

        final MagazinDir magazinDir = _listViewItems.getValue(index);

        holder._titleText.setText(magazinDir.GetDirName());
        holder._iconView.setImageBitmap(magazinDir.GetIcon());

        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_list_item_1, magazinDir.GetDirContent());
        holder._contentListView.setAdapter(listAdapter);
        holder._contentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                _logger.Debug(String.format(Locale.getDefault(), "Clicked on %s", magazinDir.GetDirContent()[position]));
            }
        });

        return rowView;
    }
}
