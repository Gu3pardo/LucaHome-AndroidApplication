package guepardoapps.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.common.enums.ShoppingEntryGroup;

public class ShoppingEntryDto implements Serializable {
    private static final long serialVersionUID = 6755793897127065732L;
    private static final String TAG = ShoppingEntryDto.class.getSimpleName();

    private int _id;
    private String _name;
    private ShoppingEntryGroup _group;
    private int _quantity;

    public ShoppingEntryDto(
            int id,
            @NonNull String name,
            @NonNull ShoppingEntryGroup group,
            int quantity) {
        _id = id;
        _name = name;
        _group = group;
        _quantity = quantity;
    }

    public int GetId() {
        return _id;
    }

    public String GetName() {
        return _name;
    }

    public ShoppingEntryGroup GetGroup() {
        return _group;
    }

    public int GetQuantity() {
        return _quantity;
    }

    public int Icon() {
        return _group.GetDrawable();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Id: %d );(Name: %s );(Group: %s );(Quantity: %d ))", TAG, _id, _name, _group, _quantity);
    }
}
