package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

@SuppressWarnings({"unused"})
public class ShoppingEntry implements Serializable, ILucaClass {
    private static final long serialVersionUID = 2455793897127065732L;
    private static final String TAG = ShoppingEntry.class.getSimpleName();

    private int _id;
    private String _name;
    private ShoppingEntryGroup _group;
    private int _quantity;
    private String _unit;

    private boolean _bought;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public ShoppingEntry(
            int id,
            @NonNull String name,
            @NonNull ShoppingEntryGroup group,
            int quantity,
            @NonNull String unit,
            boolean bought,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _id = id;

        _name = name;
        _group = group;
        _quantity = quantity;
        _unit = unit;

        _bought = bought;

        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    public ShoppingEntry(
            int id,
            @NonNull String name,
            @NonNull ShoppingEntryGroup group,
            int quantity,
            @NonNull String unit,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        this(id, name, group, quantity, unit, false, isOnServer, serverDbAction);
    }

    @Override
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

    public String GetUnit() {
        return _unit;
    }

    public void SetUnit(@NonNull String unit) {
        _unit = unit;
    }

    public boolean GetBought() {
        return _bought;
    }

    public void SetBought(boolean bought) {
        _bought = bought;
    }

    public int GetIcon() {
        return _group.GetDrawable();
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
    public String CommandAdd() {
        return String.format(Locale.getDefault(), LucaServerAction.ADD_SHOPPING_ENTRY_F.toString(), _id, _name, _group.toString(), _quantity, _unit);
    }

    @Override
    public String CommandUpdate() {
        return String.format(Locale.getDefault(), LucaServerAction.UPDATE_SHOPPING_ENTRY_F.toString(), _id, _name, _group.toString(), _quantity, _unit);
    }

    @Override
    public String CommandDelete() {
        return String.format(Locale.getDefault(), LucaServerAction.DELETE_SHOPPING_ENTRY_F.toString(), _id);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Id: %d );(Name: %s );(Group: %s );(Quantity: %d );(Unit: %s );(Bought: %s ))", TAG, _id, _name, _group, _quantity, _unit, _bought);
    }
}
