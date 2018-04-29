package guepardoapps.lucahome.mediaserver.adapter;

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

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.services.RoomService;
import guepardoapps.lucahome.common.services.WirelessSocketService;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.mediaserver.R;

@SuppressWarnings({"unused"})
public class WirelessSocketListViewAdapter extends BaseAdapter {
    private static String Tag = WirelessSocketListViewAdapter.class.getSimpleName();

    private class Holder {
        private TextView _titleText;
        private TextView _areaText;
        private TextView _codeText;
        private ImageView _cardImage;
        private Switch _cardSwitch;
        private View _stateView;
    }

    private ArrayList<WirelessSocket> _listViewItems;
    private Dialog _dialog;
    private static LayoutInflater _inflater = null;

    public WirelessSocketListViewAdapter(@NonNull Context context, @NonNull ArrayList<WirelessSocket> listViewItems, @NonNull Dialog dialog) {
        _listViewItems = listViewItems;
        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _dialog = dialog;
    }

    @Override
    public int getCount() {
        return _listViewItems.size();
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

        View rowView = _inflater.inflate(R.layout.listview_card_wireless_socket, null);

        holder._titleText = rowView.findViewById(R.id.wirelessSocketCardTitleText);
        holder._areaText = rowView.findViewById(R.id.wirelessSocketCardAreaText);
        holder._codeText = rowView.findViewById(R.id.wirelessSocketCardCodeText);
        holder._cardImage = rowView.findViewById(R.id.wirelessSocketCardImage);
        holder._cardSwitch = rowView.findViewById(R.id.wirelessSocketCardSwitch);
        holder._stateView = rowView.findViewById(R.id.wirelessSocketCardStateView);

        final WirelessSocket wirelessSocket = _listViewItems.get(index);

        holder._titleText.setText(wirelessSocket.GetName());
        holder._codeText.setText(wirelessSocket.GetCode());

        try {
            String locationName = RoomService.getInstance().GetByUuid(wirelessSocket.GetRoomUuid()).GetName();
            holder._areaText.setText(locationName);
            holder._cardImage.setImageResource(wirelessSocket.GetDrawable());
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
        }

        holder._stateView.setBackgroundResource(wirelessSocket.GetState() ? R.drawable.xml_circle_green : R.drawable.xml_circle_red);

        holder._cardSwitch.setChecked(wirelessSocket.GetState());
        holder._cardSwitch.setOnCheckedChangeListener((compoundButton, value) -> {
            try {
                WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocket, value);
            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.getMessage());
            }
            _dialog.dismiss();
        });

        return rowView;
    }
}
