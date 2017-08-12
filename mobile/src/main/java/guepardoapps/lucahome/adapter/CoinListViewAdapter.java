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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.Coin;
import guepardoapps.lucahome.common.dto.CoinDto;
import guepardoapps.lucahome.data.service.CoinService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.CoinEditActivity;

public class CoinListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _titleText;
        private TextView _amountText;
        private TextView _conversionText;
        private TextView _valueText;
        private ImageView _imageView;
        private FloatingActionButton _updateButton;
        private FloatingActionButton _deleteButton;
    }

    private static final String TAG = CoinListViewAdapter.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    private NavigationService _navigationService;

    private static LayoutInflater _inflater = null;

    private SerializableList<Coin> _listViewItems;

    public CoinListViewAdapter(@NonNull Context context, @NonNull SerializableList<Coin> listViewItems) {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");

        _context = context;

        _listViewItems = listViewItems;

        _navigationService = NavigationService.getInstance();

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

        View rowView = _inflater.inflate(R.layout.listview_card_coin, null);

        holder._titleText = rowView.findViewById(R.id.coinCardTitleText);
        holder._amountText = rowView.findViewById(R.id.coinAmountText);
        holder._conversionText = rowView.findViewById(R.id.coinConversionText);
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

        holder._updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_updateButton setOnClickListener onClick");
                Bundle data = new Bundle();
                data.putSerializable(CoinService.CoinIntent, new CoinDto(coin.GetId(), coin.GetUser(), coin.GetType(), coin.GetAmount(), CoinDto.Action.Update));
                _navigationService.NavigateToActivityWithData(_context, CoinEditActivity.class, data);
            }
        });

        holder._deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_deleteButton setOnClickListener onClick");
                /*TODO handle delete coin*/
            }
        });

        return rowView;
    }
}
