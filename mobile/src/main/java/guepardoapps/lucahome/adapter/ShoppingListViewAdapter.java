package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.ShoppingEntry;
import guepardoapps.lucahome.common.service.ShoppingListService;

public class ShoppingListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _titleText;
        private TextView _groupText;
        private TextView _amountText;
        private CheckBox _boughtCheckbox;
        private FloatingActionButton _increaseButton;
        private FloatingActionButton _decreaseButton;
        private FloatingActionButton _deleteButton;
    }

    private static final String TAG = ShoppingListViewAdapter.class.getSimpleName();
    private Logger _logger;

    private ShoppingListService _shoppingListService;

    private static LayoutInflater _inflater = null;

    private SerializableList<ShoppingEntry> _listViewItems;

    public ShoppingListViewAdapter(@NonNull Context context, @NonNull SerializableList<ShoppingEntry> listViewItems) {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");

        _listViewItems = listViewItems;

        _shoppingListService = ShoppingListService.getInstance();

        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        View rowView = _inflater.inflate(R.layout.listview_card_shopping, null);

        holder._titleText = rowView.findViewById(R.id.shoppingCardTitleText);
        holder._groupText = rowView.findViewById(R.id.shoppingGroupText);
        holder._amountText = rowView.findViewById(R.id.shoppingAmountText);

        holder._boughtCheckbox = rowView.findViewById(R.id.shoppingCheckbox);

        holder._increaseButton = rowView.findViewById(R.id.shoppingCardIncreaseButton);
        holder._decreaseButton = rowView.findViewById(R.id.shoppingCardDecreaseButton);
        holder._deleteButton = rowView.findViewById(R.id.shoppingCardDeleteButton);

        final ShoppingEntry shoppingEntry = _listViewItems.getValue(index);

        holder._titleText.setText(shoppingEntry.GetName());
        holder._groupText.setText(shoppingEntry.GetGroup().toString());
        holder._amountText.setText(String.valueOf(shoppingEntry.GetQuantity()));

        holder._boughtCheckbox.setChecked(shoppingEntry.GetBought());
        holder._boughtCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                _shoppingListService.GetShoppingList().getValue(index).SetBought(checked);
            }
        });

        holder._increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_increaseButton setOnClickListener onClick");
                shoppingEntry.SetQuantity(shoppingEntry.GetQuantity() + 1);
                _shoppingListService.UpdateShoppingEntry(shoppingEntry);
            }
        });
        holder._decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_decreaseButton setOnClickListener onClick");
                shoppingEntry.SetQuantity(shoppingEntry.GetQuantity() - 1);
                _shoppingListService.UpdateShoppingEntry(shoppingEntry);
            }
        });

        holder._deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_deleteButton setOnClickListener onClick");
                _shoppingListService.DeleteShoppingEntry(shoppingEntry);
            }
        });

        return rowView;
    }
}
