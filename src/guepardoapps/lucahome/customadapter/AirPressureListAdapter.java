package guepardoapps.lucahome.customadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.dto.sensor.AirPressureDto;

public class AirPressureListAdapter extends BaseAdapter {

	private static final String TAG = AirPressureListAdapter.class.getName();
	private LucaHomeLogger _logger;

	private SerializableList<AirPressureDto> _airPressureList;

	private Context _context;

	private static LayoutInflater _inflater = null;

	public AirPressureListAdapter(Context context, SerializableList<AirPressureDto> airPressureList) {
		_logger = new LucaHomeLogger(TAG);

		_airPressureList = airPressureList;
		for (int index = 0; index < _airPressureList.getSize(); index++) {
			_logger.Debug(_airPressureList.getValue(index).toString());
		}

		_context = context;

		_inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

	public class Holder {
		private TextView _value;
		private TextView _area;
		private TextView _lastUpdate;
	}

	@SuppressLint({ "InflateParams", "ViewHolder" })
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