package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SuggestedMeal implements ILucaClass {
    private static final String Tag = SuggestedMeal.class.getSimpleName();

    private UUID _uuid;
    private String _title;
    private String _description;
    private int _rating;
    private int _useCounter;
    private ArrayList<UUID> _shoppingItemUuidList;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public SuggestedMeal(
            UUID uuid,
            @NonNull String title,
            @NonNull String description,
            int rating,
            int useCounter,
            @NonNull ArrayList<UUID> shoppingItemUuidList,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _title = title;
        _description = description;
        _rating = rating;
        _useCounter = useCounter;
        _shoppingItemUuidList = shoppingItemUuidList;
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

    public void SetTitle(@NonNull String title) {
        _title = title;
    }

    public String GetTitle() {
        return _title;
    }

    public void SetDescription(@NonNull String description) {
        _description = description;
    }

    public String GetDescription() {
        return _description;
    }

    public void SetRating(int rating) {
        _rating = rating;
    }

    public int GetRating() {
        return _rating;
    }

    public void SetUseCounter(int useCounter) {
        _useCounter = useCounter;
    }

    public int GetUseCounter() {
        return _useCounter;
    }

    public void SetShoppingItemUuidList(@NonNull ArrayList<UUID> shoppingItemUuidList) {
        _shoppingItemUuidList = shoppingItemUuidList;
    }

    public ArrayList<UUID> GetShoppingItemUuidList() {
        return _shoppingItemUuidList;
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&title=%s&description=%s&rating=%d&shoppingitemuuidlist=%s",
                LucaServerActionTypes.ADD_SUGGESTED_MEAL.toString(), _uuid, _title, _description, _rating, getShoppingItemUuidList());
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&title=%s&description=%s&rating=%d&shoppingitemlist=%s",
                LucaServerActionTypes.UPDATE_SUGGESTED_MEAL.toString(), _uuid, _title, _description, _rating, getShoppingItemUuidList());
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.DELETE_SUGGESTED_MEAL.toString(), _uuid);
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
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Title\":\"%s\",\"Description\":\"%s\",\"Rating\":\"%s\",\"UseCounter\":%d,\"ShoppingItemIdList\":\"%s\"}",
                Tag, _uuid, _title, _description, _rating, _useCounter, getShoppingItemUuidList());
    }

    private String getShoppingItemUuidList() {
        StringBuilder stringBuilder = new StringBuilder();

        for (UUID uuid : _shoppingItemUuidList) {
            stringBuilder.append(uuid.toString()).append(",");
        }

        if (stringBuilder.length() > 0) {
            return stringBuilder.substring(0, stringBuilder.length() - 2);
        }

        return stringBuilder.toString();
    }
}
