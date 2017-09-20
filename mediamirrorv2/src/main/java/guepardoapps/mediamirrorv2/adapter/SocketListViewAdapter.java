package guepardoapps.mediamirrorv2.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.mediamirrorv2.R;

public class SocketListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _titleText;
        private TextView _areaText;
        private TextView _codeText;
        private ImageView _cardImage;
        private Switch _cardSwitch;
        private View _stateView;
    }

    private WirelessSocketService _wirelessSocketService;

    private SerializableList<WirelessSocket> _listViewItems;

    private Dialog _dialog;
    private static LayoutInflater _inflater = null;

    public SocketListViewAdapter(@NonNull Context context, @NonNull SerializableList<WirelessSocket> listViewItems, @NonNull Dialog dialog) {
        _wirelessSocketService = WirelessSocketService.getInstance();

        _listViewItems = listViewItems;

        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _dialog = dialog;
    }

    @Override
    public int getCount() {
        return _listViewItems.getSize();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        final Holder holder = new Holder();

        View rowView = _inflater.inflate(R.layout.listview_card_socket, null);

        holder._titleText = rowView.findViewById(R.id.socketCardTitleText);
        holder._areaText = rowView.findViewById(R.id.socketCardAreaText);
        holder._codeText = rowView.findViewById(R.id.socketCardCodeText);
        holder._cardImage = rowView.findViewById(R.id.socketCardImage);
        holder._cardSwitch = rowView.findViewById(R.id.socketCardSwitch);
        holder._stateView = rowView.findViewById(R.id.socketCardStateView);

        final WirelessSocket wirelessSocket = _listViewItems.getValue(index);

        holder._titleText.setText(wirelessSocket.GetName());
        holder._areaText.setText(wirelessSocket.GetArea());
        holder._codeText.setText(wirelessSocket.GetCode());

        holder._cardImage.setImageResource(wirelessSocket.GetWallpaper());

        holder._stateView.setBackgroundResource(wirelessSocket.IsActivated() ? R.drawable.circle_green : R.drawable.circle_red);

        holder._cardSwitch.setChecked(wirelessSocket.IsActivated());
        holder._cardSwitch.setOnCheckedChangeListener((compoundButton, value) -> {
            _wirelessSocketService.SetWirelessSocketState(wirelessSocket, value);
            _dialog.dismiss();
        });

        return rowView;
    }
}
