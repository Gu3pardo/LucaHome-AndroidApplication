package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.enums.ShoppingItemType;

@SuppressWarnings({"WeakerAccess"})
public class ShoppingItem extends SuggestedShoppingItem {
    private static final String Tag = ShoppingItem.class.getSimpleName();

    private boolean _bought;

    public ShoppingItem(
            @NonNull UUID uuid,
            @NonNull String name,
            @NonNull ShoppingItemType type,
            int quantity,
            @NonNull String unit,
            boolean bought,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        super(uuid, name, type, quantity, unit, isOnServer, serverDbAction);
        _bought = bought;
    }

    public ShoppingItem(
            @NonNull UUID uuid,
            @NonNull String name,
            @NonNull ShoppingItemType type,
            int quantity,
            @NonNull String unit,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        this(uuid, name, type, quantity, unit, false, isOnServer, serverDbAction);
    }

    public void SetBought(boolean bought) {
        _bought = bought;
    }

    public boolean GetBought() {
        return _bought;
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                LucaServerActionTypes.ADD_SHOPPING_ITEM_F.toString(),
                _uuid, _name, _type.toString(), _quantity, _unit);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                LucaServerActionTypes.UPDATE_SHOPPING_ITEM_F.toString(),
                _uuid, _name, _type.toString(), _quantity, _unit);
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                LucaServerActionTypes.DELETE_SHOPPING_ITEM_F.toString(),
                _uuid);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Name\":\"%s\",\"Type\":\"%s\",\"Quantity\":%d,\"Unit\":\"%s\",\"Bought\":\"%s\"}",
                Tag, _uuid, _name, _type, _quantity, _unit, _bought ? "1" : "0");
    }
}
