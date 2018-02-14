package guepardoapps.lucahome.common.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.Meal;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToMealConverter;
import guepardoapps.lucahome.common.databases.DatabaseMealList;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"WeakerAccess"})
public class MealService implements IMealService {
    private static final String Tag = MealService.class.getSimpleName();

    private static final MealService Singleton = new MealService();

    private static final int MinTimeoutMin = 15;
    private static final int MaxTimeoutMin = 24 * 60;

    private Context _context;

    private DatabaseMealList _databaseMealList;

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;

    private Calendar _lastUpdate;

    private boolean _isInitialized;

    private boolean _loadDataEnabled;

    private boolean _reloadEnabled;
    private int _reloadTimeout;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            LoadData();
            if (_reloadEnabled && _networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private class AsyncConverterTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            for (String contentResponse : strings) {
                ArrayList<Meal> mealList = JsonDataToMealConverter.getInstance().GetList(contentResponse);
                if (mealList == null) {
                    Logger.getInstance().Error(Tag, "Converted mealList is null!");
                    _broadcastController.SendBooleanBroadcast(MealDownloadFinishedBroadcast, MealDownloadFinishedBundle, false);
                    return "";
                }

                mealList = validateMealList(mealList);
                if (checkDatabaseForEntriesNotOnServer()) {
                    LoadData();
                } else {
                    _databaseMealList.ClearDatabase();
                    saveMealListToDatabase(mealList);

                    _lastUpdate = Calendar.getInstance();
                    _broadcastController.SendBooleanBroadcast(MealDownloadFinishedBroadcast, MealDownloadFinishedBundle, true);
                }
            }
            return "Success";
        }
    }

    private BroadcastReceiver _mealDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Meal) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(MealDownloadFinishedBroadcast, MealDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(MealDownloadFinishedBroadcast, MealDownloadFinishedBundle, false);
                return;
            }

            new AsyncConverterTask().execute(contentResponse);
        }
    };

    private BroadcastReceiver _mealUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MealUpdate) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(MealUpdateFinishedBroadcast, MealUpdateFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(MealUpdateFinishedBroadcast, MealUpdateFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(MealUpdateFinishedBroadcast, MealUpdateFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _mealClearFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MealClear) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(MealClearFinishedBroadcast, MealClearFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(MealClearFinishedBroadcast, MealClearFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(MealClearFinishedBroadcast, MealClearFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _homeNetworkAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            if (_reloadEnabled && _networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private BroadcastReceiver _homeNetworkNotAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
        }
    };

    private MealService() {
    }

    public static MealService getInstance() {
        return Singleton;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout, boolean displayNotification, Class<?> receiverActivity) {
        if (_isInitialized) {
            Logger.getInstance().Warning(Tag, "Already initialized!");
            return;
        }

        _loadDataEnabled = true;

        _lastUpdate = Calendar.getInstance();

        _reloadEnabled = reloadEnabled;

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _downloadController = new DownloadController(_context);
        _networkController = new NetworkController(_context);
        _receiverController = new ReceiverController(_context);

        _databaseMealList = new DatabaseMealList(_context);
        _databaseMealList.Open();

        _receiverController.RegisterReceiver(_mealDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_mealUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_mealClearFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseMealList.Close();
        _isInitialized = false;
    }

    @Override
    public ArrayList<Meal> GetDataList() {
        try {
            return _databaseMealList.GetList(null, null, DatabaseMealList.KeyDate + " ASC");
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Meal GetByUuid(@NonNull UUID uuid) {
        try {
            return _databaseMealList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseMealList.KeyUuid, uuid), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new Meal(uuid, "NULL", "NULL", Calendar.getInstance(), new ArrayList<>(), false, ILucaClass.LucaServerDbAction.Null);
        }
    }

    @Override
    public ArrayList<Meal> SearchDataList(@NonNull String searchKey) {
        ArrayList<Meal> mealList = GetDataList();
        ArrayList<Meal> foundMeals = new ArrayList<>();
        for (int index = 0; index < mealList.size(); index++) {
            Meal entry = mealList.get(index);
            if (entry.toString().contains(searchKey)) {
                foundMeals.add(entry);
            }
        }
        return foundMeals;
    }

    @Override
    public ArrayList<String> GetTitleList() {
        try {
            return _databaseMealList.GetStringQueryList(true, DatabaseMealList.KeyTitle, null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<String> GetDescriptionList() {
        try {
            return _databaseMealList.GetStringQueryList(true, DatabaseMealList.KeyDescription, null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void LoadData() {
        if (!_loadDataEnabled) {
            return;
        }

        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            _broadcastController.SendBooleanBroadcast(MealDownloadFinishedBroadcast, MealDownloadFinishedBundle, true);
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(MealDownloadFinishedBroadcast, MealDownloadFinishedBundle, false);
            return;
        }

        checkDatabaseForEntriesNotOnServer();

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_MEALS.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Meal, true);
    }

    @Override
    public void AddEntry(@NonNull Meal entry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method AddEntry not implemented for " + Tag);
    }

    @Override
    public void UpdateEntry(@NonNull Meal entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);
            _databaseMealList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(MealUpdateFinishedBroadcast, MealUpdateFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MealUpdate, true);
    }

    @Override
    public void DeleteEntry(@NonNull Meal entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);
            _databaseMealList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(MealClearFinishedBroadcast, MealClearFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MealClear, true);
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

    @Override
    public void ShareMenuList() {
        ArrayList<Meal> mealList = GetDataList();

        StringBuilder shareText = new StringBuilder("Meal:\n");
        for (int index = 0; index < mealList.size(); index++) {
            Meal entry = mealList.get(index);
            Calendar date = entry.GetDate();
            String dateString = String.format(Locale.getDefault(), "%s, %2d.%2d", date.get(Calendar.DAY_OF_WEEK), date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH));
            shareText.append(dateString).append("\n").append(entry.GetTitle()).append("\n").append(entry.GetDescription()).append("\n\n");
        }

        Intent sendIntent = new Intent();

        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        sendIntent.setType("text/plain");

        _context.startActivity(sendIntent);
    }

    private ArrayList<Meal> validateMealList(@NonNull ArrayList<Meal> mealList) {
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);

        for (int index = 0; index < mealList.size(); index++) {
            Meal meal = mealList.get(index);

            if (meal.GetDate().get(Calendar.YEAR) < year
                    || meal.GetDate().get(Calendar.MONTH) < month
                    || (meal.GetDate().get(Calendar.DAY_OF_MONTH) < dayOfMonth && meal.GetDate().get(Calendar.MONTH) <= month)) {
                Logger.getInstance().Debug(Tag, String.format("Updating meal %s ...", meal.toString()));
                meal = resetMeal(meal);
                if (meal == null) {
                    continue;
                }
                _databaseMealList.UpdateEntry(meal);
                mealList.set(index, meal);
            }
        }

        return mealList;
    }

    private Meal resetMeal(@NonNull Meal meal) {
        meal.SetTitle("-");
        meal.SetDescription("-");

        Calendar today = Calendar.getInstance();

        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH) + 1;
        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);

        int mealDayOfWeek = meal.GetDate().get(Calendar.DAY_OF_WEEK);

        if (mealDayOfWeek < 0) {
            Logger.getInstance().Error(Tag, "Day of week was not found!");
            return null;
        }

        int dayOfWeekDifference = mealDayOfWeek - dayOfWeek + 1;
        if (dayOfWeekDifference < 0) {
            dayOfWeekDifference += 7;
        }

        if (meal.GetDate().get(Calendar.YEAR) < year || meal.GetDate().get(Calendar.MONTH) < month || meal.GetDate().get(Calendar.DAY_OF_MONTH) < dayOfMonth) {
            return calculateDate(meal, year, month, dayOfMonth, dayOfWeekDifference);
        }

        return null;
    }

    private Meal calculateDate(
            @NonNull Meal meal,
            int year,
            int month,
            int dayOfMonth,
            int dayOfWeekDifference) {
        dayOfMonth += dayOfWeekDifference;

        switch (month) {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
                if (dayOfMonth > 31) {
                    dayOfMonth -= 31;
                    month++;
                }
                break;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                if (dayOfMonth > 30) {
                    dayOfMonth -= 30;
                    month++;
                }
                break;
            case Calendar.FEBRUARY:
                if (year % 4 == 0) {
                    if (dayOfMonth > 29) {
                        dayOfMonth -= 29;
                        month++;
                    }
                } else {
                    if (dayOfMonth > 28) {
                        dayOfMonth -= 28;
                        month++;
                    }
                }
                break;
            case Calendar.DECEMBER:
                if (dayOfMonth > 31) {
                    dayOfMonth -= 31;
                    month = Calendar.JANUARY;
                    year++;
                }
                break;
            default:
                Logger.getInstance().Error(Tag, String.format(Locale.getDefault(), "Invalid month %d!", month));
                return null;
        }

        Calendar newDate = Calendar.getInstance();
        newDate.set(year, month, dayOfMonth);

        meal.SetDate(newDate);

        return meal;
    }

    private void saveMealListToDatabase(@NonNull ArrayList<Meal> mealList) {
        for (int index = 0; index < mealList.size(); index++) {
            Meal meal = mealList.get(index);
            _databaseMealList.AddEntry(meal);
        }
    }

    private boolean checkDatabaseForEntriesNotOnServer() {
        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            ArrayList<Meal> notOnServerMealList = notOnServerEntries();
            for (int index = 0; index < notOnServerMealList.size(); index++) {
                Meal meal = notOnServerMealList.get(index);

                switch (meal.GetServerDbAction()) {
                    case Update:
                        UpdateEntry(meal);
                        break;
                    case Delete:
                        DeleteEntry(meal);
                        break;
                    case Add:
                    case Null:
                    default:
                        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Nothing todo with %s.", meal));
                        break;
                }
            }

            _loadDataEnabled = true;
            return true;
        }
        return false;
    }

    private ArrayList<Meal> notOnServerEntries() {
        try {
            return _databaseMealList.GetList(String.format(Locale.getDefault(), "%s like %d", DatabaseMealList.KeyIsOnServer, 0), null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerEntries().size() > 0;
    }
}
