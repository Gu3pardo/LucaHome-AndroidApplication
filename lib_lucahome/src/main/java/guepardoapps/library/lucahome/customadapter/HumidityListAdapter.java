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
import guepardoapps.library.lucahome.common.dto.HumidityDto;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class HumidityListAdapter extends BaseAdapter {

    private SerializableList<HumidityDto> _humidityList;
    private static LayoutInflater _inflater = null;

    public HumidityListAdapter(
            @NonNull Context context,
            @NonNull SerializableList<HumidityDto> humidityList) {
        _humidityList = humidityList;
        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _humidityList.getSize();
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
        private TextView _value;
        private TextView _area;
        private TextView _lastUpdate;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_humidity_item, null);
        HumidityDto entry = _humidityList.getValue(index);

        holder._value = (TextView) rowView.findViewById(R.id.humidity_item_value);
        holder._value.setText(entry.GetHumidityString());

        holder._area = (TextView) rowView.findViewById(R.id.humidity_item_area);
        holder._area.setText(entry.GetArea());

        holder._lastUpdate = (TextView) rowView.findViewById(R.id.humidity_item_lastUpdate);
        holder._lastUpdate.setText(entry.GetLastUpdate().toString());

        return rowView;
    }
}