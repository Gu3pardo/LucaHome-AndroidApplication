package guepardoapps.lucahome.view.customadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.common.dto.sensor.HumidityDto;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;

public class HumidityListAdapter extends BaseAdapter {

	private static final String TAG = HumidityListAdapter.class.getName();
	private LucaHomeLogger _logger;

	private SerializableList<HumidityDto> _humidityList;

	private Context _context;

	private static LayoutInflater _inflater = null;

	public HumidityListAdapter(Context context, SerializableList<HumidityDto> humidityList) {
		_logger = new LucaHomeLogger(TAG);

		_humidityList = humidityList;
		for (int index = 0; index < _humidityList.getSize(); index++) {
			_logger.Debug(_humidityList.getValue(index).toString());
		}

		_context = context;

		_inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

	public class Holder {
		private TextView _value;
		private TextView _area;
		private TextView _lastUpdate;
	}

	@SuppressLint({ "InflateParams", "ViewHolder" })
	@Override
	public View getView(final int index, View convertView, ViewGroup parent) {
		Holder holder = new Holder();
		View rowView = _inflater.inflate(R.layout.list_humidity_item, null);

		holder._value = (TextView) rowView.findViewById(R.id.humidity_item_value);
		holder._value.setText(_humidityList.getValue(index).GetHumidityString());

		holder._area = (TextView) rowView.findViewById(R.id.humidity_item_area);
		holder._area.setText(_humidityList.getValue(index).GetArea());

		holder._lastUpdate = (TextView) rowView.findViewById(R.id.humidity_item_lastUpdate);
		holder._lastUpdate.setText(_humidityList.getValue(index).GetLastUpdate().toString());

		return rowView;
	}
}