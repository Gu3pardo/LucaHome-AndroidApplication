package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;
import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.dto.WirelessSwitchDto;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.WirelessSwitchService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.WirelessSwitchEditActivity;

public class SwitchListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _titleText;
        private TextView _areaText;
        private TextView _remoteIdText;
        private TextView _keyCodeText;
        private ImageView _cardImage;
        private Switch _cardSwitch;
        private FloatingActionButton _updateButton;
        private FloatingActionButton _deleteButton;
        private View _actionView;

        private void displayDeleteDialog(@NonNull final WirelessSwitch wirelessSwitch) {
            final Dialog deleteDialog = new Dialog(_context);

            deleteDialog
                    .title(String.format(Locale.getDefault(), "Delete %s?", wirelessSwitch.GetName()))
                    .positiveAction("Delete")
                    .negativeAction("Cancel")
                    .applyStyle(_isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                    .setCancelable(true);

            deleteDialog.positiveActionClickListener(view -> {
                _wirelessSwitchService.DeleteWirelessSwitch(wirelessSwitch);
                deleteDialog.dismiss();
            });

            deleteDialog.negativeActionClickListener(view -> deleteDialog.dismiss());

            deleteDialog.show();
        }
    }

    private Context _context;
    private NavigationService _navigationService;
    private WirelessSwitchService _wirelessSwitchService;

    private SerializableList<WirelessSwitch> _listViewItems;

    private static LayoutInflater _inflater = null;
    private boolean _isLightTheme;

    public SwitchListViewAdapter(@NonNull Context context, @NonNull SerializableList<WirelessSwitch> listViewItems) {
        _context = context;
        _navigationService = NavigationService.getInstance();
        _wirelessSwitchService = WirelessSwitchService.getInstance();

        _listViewItems = listViewItems;

        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _isLightTheme = ThemeManager.getInstance().getCurrentTheme() == 0;
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

        View rowView = _inflater.inflate(R.layout.listview_card_switch, null);

        holder._titleText = rowView.findViewById(R.id.switchCardTitleText);
        holder._areaText = rowView.findViewById(R.id.switchCardAreaText);
        holder._remoteIdText = rowView.findViewById(R.id.switchCardRemoteIdText);
        holder._keyCodeText = rowView.findViewById(R.id.switchCardKeyCodeText);
        holder._cardImage = rowView.findViewById(R.id.switchCardImage);
        holder._cardSwitch = rowView.findViewById(R.id.switchCardSwitch);
        holder._updateButton = rowView.findViewById(R.id.switch_card_update_button);
        holder._deleteButton = rowView.findViewById(R.id.switch_card_delete_button);
        holder._actionView = rowView.findViewById(R.id.switchCardActionView);

        final WirelessSwitch wirelessSwitch = _listViewItems.getValue(index);

        holder._titleText.setText(wirelessSwitch.GetName());
        holder._areaText.setText(wirelessSwitch.GetArea());
        holder._remoteIdText.setText(String.valueOf(wirelessSwitch.GetRemoteId()));
        holder._keyCodeText.setText(String.valueOf(wirelessSwitch.GetKeyCode()));

        holder._cardImage.setImageResource(wirelessSwitch.GetWallpaper());

        holder._actionView.setBackgroundResource(wirelessSwitch.GetAction() ? R.drawable.circle_green : R.drawable.circle_red);

        holder._cardSwitch.setChecked(wirelessSwitch.GetAction());
        holder._cardSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            try {
                _wirelessSwitchService.ToggleWirelessSwitch(wirelessSwitch);
            } catch (Exception exception) {
                // TODO perhaps log exception or make a notification...
            }
        });

        holder._updateButton.setOnClickListener(view -> {
            Bundle data = new Bundle();
            data.putSerializable(WirelessSwitchService.WirelessSwitchIntent, new WirelessSwitchDto(wirelessSwitch.GetId(), wirelessSwitch.GetName(), wirelessSwitch.GetArea(), wirelessSwitch.GetRemoteId(), wirelessSwitch.GetKeyCode(), WirelessSwitchDto.Action.Update));
            _navigationService.NavigateToActivityWithData(_context, WirelessSwitchEditActivity.class, data);
        });

        holder._deleteButton.setOnClickListener(view -> holder.displayDeleteDialog(wirelessSwitch));

        rowView.setVisibility((wirelessSwitch.GetServerDbAction() == ILucaClass.LucaServerDbAction.Delete) ? View.GONE : View.VISIBLE);

        return rowView;
    }
}
