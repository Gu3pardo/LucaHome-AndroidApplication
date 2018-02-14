package guepardoapps.lucahome.common.services;

import android.content.Context;
import android.database.SQLException;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.CalendarEntry;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.CalendarController;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class CalendarService implements ICalendarService {
    private static final String Tag = CalendarService.class.getSimpleName();

    private static final CalendarService Singleton = new CalendarService();

    private static final int MinTimeoutMin = 30;
    private static final int MaxTimeoutMin = 6 * 60;

    private BroadcastController _broadcastController;
    private CalendarController _calendarController;

    private Calendar _lastUpdate;

    private boolean _isInitialized;

    private boolean _reloadEnabled;
    private int _reloadTimeout;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                LoadData();
            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.toString());
            }
            if (_reloadEnabled) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private CalendarService() {
    }

    public static CalendarService getInstance() {
        return Singleton;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout, boolean displayNotification, Class<?> receiverActivity) {
        if (_isInitialized) {
            Logger.getInstance().Warning(Tag, "Already initialized!");
            return;
        }

        _lastUpdate = Calendar.getInstance();
        _reloadEnabled = reloadEnabled;
        _broadcastController = new BroadcastController(context);
        _calendarController = new CalendarController(context);

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _isInitialized = false;
    }

    @Override
    public ArrayList<CalendarEntry> GetDataList() {
        try {
            return _calendarController.ReadCalendar(DateUtils.YEAR_IN_MILLIS * 10000);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public CalendarEntry GetByUuid(@NonNull UUID uuid) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetByUuid not implemented for " + Tag);
    }

    @Override
    public ArrayList<CalendarEntry> SearchDataList(@NonNull String searchKey) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SearchDataList not implemented for " + Tag);
    }

    @Override
    public void LoadData() throws NoSuchMethodException {
        ArrayList<CalendarEntry> calendarEntries = GetDataList();
        _broadcastController.SendSerializableBroadcast(CalendarLoadFinishedBroadcast, CalendarLoadFinishedBundle, calendarEntries);
    }

    @Override
    public void AddEntry(@NonNull CalendarEntry entry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method AddEntry not implemented for " + Tag);
    }

    @Override
    public void UpdateEntry(@NonNull CalendarEntry entry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method UpdateEntry not implemented for " + Tag);
    }

    @Override
    public void DeleteEntry(@NonNull CalendarEntry entry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method DeleteEntry not implemented for " + Tag);
    }

    @Override
    public void SetReloadEnabled(boolean reloadEnabled) {
        _reloadEnabled = reloadEnabled;
        if (_reloadEnabled) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
        }
    }

    @Override
    public boolean GetReloadEnabled() {
        return _reloadEnabled;
    }

    @Override
    public void SetReloadTimeout(int reloadTimeout) {
        if (reloadTimeout < MinTimeoutMin) {
            reloadTimeout = MinTimeoutMin;
        }
        if (reloadTimeout > MaxTimeoutMin) {
            reloadTimeout = MaxTimeoutMin;
        }

        _reloadTimeout = reloadTimeout * 60 * 1000;
        if (_reloadEnabled) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
        }
    }

    @Override
    public int GetReloadTimeout() {
        return _reloadTimeout;
    }

    @Override
    public Calendar GetLastUpdate() {
        return _lastUpdate;
    }

    @Override
    public void ShowNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method ShowNotification not implemented for " + Tag);
    }

    @Override
    public void CloseNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method CloseNotification not implemented for " + Tag);
    }

    @Override
    public void SetDisplayNotification(boolean displayNotification) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetDisplayNotification not implemented for " + Tag);
    }

    @Override
    public boolean GetDisplayNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetDisplayNotification not implemented for " + Tag);
    }

    @Override
    public void SetReceiverActivity(@NonNull Class<?> receiverActivity) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetReceiverActivity not implemented for " + Tag);
    }

    @Override
    public Class<?> GetReceiverActivity() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetReceiverActivity not implemented for " + Tag);
    }
}
