package guepardoapps.lucahome.common.services;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public interface ILucaService<T> {
    void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout, boolean displayNotification, Class<?> receiverActivity);

    void Dispose();

    ArrayList<T> GetDataList() throws NoSuchMethodException;

    T GetByUuid(@NonNull UUID uuid) throws NoSuchMethodException;

    ArrayList<T> SearchDataList(@NonNull String searchKey) throws NoSuchMethodException;

    void LoadData() throws NoSuchMethodException;

    void AddEntry(@NonNull T newEntry) throws NoSuchMethodException;

    void UpdateEntry(@NonNull T updateEntry) throws NoSuchMethodException;

    void DeleteEntry(@NonNull T deleteEntry) throws NoSuchMethodException;

    void SetReloadEnabled(boolean reloadEnabled) throws NoSuchMethodException;

    boolean GetReloadEnabled() throws NoSuchMethodException;

    void SetReloadTimeout(int reloadTimeout) throws NoSuchMethodException;

    int GetReloadTimeout() throws NoSuchMethodException;

    Calendar GetLastUpdate() throws NoSuchMethodException;

    void ShowNotification() throws NoSuchMethodException;

    void CloseNotification() throws NoSuchMethodException;

    void SetDisplayNotification(boolean displayNotification) throws NoSuchMethodException;

    boolean GetDisplayNotification() throws NoSuchMethodException;

    void SetReceiverActivity(Class<?> receiverActivity) throws NoSuchMethodException;

    Class<?> GetReceiverActivity() throws NoSuchMethodException;
}
