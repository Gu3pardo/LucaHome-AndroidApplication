package guepardoapps.lucahome.bixby.services;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings({"unused"})
public interface IBixbyPairService<T> {
    String BixbyPairLoadFinishedBroadcast = "guepardoapps.bixby.services.bixpypair.load.finished";
    String BixbyPairAddFinishedBroadcast = "guepardoapps.bixby.services.bixpypair.add.finished";
    String BixbyPairUpdateFinishedBroadcast = "guepardoapps.bixby.services.bixpypair.update.finished";
    String BixbyPairDeleteFinishedBroadcast = "guepardoapps.bixby.services.bixpypair.delete.finished";

    String BixbyPairLoadFinishedBundle = "BixbyPairLoadFinishedBundle";
    String BixbyPairAddFinishedBundle = "BixbyPairAddFinishedBundle";
    String BixbyPairUpdateFinishedBundle = "BixbyPairUpdateFinishedBundle";
    String BixbyPairDeleteFinishedBundle = "BixbyPairDeleteFinishedBundle";

    void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout, boolean displayNotification, Class<?> receiverActivity);

    void Dispose();

    void BixbyButtonPressed() throws Exception;

    ArrayList<T> GetDataList() throws NoSuchMethodException;

    T GetById(int id) throws NoSuchMethodException;

    int GetHighestId() throws NoSuchMethodException;

    ArrayList<T> SearchDataList(@NonNull String searchKey) throws NoSuchMethodException;

    ArrayList<String> GetPackageNameList();

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
