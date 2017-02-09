package guepardoapps.lucahome.view.customadapter;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.common.dto.sensor.TemperatureDto;
import guepardoapps.lucahome.common.enums.TemperatureType;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.helper.DialogService;
import guepardoapps.lucahome.view.controller.TemperatureController;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class TemperatureListAdapter extends BaseAdapter {

	private static final String TAG = TemperatureListAdapter.class.getName();
	private LucaHomeLogger _logger;

	private SerializableList<TemperatureDto> _temperatureList;

	private Context _context;
	private DialogService _dialogService;

	private TemperatureController _temperatureController;

	private static LayoutInflater _inflater = null;

	public TemperatureListAdapter(Context context, SerializableList<TemperatureDto> temperatureList) {
		_logger = new LucaHomeLogger(TAG);

		_temperatureList = temperatureList;
		for (int index = 0; index < _temperatureList.getSize(); index++) {
			_logger.Debug(_temperatureList.getValue(index).toString());
		}

		_context = context;
		_dialogService = new DialogService(_context);

		_temperatureController = new TemperatureController(_context);

		_inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return _temperatureList.getSize();
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
		private TextView _temperatureType;
		private TextView _sensorPath;
		private ImageButton _reloadTemperature;
		private ImageButton _graphTemperature;
	}

	@SuppressLint({ "InflateParams", "ViewHolder" })
	@Override
	public View getView(final int index, View convertView, ViewGroup parent) {
		Holder holder = new Holder();
		View rowView = _inflater.inflate(R.layout.list_temperature_item, null);

		holder._value = (TextView) rowView.findViewById(R.id.temperature_item_value);
		holder._value.setText(_temperatureList.getValue(index).GetTemperatureString());

		holder._area = (TextView) rowView.findViewById(R.id.temperature_item_area);
		holder._area.setText(_temperatureList.getValue(index).GetArea());

		holder._lastUpdate = (TextView) rowView.findViewById(R.id.temperature_item_lastUpdate);
		holder._lastUpdate.setText(_temperatureList.getValue(index).GetLastUpdate().toString());

		holder._temperatureType = (TextView) rowView.findViewById(R.id.temperature_item_temperatureType);
		holder._temperatureType.setText(_temperatureList.getValue(index).GetTemperatureType().toString());

		holder._sensorPath = (TextView) rowView.findViewById(R.id.temperature_item_sensorPath);
		holder._sensorPath.setText(_temperatureList.getValue(index).GetSensorPath());

		holder._reloadTemperature = (ImageButton) rowView.findViewById(R.id.temperature_item_reload);
		holder._reloadTemperature.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				_logger.Debug("onClick _reloadTemperature: " + _temperatureList.toString());
				_temperatureController.ReloadTemperature(_temperatureList.getValue(index));
			}
		});
		if (_temperatureList.getValue(index).GetTemperatureType() == TemperatureType.SMARTPHONE_SENSOR) {
			holder._reloadTemperature.setVisibility(View.INVISIBLE);
			holder._reloadTemperature.setClickable(false);
		}

		holder._graphTemperature = (ImageButton) rowView.findViewById(R.id.temperature_item_graph);
		holder._graphTemperature.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				_logger.Debug("onClick _graphTemperature: " + _temperatureList.toString());
				_dialogService.ShowTemperatureGraphDialog(_temperatureList.getValue(index).GetGraphPath());
			}
		});
		if (_temperatureList.getValue(index).GetTemperatureType() == TemperatureType.SMARTPHONE_SENSOR
				|| _temperatureList.getValue(index).GetTemperatureType() == TemperatureType.CITY) {
			holder._graphTemperature.setVisibility(View.INVISIBLE);
			holder._graphTemperature.setClickable(false);
		}

		return rowView;
	}
}