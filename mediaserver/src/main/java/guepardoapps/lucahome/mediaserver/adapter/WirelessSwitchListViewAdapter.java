package guepardoapps.lucahome.mediaserver.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.services.RoomService;
import guepardoapps.lucahome.common.services.WirelessSwitchService;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.mediaserver.R;

public class WirelessSwitchListViewAdapter extends BaseAdapter {
    private static String Tag = WirelessSwitchListViewAdapter.class.getSimpleName();

    private class Holder {
        private TextView _titleText;
        private TextView _areaText;
        private TextView _codeText;
        private ImageView _cardImage;
        private Button _cardButton;
    }

    private ArrayList<WirelessSwitch> _listViewItems;
    private Dialog _dialog;
    private static LayoutInflater _inflater = null;

    public WirelessSwitchListViewAdapter(@NonNull Context context, @NonNull ArrayList<WirelessSwitch> listViewItems, @NonNull Dialog dialog) {
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

        View rowView = _inflater.inflate(R.layout.listview_card_wireless_switch, null);

        holder._titleText = rowView.findViewById(R.id.wirelessSwitchCardTitleText);
        holder._areaText = rowView.findViewById(R.id.wirelessSwitchCardAreaText);
        holder._codeText = rowView.findViewById(R.id.wirelessSwitchCardCodeText);
        holder._cardImage = rowView.findViewById(R.id.wirelessSwitchCardImage);
        holder._cardButton = rowView.findViewById(R.id.wirelessSwitchCardButton);

        final WirelessSwitch wirelessSwitch = _listViewItems.get(index);

        holder._titleText.setText(wirelessSwitch.GetName());
        holder._codeText.setText(wirelessSwitch.GetKeyCode());

        try {
            String locationName = RoomService.getInstance().GetByUuid(wirelessSwitch.GetRoomUuid()).GetName();
            holder._areaText.setText(locationName);
            holder._cardImage.setImageResource(wirelessSwitch.GetDrawable());
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
        }

        holder._cardButton.setOnClickListener(view -> {
            try {
                WirelessSwitchService.getInstance().ToggleWirelessSwitch(wirelessSwitch);
            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.getMessage());
            }
            _dialog.dismiss();
        });

        return rowView;
    }
}
