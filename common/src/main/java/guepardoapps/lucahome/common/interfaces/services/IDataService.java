package guepardoapps.lucahome.common.interfaces.services;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableList;

public interface IDataService {
    void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout);

    void Dispose();

    SerializableList<?> GetDataList();

    SerializableList<?> SearchDataList(@NonNull String searchKey);

    int GetHighestId();

    void LoadData();

    boolean GetReloadEnabled();

    void SetReloadEnabled(boolean reloadEnabled);

    int GetReloadTimeout();

    void SetReloadTimeout(int reloadTimeout);
}
