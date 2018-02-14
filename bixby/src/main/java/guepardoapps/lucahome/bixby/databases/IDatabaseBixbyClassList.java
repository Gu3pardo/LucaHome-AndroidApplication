package guepardoapps.lucahome.bixby.databases;

import android.database.SQLException;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public interface IDatabaseBixbyClassList<T> {
    String DatabaseName = "DatabaseBixbyClassListDb";

    int DatabaseVersion = 1;

    IDatabaseBixbyClassList Open() throws SQLException;

    void Close();

    long AddEntry(@NonNull T entry) throws SQLException;

    long UpdateEntry(@NonNull T entry) throws SQLException;

    long DeleteEntry(@NonNull T entry) throws SQLException;

    void ClearDatabase();

    ArrayList<T> GetList(String selection, String groupBy, String orderBy) throws SQLException;
}
