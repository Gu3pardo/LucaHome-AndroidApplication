package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;
import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.common.classes.MeterData;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.MeterListService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.MeterDataEditActivity;

public class MeterListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _dateText;
        private TextView _timeText;
        private TextView _valueText;
        private FloatingActionButton _updateButton;
        private FloatingActionButton _deleteButton;

        private void navigateToEditActivity(@NonNull final MeterData meterData) {
            Bundle data = new Bundle();
            meterData.SetServerAction(MeterData.ServerAction.Update);
            data.putSerializable(MeterListService.MeterDataIntent, meterData);
            NavigationService.getInstance().NavigateToActivityWithData(_context, MeterDataEditActivity.class, data);
        }

        private void displayDeleteDialog(@NonNull final MeterData meterData) {
            final Dialog deleteDialog = new Dialog(_context);

            deleteDialog
                    .title(String.format(Locale.getDefault(), "Delete %s?", meterData.GetSaveDate()))
                    .positiveAction("Delete")
                    .negativeAction("Cancel")
                    .applyStyle(_isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                    .setCancelable(true);

            deleteDialog.positiveActionClickListener(view -> {
                MeterListService.getInstance().DeleteMeterData(meterData);
                deleteDialog.dismiss();
            });

            deleteDialog.negativeActionClickListener(view -> deleteDialog.dismiss());

            deleteDialog.show();
        }
    }

    private Context _context;
    private SerializableList<MeterData> _listViewItems;

    private static LayoutInflater _inflater = null;
    private boolean _isLightTheme;

    public MeterListViewAdapter(@NonNull Context context, @NonNull SerializableList<MeterData> listViewItems) {
        _context = context;
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

        View rowView = _inflater.inflate(R.layout.listview_card_meterdata, null);

        holder._dateText = rowView.findViewById(R.id.meterdata_date_text_view);
        holder._timeText = rowView.findViewById(R.id.meterdata_time_text_view);
        holder._valueText = rowView.findViewById(R.id.meterdata_value_text_view);
        holder._updateButton = rowView.findViewById(R.id.meterdata_card_update_button);
        holder._deleteButton = rowView.findViewById(R.id.meterdata_card_delete_button);

        final MeterData meterData = _listViewItems.getValue(index);

        holder._dateText.setText(meterData.GetSaveDate().DDMMYYYY());
        holder._timeText.setText(meterData.GetSaveTime().HHMM());
        holder._valueText.setText(String.valueOf(meterData.GetValue()));

        holder._updateButton.setOnClickListener(view -> holder.navigateToEditActivity(meterData));
        holder._deleteButton.setOnClickListener(view -> holder.displayDeleteDialog(meterData));

        rowView.setVisibility((meterData.GetServerDbAction() == ILucaClass.LucaServerDbAction.Delete) ? View.GONE : View.VISIBLE);

        return rowView;
    }
}
