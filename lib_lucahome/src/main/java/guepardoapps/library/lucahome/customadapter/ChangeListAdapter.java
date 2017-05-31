package guepardoapps.library.lucahome.customadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.dto.ChangeDto;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class ChangeListAdapter extends BaseAdapter {

    private SerializableList<ChangeDto> _changeList;
    private static LayoutInflater _inflater = null;

    public ChangeListAdapter(
            @NonNull Context context,
            @NonNull SerializableList<ChangeDto> changeList) {
        _changeList = changeList;
        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _changeList.getSize();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class Holder {
        private TextView _type;
        private TextView _date;
        private TextView _time;
        private TextView _user;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_change_item, null);

        holder._type = (TextView) rowView.findViewById(R.id.change_item_type);
        holder._type.setText(_changeList.getValue(index).GetType());

        holder._date = (TextView) rowView.findViewById(R.id.change_item_date);
        holder._date.setText(_changeList.getValue(index).GetDate().toString());

        holder._time = (TextView) rowView.findViewById(R.id.change_item_time);
        holder._time.setText(_changeList.getValue(index).GetTime().toString());

        holder._user = (TextView) rowView.findViewById(R.id.change_item_user);
        holder._user.setText(_changeList.getValue(index).GetUser());

        return rowView;
    }
}