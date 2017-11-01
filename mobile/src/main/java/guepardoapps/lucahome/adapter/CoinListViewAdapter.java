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
import android.widget.TextView;

import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;
import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.common.classes.Coin;
import guepardoapps.lucahome.common.dto.CoinDto;
import guepardoapps.lucahome.common.service.CoinService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.CoinEditActivity;

public class CoinListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _titleText;
        private TextView _amountText;
        private TextView _conversionText;
        private ImageView _aggregationImage;
        private TextView _valueText;
        private ImageView _imageView;
        private FloatingActionButton _updateButton;
        private FloatingActionButton _deleteButton;

        private void navigateToEditActivity(@NonNull final Coin coin) {
            Bundle data = new Bundle();
            data.putSerializable(CoinService.CoinIntent, new CoinDto(coin.GetId(), coin.GetUser(), coin.GetType(), coin.GetAmount(), CoinDto.Action.Update));
            _navigationService.NavigateToActivityWithData(_context, CoinEditActivity.class, data);
        }

        private void displayDeleteDialog(@NonNull final Coin coin) {
            final Dialog deleteDialog = new Dialog(_context);

            deleteDialog
                    .title(String.format(Locale.getDefault(), "Delete %s?", coin.GetType()))
                    .positiveAction("Delete")
                    .negativeAction("Cancel")
                    .applyStyle(_isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                    .setCancelable(true);

            deleteDialog.positiveActionClickListener(view -> {
                _coinService.DeleteCoin(coin);
                deleteDialog.dismiss();
            });

            deleteDialog.negativeActionClickListener(view -> deleteDialog.dismiss());

            deleteDialog.show();
        }
    }

    private Context _context;
    private CoinService _coinService;
    private NavigationService _navigationService;

    private SerializableList<Coin> _listViewItems;

    private static LayoutInflater _inflater = null;
    private boolean _isLightTheme;

    public CoinListViewAdapter(@NonNull Context context, @NonNull SerializableList<Coin> listViewItems) {
        _context = context;
        _coinService = CoinService.getInstance();
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

        View rowView = _inflater.inflate(R.layout.listview_card_coin, null);

        holder._titleText = rowView.findViewById(R.id.coinCardTitleText);
        holder._amountText = rowView.findViewById(R.id.coinAmountText);
        holder._conversionText = rowView.findViewById(R.id.coinConversionText);
        holder._aggregationImage = rowView.findViewById(R.id.coinAggregationImage);
        holder._valueText = rowView.findViewById(R.id.coinValueText);
        holder._imageView = rowView.findViewById(R.id.coin_card_image);
        holder._updateButton = rowView.findViewById(R.id.coin_card_update_button);
        holder._deleteButton = rowView.findViewById(R.id.coin_card_delete_button);

        final Coin coin = _listViewItems.getValue(index);

        holder._titleText.setText(coin.GetType());
        holder._amountText.setText(String.valueOf(coin.GetAmount()));
        holder._conversionText.setText(coin.GetCurrentConversionString());
        holder._valueText.setText(coin.GetValueString());

        holder._imageView.setImageResource(coin.GetIcon());

        if (coin.GetCurrentTrend() == Coin.Trend.Rise) {
            holder._aggregationImage.setImageResource(android.R.drawable.arrow_up_float);
        } else if (coin.GetCurrentTrend() == Coin.Trend.Fall) {
            holder._aggregationImage.setImageResource(android.R.drawable.arrow_down_float);
        } else {
            holder._aggregationImage.setImageResource(android.R.drawable.radiobutton_off_background);
        }

        holder._updateButton.setOnClickListener(view -> holder.navigateToEditActivity(coin));

        holder._deleteButton.setOnClickListener(view -> holder.displayDeleteDialog(coin));

        return rowView;
    }
}
