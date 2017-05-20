package guepardoapps.library.lucahome.customadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.dto.TimerDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.controller.TimerController;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class TimerListAdapter extends BaseAdapter {

    private static final String TAG = TimerListAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private SerializableList<TimerDto> _timerList;
    private boolean _isOnWear;

    private LucaDialogController _dialogController;
    private TimerController _timerController;

    private static LayoutInflater _inflater = null;

    public TimerListAdapter(@NonNull Context context,
                            @NonNull SerializableList<TimerDto> timerList,
                            SerializableList<WirelessSocketDto> socketList,
                            boolean isOnWear) {
        _logger = new LucaHomeLogger(TAG);

        _timerList = timerList;
        for (int index = 0; index < _timerList.getSize(); index++) {
            _logger.Debug(_timerList.getValue(index).toString());
        }
        _isOnWear = isOnWear;

        _timerController = new TimerController(context);
        if (!_isOnWear) {
            _dialogController = new LucaDialogController(context);
            if (socketList != null) {
                _dialogController.InitializeSocketList(socketList);
            } else {
                _logger.Error("SocketList is null!");
            }
        }

        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    private class Holder {
        private ImageView _image;
        private Button _name;
        private TextView _time;
        private TextView _weekday;
        private TextView _socket;
        private TextView _action;
        private TextView _playSound;
        private Button _delete;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
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
                if (!_isOnWear) {
                    _dialogController.ShowUpdateTimerDialog(_timerList.getValue(index));
                } else {
                    _logger.Warn("Not supported on wear!");
                }
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

        holder._playSound = (TextView) rowView.findViewById(R.id.timer_item_playsound);
        if (_timerList.getValue(index).GetPlaySound()) {
            holder._playSound.setText(String.valueOf("Sound"));
        } else {
            holder._playSound.setText(String.valueOf("-/-"));
        }

        holder._delete = (Button) rowView.findViewById(R.id.timer_item_delete);
        holder._delete.setText(_timerList.getValue(index).GetIsActiveString());
        holder._delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                _logger.Debug("onClick _delete button: " + _timerList.getValue(index).GetName());
                if (!_isOnWear) {
                    _timerController.Delete(_timerList.getValue(index));
                } else {
                    _logger.Warn("Not supported on wear!");
                }
            }
        });

        return rowView;
    }
}