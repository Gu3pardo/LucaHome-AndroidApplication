package guepardoapps.lucahome.common.interfaces.services;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableList;

@SuppressWarnings({"unused"})
public interface IDataNotificationService {
    void Initialize(@NonNull Context context, @NonNull Class<?> receiverActivity, boolean displayNotification, boolean reloadEnabled, int reloadTimeout);

    void Dispose();

    SerializableList<?> GetDataList();

    SerializableList<?> SearchDataList(@NonNull String searchKey);

    int GetHighestId();

    void LoadData();

    void ShowNotification();

    void CloseNotification();

    void SetReceiverActivity(@NonNull Class<?> receiverActivity);

    Class<?> GetReceiverActivity();

    boolean GetDisplayNotification();

    void SetDisplayNotification(boolean displayNotification);

    boolean GetReloadEnabled();

    void SetReloadEnabled(boolean reloadEnabled);

    int GetReloadTimeout();

    void SetReloadTimeout(int reloadTimeout);
}
