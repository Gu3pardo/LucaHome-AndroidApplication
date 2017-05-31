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
import guepardoapps.library.lucahome.common.dto.AirPressureDto;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class AirPressureListAdapter extends BaseAdapter {

    private SerializableList<AirPressureDto> _airPressureList;
    private static LayoutInflater _inflater = null;

    public AirPressureListAdapter(
            @NonNull Context context,
            @NonNull SerializableList<AirPressureDto> airPressureList) {
        _airPressureList = airPressureList;
        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _airPressureList.getSize();
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
        View rowView = _inflater.inflate(R.layout.list_airpressure_item, null);

        holder._value = (TextView) rowView.findViewById(R.id.air_pressure_item_value);
        holder._value.setText(_airPressureList.getValue(index).GetAirPressureString());

        holder._area = (TextView) rowView.findViewById(R.id.air_pressure_item_area);
        holder._area.setText(_airPressureList.getValue(index).GetArea());

        holder._lastUpdate = (TextView) rowView.findViewById(R.id.air_pressure_item_lastUpdate);
        holder._lastUpdate.setText(_airPressureList.getValue(index).GetLastUpdate().toString());

        return rowView;
    }
}