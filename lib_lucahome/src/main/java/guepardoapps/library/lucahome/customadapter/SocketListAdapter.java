package guepardoapps.library.lucahome.customadapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

import de.mateware.snacky.Snacky;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.controller.SocketController;

import guepardoapps.library.lucahome.services.helper.MessageSendHelper;
import guepardoapps.library.toolset.common.classes.SerializableList;

public class SocketListAdapter extends BaseAdapter {

    private static final String TAG = SocketListAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private SerializableList<WirelessSocketDto> _socketList;
    private boolean _isOnWear;

    private Context _context;
    private MessageSendHelper _messageSendHelper;

    private LucaDialogController _dialogController;
    private SocketController _socketController;

    private static LayoutInflater _inflater = null;

    public SocketListAdapter(@NonNull Context context,
                             @NonNull SerializableList<WirelessSocketDto> socketList,
                             boolean isOnWear,
                             Class<?> phoneMessageService) {
        _logger = new LucaHomeLogger(TAG);

        _socketList = socketList;
        for (int index = 0; index < _socketList.getSize(); index++) {
            _logger.Debug(_socketList.getValue(index).toString());
        }
        _isOnWear = isOnWear;

        _context = context;

        _dialogController = new LucaDialogController(_context);
        _socketController = new SocketController(_context);

        if (phoneMessageService != null && _isOnWear) {
            _messageSendHelper = new MessageSendHelper(context, phoneMessageService);
        }

        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _socketList.getSize();
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
        private TextView _code;
        private TextView _area;
        private Switch _state;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_socket_item, null);

        holder._name = (Button) rowView.findViewById(R.id.socket_item_name);
        holder._name.setText(_socketList.getValue(index).GetName());
        holder._name.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                _logger.Debug("onLongClick _name button: " + _socketList.getValue(index).GetName());
                if (!_isOnWear) {
                    _dialogController.ShowUpdateSocketDialog(_socketList.getValue(index));
                } else {
                    _logger.Warn("Not supported on wear!");
                }
                return true;
            }
        });

        holder._code = (TextView) rowView.findViewById(R.id.socket_item_code);
        holder._code.setText(_socketList.getValue(index).GetCode());

        holder._area = (TextView) rowView.findViewById(R.id.socket_item_area);
        holder._area.setText(_socketList.getValue(index).GetArea());

        holder._state = (Switch) rowView.findViewById(R.id.socket_item_switch);
        holder._state.setChecked(_socketList.getValue(index).GetIsActivated());
        holder._state.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _logger.Debug("onCheckedChanged _name button: " + _socketList.getValue(index).GetName());

                if (!_isOnWear) {

                    _socketController.SetSocket(_socketList.getValue(index), isChecked);
                    _socketController.CheckMedia(_socketList.getValue(index));

                    Snacky.builder()
                            .setActivty((Activity) _context)
                            .setText(String.format(Locale.GERMAN,
                                    "Trying to set socket %s to %s",
                                    _socketList.getValue(index).GetName(),
                                    (isChecked ? "on" : "off")))
                            .setDuration(Snacky.LENGTH_LONG)
                            .setActionText(_context.getResources().getString(android.R.string.ok))
                            .info()
                            .show();
                } else {
                    if (_messageSendHelper != null) {
                        _messageSendHelper.SendMessage(_socketList.getValue(index).GetCommandWear());
                    } else {
                        _logger.Error("MessageSendHelper is null!");
                    }
                }
            }
        });

        return rowView;
    }
}