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
import android.widget.ImageView;
import android.widget.TextView;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.common.dto.TimerDto;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.helper.DialogService;
import guepardoapps.lucahome.view.controller.TimerController;

public class TimerListAdapter extends BaseAdapter {

	private static final String TAG = TimerListAdapter.class.getName();
	private LucaHomeLogger _logger;

	private SerializableList<TimerDto> _timerList;

	private Context _context;

	private TimerController _timerController;
	private DialogService _dialogService;

	private static LayoutInflater _inflater = null;

	public TimerListAdapter(Context context, SerializableList<TimerDto> timerList,
			SerializableList<WirelessSocketDto> socketList) {
		_logger = new LucaHomeLogger(TAG);

		_timerList = timerList;
		for (int index = 0; index < _timerList.getSize(); index++) {
			_logger.Debug(_timerList.getValue(index).toString());
		}

		_context = context;

		_timerController = new TimerController(_context);
		_dialogService = new DialogService(_context);
		_dialogService.InitializeSocketList(socketList);

		_inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return _timerList.getSize();
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
		private ImageView _image;
		private Button _name;
		private TextView _time;
		private TextView _weekday;
		private TextView _socket;
		private TextView _action;
		private TextView _playsound;
		private Button _delete;
	}

	@SuppressLint({ "InflateParams", "ViewHolder" })
	@Override
	public View getView(final int index, View convertView, ViewGroup parent) {
		Holder holder = new Holder();
		View rowView = _inflater.inflate(R.layout.list_timer_item, null);

		holder._image = (ImageView) rowView.findViewById(R.id.timer_item_image);
		holder._image.setImageResource(R.drawable.timer);

		holder._name = (Button) rowView.findViewById(R.id.timer_item_name);
		holder._name.setText(_timerList.getValue(index).GetName());
		holder._name.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				_logger.Debug("onLongClick _name button: " + _timerList.getValue(index).GetName());
				_dialogService.ShowUpdateTimerDialog(_timerList.getValue(index));
				return true;
			}
		});

		holder._time = (TextView) rowView.findViewById(R.id.timer_item_time);
		holder._time.setText(_timerList.getValue(index).GetTime().toString());

		holder._weekday = (TextView) rowView.findViewById(R.id.timer_item_weekday);
		holder._weekday.setText(_timerList.getValue(index).GetWeekday().toString());

		holder._socket = (TextView) rowView.findViewById(R.id.timer_item_socket);
		holder._socket.setText(_timerList.getValue(index).GetSocket().GetName());

		holder._action = (TextView) rowView.findViewById(R.id.timer_item_action);
		if (_timerList.getValue(index).GetAction()) {
			holder._action.setText(String.valueOf("Activate"));
		} else {
			holder._action.setText(String.valueOf("Deactivate"));
		}

		holder._playsound = (TextView) rowView.findViewById(R.id.timer_item_playsound);
		if (_timerList.getValue(index).GetPlaySound()) {
			holder._playsound.setText(String.valueOf("Sound"));
		} else {
			holder._playsound.setText(String.valueOf("-/-"));
		}

		holder._delete = (Button) rowView.findViewById(R.id.timer_item_delete);
		holder._delete.setText(_timerList.getValue(index).GetIsActiveString());
		holder._delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_logger.Debug("onClick _delete button: " + _timerList.getValue(index).GetName());
				_timerController.Delete(_timerList.getValue(index));
			}
		});

		return rowView;
	}
}