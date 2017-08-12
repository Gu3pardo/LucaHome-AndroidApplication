package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.enums.ShoppingEntryGroup;

public class ShoppingEntry implements Serializable {
    private static final long serialVersionUID = 2455793897127065732L;
    private static final String TAG = ShoppingEntry.class.getSimpleName();

    private int _id;
    private String _name;
    private ShoppingEntryGroup _group;
    private int _quantity;

    private boolean _bought;

    public ShoppingEntry(
            int id,
            @NonNull String name,
            @NonNull ShoppingEntryGroup group,
            int quantity,
            boolean bought) {
        _id = id;
        _name = name;
        _group = group;
        _quantity = quantity;
        _bought = bought;
    }

    public ShoppingEntry(
            int id,
            @NonNull String name,
            @NonNull ShoppingEntryGroup group,
            int quantity) {
        this(id, name, group, quantity, false);
    }

    public int GetId() {
        return _id;
    }

    public String GetName() {
        return _name;
    }

    public void SetName(@NonNull String name) {
        _name = name;
    }

    public ShoppingEntryGroup GetGroup() {
        return _group;
    }

    public void SetGroup(@NonNull ShoppingEntryGroup group) {
        _group = group;
    }

    public int GetQuantity() {
        return _quantity;
    }

    public void SetQuantity(int quantity) {
        _quantity = quantity;
        if (_quantity < 1) {
            _quantity = 1;
        }
    }

    public boolean GetBought() {
        return _bought;
    }

    public void SetBought(boolean bought) {
        _bought = bought;
    }

    public int Icon() {
        return _group.GetDrawable();
    }

    public String CommandAdd() {
        return String.format(Locale.getDefault(), LucaServerAction.ADD_SHOPPING_ENTRY_F.toString(), _id, _name, _group.toString(), _quantity);
    }

    public String CommandUpdate() {
        return String.format(Locale.getDefault(), LucaServerAction.UPDATE_SHOPPING_ENTRY_F.toString(), _id, _name, _group.toString(), _quantity);
    }

    public String CommandDelete() {
        return String.format(Locale.getDefault(), LucaServerAction.DELETE_SHOPPING_ENTRY_F.toString(), _id);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Id: %d );(Name: %s );(Group: %s );(Quantity: %d );(Bought: %s ))", TAG, _id, _name, _group, _quantity, _bought);
    }
}
