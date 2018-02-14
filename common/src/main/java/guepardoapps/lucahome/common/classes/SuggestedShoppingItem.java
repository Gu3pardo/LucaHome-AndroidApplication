package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.enums.ShoppingItemType;

@SuppressWarnings({"WeakerAccess"})
public class SuggestedShoppingItem implements ILucaClass {
    private static final String Tag = SuggestedShoppingItem.class.getSimpleName();

    protected UUID _uuid;
    protected String _name;
    protected ShoppingItemType _type;
    protected int _quantity;
    protected String _unit;

    protected boolean _isOnServer;
    protected LucaServerDbAction _serverDbAction;

    public SuggestedShoppingItem(
            @NonNull UUID uuid,
            @NonNull String name,
            @NonNull ShoppingItemType type,
            int quantity,
            @NonNull String unit,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _name = name;
        _type = type;
        _quantity = quantity;
        _unit = unit;
        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetRoomUuid not implemented for " + Tag);
    }

    public void SetName(@NonNull String name) {
        _name = name;
    }

    public String GetName() {
        return _name;
    }

    public void SetType(@NonNull ShoppingItemType type) {
        _type = type;
    }

    public ShoppingItemType GetType() {
        return _type;
    }

    public void SetQuantity(int quantity) {
        _quantity = quantity;
        if (_quantity < 1) {
            _quantity = 1;
        }
    }

    public int GetQuantity() {
        return _quantity;
    }

    public void SetUnit(@NonNull String unit) {
        _unit = unit;
    }

    public String GetUnit() {
        return _unit;
    }

    public int GetIcon() {
        return _type.GetDrawable();
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                LucaServerActionTypes.ADD_SUGGESTED_SHOPPING_ITEM_F.toString(),
                _uuid, _name, _type.toString(), _quantity, _unit);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                LucaServerActionTypes.UPDATE_SUGGESTED_SHOPPING_ITEM_F.toString(),
                _uuid, _name, _type.toString(), _quantity, _unit);
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                LucaServerActionTypes.DELETE_SUGGESTED_SHOPPING_ITEM_F.toString(),
                _uuid);
    }

    @Override
    public void SetIsOnServer(boolean isOnServer) {
        _isOnServer = isOnServer;
    }

    @Override
    public boolean GetIsOnServer() {
        return _isOnServer;
    }

    @Override
    public void SetServerDbAction(@NonNull LucaServerDbAction serverDbAction) {
        _serverDbAction = serverDbAction;
    }

    @Override
    public LucaServerDbAction GetServerDbAction() {
        return _serverDbAction;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Name\":\"%s\",\"Type\":\"%s\",\"Quantity\":%d,\"Unit\":\"%s\"}",
                Tag, _uuid, _name, _type, _quantity, _unit);
    }
}
