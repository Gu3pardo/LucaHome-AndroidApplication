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
import guepardoapps.lucahome.common.classes.MoneyMeterData;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.MoneyMeterListService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.MoneyMeterDataEditActivity;

public class MoneyMeterListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _dateText;
        private TextView _valueText;
        private TextView _unitText;
        private FloatingActionButton _updateButton;
        private FloatingActionButton _deleteButton;

        private void displayDeleteDialog(@NonNull final MoneyMeterData moneyMeterData) {
            final Dialog deleteDialog = new Dialog(_context);

            deleteDialog
                    .title(String.format(Locale.getDefault(), "Delete %s?", moneyMeterData.GetSaveDate()))
                    .positiveAction("Delete")
                    .negativeAction("Cancel")
                    .applyStyle(_isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                    .setCancelable(true);

            deleteDialog.positiveActionClickListener(view -> {
                _moneyMeterListService.DeleteMoneyMeterData(moneyMeterData);
                deleteDialog.dismiss();
            });

            deleteDialog.negativeActionClickListener(view -> deleteDialog.dismiss());

            deleteDialog.show();
        }
    }

    private Context _context;
    private MoneyMeterListService _moneyMeterListService;
    private NavigationService _navigationService;

    private SerializableList<MoneyMeterData> _listViewItems;

    private static LayoutInflater _inflater = null;
    private boolean _isLightTheme;

    public MoneyMeterListViewAdapter(@NonNull Context context, @NonNull SerializableList<MoneyMeterData> listViewItems) {
        _context = context;
        _moneyMeterListService = MoneyMeterListService.getInstance();
        _navigationService = NavigationService.getInstance();

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

        View rowView = _inflater.inflate(R.layout.listview_card_moneymeterdata, null);

        holder._dateText = rowView.findViewById(R.id.moneymeterdata_date_text_view);
        holder._valueText = rowView.findViewById(R.id.moneymeterdata_value_text_view);
        holder._unitText = rowView.findViewById(R.id.moneymeterdata_unit_text_view);
        holder._updateButton = rowView.findViewById(R.id.moneymeterdata_card_update_button);
        holder._deleteButton = rowView.findViewById(R.id.moneymeterdata_card_delete_button);

        final MoneyMeterData moneyMeterData = _listViewItems.getValue(index);

        holder._dateText.setText(moneyMeterData.GetSaveDate().DDMMYYYY());
        holder._valueText.setText(String.valueOf(moneyMeterData.GetAmount()));
        holder._unitText.setText(moneyMeterData.GetUnit());

        holder._updateButton.setOnClickListener(view -> {
            Bundle data = new Bundle();
            moneyMeterData.SetServerAction(MoneyMeterData.ServerAction.Update);
            data.putSerializable(MoneyMeterListService.MoneyMeterDataIntent, moneyMeterData);
            _navigationService.NavigateToActivityWithData(_context, MoneyMeterDataEditActivity.class, data);
        });

        holder._deleteButton.setOnClickListener(view -> holder.displayDeleteDialog(moneyMeterData));

        rowView.setVisibility((moneyMeterData.GetServerDbAction() == ILucaClass.LucaServerDbAction.Delete) ? View.GONE : View.VISIBLE);

        return rowView;
    }
}
