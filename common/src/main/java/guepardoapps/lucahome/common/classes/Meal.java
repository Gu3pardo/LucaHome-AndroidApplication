package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

public class Meal implements ILucaClass {
    private static final String Tag = Meal.class.getSimpleName();

    private UUID _uuid;
    private String _title;
    private String _description;
    private Calendar _date;
    private ArrayList<UUID> _shoppingItemUuidList;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public Meal(
            @NonNull UUID uuid,
            @NonNull String title,
            @NonNull String description,
            @NonNull Calendar date,
            @NonNull ArrayList<UUID> shoppingItemUuidList,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _title = title;
        _description = description;
        _date = date;
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
        throw new NoSuchMethodException("Method GetCommandAdd not implemented for " + Tag);
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

    public void SetDate(@NonNull Calendar date) {
        _date = date;
    }

    public Calendar GetDate() {
        return _date;
    }

    public void SetShoppingItemUuidList(@NonNull ArrayList<UUID> shoppingItemUuidList) {
        _shoppingItemUuidList = shoppingItemUuidList;
    }

    public ArrayList<UUID> GetShoppingItemUuidList() {
        return _shoppingItemUuidList;
    }

    @Override
    public String GetCommandAdd() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetCommandAdd not implemented for " + Tag);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&day=%d&month=%d&year=%d&title=%s&description=%s&shoppingitemuuidlist=%s",
                LucaServerActionTypes.UPDATE_MEAL.toString(), _uuid, _date.get(Calendar.DAY_OF_MONTH), _date.get(Calendar.MONTH), _date.get(Calendar.YEAR), _title, _description, getShoppingItemUuidList());
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.CLEAR_MEAL.toString(), _uuid);
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
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Title\":\"%s\",\"Description\":\"%s\",\"Date\":\"%s\",\"ShoppingItemIdList\":\"%s\"}",
                Tag, _uuid, _title, _description, _date, getShoppingItemUuidList());
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
