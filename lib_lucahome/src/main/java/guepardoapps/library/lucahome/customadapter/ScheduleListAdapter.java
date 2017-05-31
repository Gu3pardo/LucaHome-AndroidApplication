package guepardoapps.library.lucahome.customadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.dto.ScheduleDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.controller.ScheduleController;

import guepardoapps.library.lucahome.services.helper.MessageSendHelper;
import guepardoapps.library.toolset.common.classes.SerializableList;

public class ScheduleListAdapter extends BaseAdapter {

    private static final String TAG = ScheduleListAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private SerializableList<ScheduleDto> _scheduleList;
    private boolean _isOnWear;

    private ScheduleController _scheduleController;
    private LucaDialogController _dialogController;
    private MessageSendHelper _messageSendHelper;

    private static LayoutInflater _inflater = null;

    public ScheduleListAdapter(
            @NonNull Context context,
            @NonNull SerializableList<ScheduleDto> scheduleList,
            SerializableList<WirelessSocketDto> socketList,
            boolean isOnWear,
            Class<?> phoneMessageService) {
        _logger = new LucaHomeLogger(TAG);

        _scheduleList = scheduleList;
        for (int index = 0; index < _scheduleList.getSize(); index++) {
            _logger.Debug(_scheduleList.getValue(index).toString());
        }
        _isOnWear = isOnWear;

        _scheduleController = new ScheduleController(context);
        _dialogController = new LucaDialogController(context);
        if (socketList != null) {
            _dialogController.InitializeSocketList(socketList);
        }
        if (phoneMessageService != null && _isOnWear) {
            _messageSendHelper = new MessageSendHelper(context, phoneMessageService);
        }

        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    private class Holder {
        private Button _name;
        private TextView _time;
        private TextView _weekday;
        private TextView _socket;
        private TextView _action;
        private TextView _playSound;
        private Button _state;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_schedule_item, null);

        holder._name = (Button) rowView.findViewById(R.id.schedule_item_name);
        holder._name.setText(_scheduleList.getValue(index).GetName());
        holder._name.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                _logger.Debug("onLongClick _name button: " + _scheduleList.getValue(index).GetName());
                if (!_isOnWear) {
                    _dialogController.ShowUpdateScheduleDialog(_scheduleList.getValue(index));
                } else {
                    _logger.Warn("Not supported on wear!");
                }
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

        holder._playSound = (TextView) rowView.findViewById(R.id.schedule_item_playsound);
        if (_scheduleList.getValue(index).GetPlaySound()) {
            holder._playSound.setText(String.valueOf("Sound"));
        } else {
            holder._playSound.setText(String.valueOf("-/-"));
        }

        holder._state = (Button) rowView.findViewById(R.id.schedule_item_state);
        holder._state.setText(_scheduleList.getValue(index).GetIsActiveString());
        holder._state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("onClick _state button: " + _scheduleList.getValue(index).GetName());
                if (!_isOnWear) {
                    _scheduleController.SetSchedule(_scheduleList.getValue(index),
                            !_scheduleList.getValue(index).IsActive());
                } else {
                    if (_messageSendHelper != null) {
                        _messageSendHelper.SendMessage(_scheduleList.getValue(index).GetCommandWear());
                    } else {
                        _logger.Error("MessageSendHelper is null!");
                    }
                }
            }
        });

        return rowView;
    }
}