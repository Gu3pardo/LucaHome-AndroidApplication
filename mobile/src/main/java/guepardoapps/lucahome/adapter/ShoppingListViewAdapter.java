package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.rey.material.widget.FloatingActionButton;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.common.classes.ShoppingEntry;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.ShoppingListService;

public class ShoppingListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _titleText;
        private TextView _groupText;
        private TextView _amountText;
        private TextView _unitText;
        private ImageView _groupImageView;
        private CheckBox _boughtCheckbox;
        private FloatingActionButton _increaseButton;
        private FloatingActionButton _decreaseButton;
        private FloatingActionButton _deleteButton;
    }

    private SerializableList<ShoppingEntry> _listViewItems;
    private ShoppingListService _shoppingListService;
    private static LayoutInflater _inflater = null;

    public ShoppingListViewAdapter(@NonNull Context context, @NonNull SerializableList<ShoppingEntry> listViewItems) {
        _listViewItems = listViewItems;
        _shoppingListService = ShoppingListService.getInstance();
        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        Holder holder = new Holder();

        View rowView = _inflater.inflate(R.layout.listview_card_shopping, null);

        holder._titleText = rowView.findViewById(R.id.shoppingCardTitleText);
        holder._groupText = rowView.findViewById(R.id.shoppingGroupText);
        holder._amountText = rowView.findViewById(R.id.shoppingAmountText);
        holder._unitText = rowView.findViewById(R.id.shoppingUnitText);

        holder._groupImageView = rowView.findViewById(R.id.shoppingGroupCardImage);
        holder._boughtCheckbox = rowView.findViewById(R.id.shoppingCheckbox);

        holder._increaseButton = rowView.findViewById(R.id.shoppingCardIncreaseButton);
        holder._decreaseButton = rowView.findViewById(R.id.shoppingCardDecreaseButton);
        holder._deleteButton = rowView.findViewById(R.id.shoppingCardDeleteButton);

        final ShoppingEntry shoppingEntry = _listViewItems.getValue(index);

        holder._titleText.setText(shoppingEntry.GetName());
        holder._groupText.setText(shoppingEntry.GetGroup().toString());
        holder._amountText.setText(String.valueOf(shoppingEntry.GetQuantity()));
        holder._unitText.setText(shoppingEntry.GetUnit());

        holder._groupImageView.setImageResource(shoppingEntry.GetGroup().GetDrawable());

        holder._boughtCheckbox.setChecked(shoppingEntry.GetBought());
        holder._boughtCheckbox.setOnCheckedChangeListener((compoundButton, checked) -> _shoppingListService.GetDataList().getValue(index).SetBought(checked));

        holder._increaseButton.setOnClickListener(view -> {
            shoppingEntry.SetQuantity(shoppingEntry.GetQuantity() + 1);
            shoppingEntry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);
            _shoppingListService.UpdateShoppingEntry(shoppingEntry);
        });
        holder._decreaseButton.setOnClickListener(view -> {
            shoppingEntry.SetQuantity(shoppingEntry.GetQuantity() - 1);
            shoppingEntry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);
            _shoppingListService.UpdateShoppingEntry(shoppingEntry);
        });

        holder._deleteButton.setOnClickListener(view -> _shoppingListService.DeleteShoppingEntry(shoppingEntry));

        rowView.setVisibility((shoppingEntry.GetServerDbAction() == ILucaClass.LucaServerDbAction.Delete) ? View.GONE : View.VISIBLE);

        return rowView;
    }
}
