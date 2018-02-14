package guepardoapps.lucahome.common.databases;

import android.database.SQLException;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public interface IDatabaseLucaClassList<T> {
    String KeyRowId = "_id";
    String KeyUuid = "_uuid";
    String KeyIsOnServer = "_isOnServer";
    String KeyServerAction = "_serverAction";

    String DatabaseName = "IDatabaseLucaClassListDb";
    int DatabaseVersion = 1;

    IDatabaseLucaClassList Open() throws SQLException;

    void Close();

    long AddEntry(@NonNull T entry) throws SQLException;

    long UpdateEntry(@NonNull T entry) throws SQLException;

    long DeleteEntry(@NonNull T entry) throws SQLException;

    void ClearDatabase();

    ArrayList<T> GetList(String selection, String groupBy, String orderBy) throws SQLException;

    ArrayList<String> GetStringQueryList(boolean distinct, @NonNull String column, String selection, String groupBy, String orderBy) throws SQLException;

    ArrayList<Integer> GetIntegerQueryList(boolean distinct, @NonNull String column, String selection, String groupBy, String orderBy) throws SQLException;
}
