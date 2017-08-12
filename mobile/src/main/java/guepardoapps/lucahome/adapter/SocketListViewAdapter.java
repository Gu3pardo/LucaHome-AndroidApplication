package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.WirelessSocketEditActivity;

public class SocketListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _titleText;
        private TextView _areaText;
        private TextView _codeText;
        private ImageView _cardImage;
        private Switch _cardSwitch;
        private FloatingActionButton _updateButton;
        private FloatingActionButton _deleteButton;
        private View _stateView;
    }

    private static final String TAG = SocketListViewAdapter.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    private NavigationService _navigationService;
    private WirelessSocketService _wirelessSocketService;

    private static LayoutInflater _inflater = null;

    private SerializableList<WirelessSocket> _listViewItems;

    public SocketListViewAdapter(@NonNull Context context, @NonNull SerializableList<WirelessSocket> listViewItems) {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");

        _context = context;

        _listViewItems = listViewItems;

        _navigationService = NavigationService.getInstance();
        _wirelessSocketService = WirelessSocketService.getInstance();

        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        _logger.Debug(String.format(Locale.getDefault(), "getCount: %d", _listViewItems.getSize()));
        return _listViewItems.getSize();
    }

    @Override
    public Object getItem(int position) {
        _logger.Debug(String.format(Locale.getDefault(), "getItem: %d", position));
        return position;
    }

    @Override
    public long getItemId(int position) {
        _logger.Debug(String.format(Locale.getDefault(), "getItemId: %d", position));
        return position;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();

        View rowView = _inflater.inflate(R.layout.listview_card_socket, null);

        holder._titleText = rowView.findViewById(R.id.socketCardTitleText);
        holder._areaText = rowView.findViewById(R.id.socketCardAreaText);
        holder._codeText = rowView.findViewById(R.id.socketCardCodeText);
        holder._cardImage = rowView.findViewById(R.id.socketCardImage);
        holder._cardSwitch = rowView.findViewById(R.id.socketCardSwitch);
        holder._updateButton = rowView.findViewById(R.id.socket_card_update_button);
        holder._deleteButton = rowView.findViewById(R.id.socket_card_delete_button);
        holder._stateView = rowView.findViewById(R.id.socketCardStateView);

        final WirelessSocket wirelessSocket = _listViewItems.getValue(index);

        holder._titleText.setText(wirelessSocket.GetName());
        holder._areaText.setText(wirelessSocket.GetArea());
        holder._codeText.setText(wirelessSocket.GetCode());

        holder._cardImage.setImageResource(wirelessSocket.GetWallpaper());

        holder._stateView.setBackgroundResource(wirelessSocket.IsActivated() ? R.drawable.circle_green : R.drawable.circle_red);

        holder._cardSwitch.setChecked(wirelessSocket.IsActivated());
        holder._cardSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                _logger.Debug(String.format(
                        Locale.getDefault(),
                        "setOnCheckedChangeListener onCheckedChanged on CompoundButton %s with value %s",
                        compoundButton, value));
                _wirelessSocketService.SetWirelessSocketState(wirelessSocket, value);
            }
        });

        holder._updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_updateButton setOnClickListener onClick");
                Bundle data = new Bundle();
                data.putSerializable(WirelessSocketService.WirelessSocketIntent, new WirelessSocketDto(wirelessSocket.GetId(), wirelessSocket.GetName(), wirelessSocket.GetArea(), wirelessSocket.GetCode(), wirelessSocket.IsActivated(), WirelessSocketDto.Action.Update));
                _navigationService.NavigateToActivityWithData(_context, WirelessSocketEditActivity.class, data);
            }
        });

        holder._deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_deleteButton setOnClickListener onClick");

                boolean isLightTheme = ThemeManager.getInstance().getCurrentTheme() == 0;

                final Dialog deleteDialog = new Dialog(_context);
                deleteDialog
                        .title(String.format(Locale.getDefault(), "Delete %s?", wirelessSocket.GetName()))
                        .positiveAction("Delete")
                        .negativeAction("Cancel")
                        .applyStyle(isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                        .setCancelable(true);

                deleteDialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        _wirelessSocketService.DeleteWirelessSocket(wirelessSocket);
                        deleteDialog.dismiss();
                    }
                });

                deleteDialog.negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteDialog.dismiss();
                    }
                });

                deleteDialog.show();
            }
        });

        return rowView;
    }
}
