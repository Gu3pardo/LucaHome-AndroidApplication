package guepardoapps.lucahome.customadapter;

import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.controller.NavigationController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.dto.MainListViewItemDto;
import guepardoapps.lucahome.enums.TargetActivity;

public class MainListViewAdapter extends BaseAdapter {

    private static final String TAG = MainListViewAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private List<MainListViewItemDto> _list;
    private Context _context;
    private NavigationController _navigationController;
    private static LayoutInflater _inflater = null;

    public MainListViewAdapter(
            @NonNull Context context,
            @NonNull List<MainListViewItemDto> _viewItemList) {
        _logger = new LucaHomeLogger(TAG);
        _logger.Debug(String.format(Locale.getDefault(), "%s created...", TAG));

        _list = _viewItemList;
        _context = context;
        _navigationController = new NavigationController(_context);
        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _list.size();
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
        private TextView _item;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_item_main, null);

        Drawable drawable = _context.getResources().getDrawable(_list.get(index).GetImageResource());
        drawable.setBounds(0, 0, 20, 20);

        holder._item = (TextView) rowView.findViewById(R.id.list_main_item_text);
        holder._item.setText(_list.get(index).GetText());
        holder._item.setCompoundDrawablesRelative(drawable, null, null, null);
        holder._item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TargetActivity targetActivity = TargetActivity.GetByString(_list.get(index).GetText());
                if (targetActivity != null) {
                    _navigationController.NavigateTo(targetActivity.GetActivity(), false);
                } else {
                    _logger.Error("TargetActivity is null!");
                    Toasty.error(_context, "TargetActivity is null!", Toast.LENGTH_LONG).show();
                }
            }
        });

        return rowView;
    }
}