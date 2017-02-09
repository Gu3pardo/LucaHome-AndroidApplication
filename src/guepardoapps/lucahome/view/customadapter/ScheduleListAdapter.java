package guepardoapps.lucahome.view.customadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.common.dto.ScheduleDto;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.helper.DialogService;
import guepardoapps.lucahome.view.controller.ScheduleController;

public class ScheduleListAdapter extends BaseAdapter {

	private static final String TAG = ScheduleListAdapter.class.getName();
	private LucaHomeLogger _logger;

	private SerializableList<ScheduleDto> _scheduleList;

	private Context _context;

	private ScheduleController _scheduleController;
	private DialogService _dialogService;

	private static LayoutInflater _inflater = null;

	public ScheduleListAdapter(Context context, SerializableList<ScheduleDto> scheduleList,
			SerializableList<WirelessSocketDto> socketList) {
		_logger = new LucaHomeLogger(TAG);

		_scheduleList = scheduleList;
		for (int index = 0; index < _scheduleList.getSize(); index++) {
			_logger.Debug(_scheduleList.getValue(index).toString());
		}

		_context = context;

		_scheduleController = new ScheduleController(_context);
		_dialogService = new DialogService(_context);
		_dialogService.InitializeSocketList(socketList);

		_inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return _scheduleList.getSize();
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
		private Button _name;
		private TextView _time;
		private TextView _weekday;
		private TextView _socket;
		private TextView _action;
		private TextView _playsound;
		private Button _state;
	}

	@SuppressLint({ "InflateParams", "ViewHolder" })
	@Override
	public View getView(final int index, View convertView, ViewGroup parent) {
		Holder holder = new Holder();
		View rowView = _inflater.inflate(R.layout.list_schedule_item, null);

		holder._name = (Button) rowView.findViewById(R.id.schedule_item_name);
		holder._name.setText(_scheduleList.getValue(index).GetName());
		holder._name.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				_logger.Debug("onLongClick _name button: " + _scheduleList.getValue(index).GetName());
				_dialogService.ShowUpdateScheduleDialog(_scheduleList.getValue(index));
				return true;
			}
		});

		holder._time = (TextView) rowView.findViewById(R.id.schedule_item_time);
		holder._time.setText(_scheduleList.getValue(index).GetTime().toString());

		holder._weekday = (TextView) rowView.findViewById(R.id.schedule_item_weekday);
		holder._weekday.setText(_scheduleList.getValue(index).GetWeekday().toString());

		holder._socket = (TextView) rowView.findViewById(R.id.schedule_item_socket);
		if (_scheduleList.getValue(index).GetSocket() != null) {
			holder._socket.setText(_scheduleList.getValue(index).GetSocket().GetName());
		} else {
			holder._socket.setText("n.a.");
		}

		holder._action = (TextView) rowView.findViewById(R.id.schedule_item_action);
		if (_scheduleList.getValue(index).GetAction()) {
			holder._action.setText(String.valueOf("Activate"));
		} else {
			holder._action.setText(String.valueOf("Deactivate"));
		}

		holder._playsound = (TextView) rowView.findViewById(R.id.schedule_item_playsound);
		if (_scheduleList.getValue(index).GetPlaySound()) {
			holder._playsound.setText(String.valueOf("Sound"));
		} else {
			holder._playsound.setText(String.valueOf("-/-"));
		}

		holder._state = (Button) rowView.findViewById(R.id.schedule_item_state);
		holder._state.setText(_scheduleList.getValue(index).GetIsActiveString());
		holder._state.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_logger.Debug("onClick _state button: " + _scheduleList.getValue(index).GetName());
				_scheduleController.SetSchedule(_scheduleList.getValue(index),
						!_scheduleList.getValue(index).GetIsActive());
			}
		});

		return rowView;
	}
}