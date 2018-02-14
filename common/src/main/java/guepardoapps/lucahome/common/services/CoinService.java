package guepardoapps.lucahome.common.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.Coin;
import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToCoinConverter;
import guepardoapps.lucahome.common.converter.JsonDataToCoinDataConverter;
import guepardoapps.lucahome.common.databases.DatabaseCoinList;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"WeakerAccess"})
public class CoinService implements ICoinService {
    private static final String Tag = CoinService.class.getSimpleName();

    private static final CoinService Singleton = new CoinService();

    private static final int MinTimeoutMin = 15;
    private static final int MaxTimeoutMin = 24 * 60;

    private Context _context;

    private DatabaseCoinList _databaseCoinList;

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private NotificationController _notificationController;
    private ReceiverController _receiverController;

    private Calendar _lastUpdate;

    private Class<?> _receiverActivity;
    private boolean _displayNotification;

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

    private class AsyncDownloadConverterTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            for (String contentResponse : strings) {
                ArrayList<Coin> coinList = JsonDataToCoinConverter.getInstance().GetList(contentResponse);
                if (coinList == null) {
                    Logger.getInstance().Error(Tag, "Converted CoinList is null!");
                    _broadcastController.SendSerializableBroadcast(CoinDownloadFinishedBroadcast, CoinDownloadFinishedBundle, false);
                    return "";
                }

                _databaseCoinList.ClearDatabase();
                saveCoinListToDatabase(coinList);

                ShowNotification();

                _lastUpdate = Calendar.getInstance();
                _broadcastController.SendSerializableBroadcast(CoinDownloadFinishedBroadcast, CoinDownloadFinishedBundle, true);

                for (int index = 0; index < coinList.size(); index++) {
                    LoadCoinData(coinList.get(index));
                }
            }
            return "Success";
        }
    }

    private class AsyncDataConverterTask extends AsyncTask<String, String, String> {
        Coin coin;

        @Override
        protected String doInBackground(String... strings) {
            for (String contentResponse : strings) {
                try {
                    coin = JsonDataToCoinDataConverter.UpdateData(coin, contentResponse, "EUR");
                    _databaseCoinList.UpdateEntry(coin);
                    _broadcastController.SendBooleanBroadcast(CoinDataDownloadFinishedBroadcast, CoinDataDownloadFinishedBundle, true);
                    _lastUpdate = Calendar.getInstance();
                    ShowNotification();
                } catch (JSONException jsonException) {
                    Logger.getInstance().Error(Tag, jsonException.getMessage());
                    _broadcastController.SendBooleanBroadcast(CoinDataDownloadFinishedBroadcast, CoinDataDownloadFinishedBundle, false);
                }
            }
            return "Success";
        }
    }

    private BroadcastReceiver _coinDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Coin) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(CoinDownloadFinishedBroadcast, CoinDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(CoinDownloadFinishedBroadcast, CoinDownloadFinishedBundle, false);
                return;
            }

            new AsyncDownloadConverterTask().execute(contentResponse);
        }
    };

    private BroadcastReceiver _coinAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.CoinAdd) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(CoinAddFinishedBroadcast, CoinAddFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(CoinAddFinishedBroadcast, CoinAddFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(CoinAddFinishedBroadcast, CoinAddFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _coinUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.CoinUpdate) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(CoinUpdateFinishedBroadcast, CoinUpdateFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(CoinUpdateFinishedBroadcast, CoinUpdateFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(CoinUpdateFinishedBroadcast, CoinUpdateFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _coinDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.CoinDelete) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(CoinDeleteFinishedBroadcast, CoinDeleteFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(CoinDeleteFinishedBroadcast, CoinDeleteFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(CoinDeleteFinishedBroadcast, CoinDeleteFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _coinDataDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.CoinData) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            AsyncDataConverterTask asyncTrendConverterTask = new AsyncDataConverterTask();
            asyncTrendConverterTask.coin = (Coin) content.Additional;
            asyncTrendConverterTask.execute(contentResponse);
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

    private CoinService() {
    }

    public static CoinService getInstance() {
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

        _receiverActivity = receiverActivity;
        _displayNotification = displayNotification;
        _reloadEnabled = reloadEnabled;

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _downloadController = new DownloadController(_context);
        _networkController = new NetworkController(_context);
        _notificationController = new NotificationController(_context);
        _receiverController = new ReceiverController(_context);

        _databaseCoinList = new DatabaseCoinList(_context);
        _databaseCoinList.Open();

        _receiverController.RegisterReceiver(_coinDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinDataDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseCoinList.Close();
        _isInitialized = false;
    }

    @Override
    public ArrayList<Coin> GetDataList() {
        try {
            return _databaseCoinList.GetList(null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Coin GetByUuid(@NonNull UUID uuid) {
        try {
            return _databaseCoinList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseCoinList.KeyUuid, uuid), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new Coin(uuid, "NULL", "NULL", 0, 0, Coin.Trend.Null, false, ILucaClass.LucaServerDbAction.Null);
        }
    }

    @Override
    public ArrayList<Coin> SearchDataList(@NonNull String searchKey) {
        ArrayList<Coin> coinList = GetDataList();
        ArrayList<Coin> foundCoins = new ArrayList<>();
        for (int index = 0; index < coinList.size(); index++) {
            Coin entry = coinList.get(index);
            if (entry.toString().contains(searchKey)) {
                foundCoins.add(entry);
            }
        }
        return foundCoins;
    }

    @Override
    public ArrayList<String> GetTypeList() {
        try {
            return _databaseCoinList.GetStringQueryList(true, DatabaseCoinList.KeyType, null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public double AllCoinsValue() {
        ArrayList<Coin> coinList = GetDataList();
        double value = 0;
        for (int index = 0; index < coinList.size(); index++) {
            value += coinList.get(index).GetValue();
        }
        return value;
    }

    @Override
    public double FilteredCoinsValue(@NonNull String searchKey) {
        ArrayList<Coin> filteredCoinList = SearchDataList(searchKey);
        double value = 0;
        for (int index = 0; index < filteredCoinList.size(); index++) {
            value += filteredCoinList.get(index).GetValue();
        }
        return value;
    }

    @Override
    public void LoadData() {
        if (!_loadDataEnabled) {
            return;
        }

        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            _broadcastController.SendBooleanBroadcast(CoinDownloadFinishedBroadcast, CoinDownloadFinishedBundle, true);
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(CoinDownloadFinishedBroadcast, CoinDownloadFinishedBundle, false);
            return;
        }

        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            ArrayList<Coin> notOnServerCoinList = notOnServerEntries();
            for (int index = 0; index < notOnServerCoinList.size(); index++) {
                Coin coin = notOnServerCoinList.get(index);

                switch (coin.GetServerDbAction()) {
                    case Add:
                        AddEntry(coin);
                        break;
                    case Update:
                        UpdateEntry(coin);
                        break;
                    case Delete:
                        DeleteEntry(coin);
                        break;
                    case Null:
                    default:
                        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Nothing todo with %s.", coin));
                        break;
                }

            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_COINS_USER.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Coin, true);
    }

    @Override
    public void AddEntry(@NonNull Coin entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);
            _databaseCoinList.AddEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(CoinAddFinishedBroadcast, CoinAddFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.CoinAdd, true);
    }

    @Override
    public void UpdateEntry(@NonNull Coin entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);
            _databaseCoinList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(CoinUpdateFinishedBroadcast, CoinUpdateFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.CoinUpdate, true);
    }

    @Override
    public void DeleteEntry(@NonNull Coin entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);
            _databaseCoinList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(CoinDeleteFinishedBroadcast, CoinDeleteFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.CoinDelete, true);
    }

    @Override
    public void LoadCoinData(@NonNull Coin coin) {
        String requestUrlEur = String.format(Locale.getDefault(), "https://min-api.cryptocompare.com/data/histohour?fsym=%s&tsym=%s&limit=%d&aggregate=3&e=CCCAGG", coin.GetType(), "EUR", SettingsController.getInstance().GetCoinHoursTrend());
        _downloadController.SendCommandToWebsiteAsync(requestUrlEur, DownloadController.DownloadType.CoinData, false, coin);
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
    public void ShowNotification() {
        if (!_displayNotification) {
            Logger.getInstance().Warning(Tag, "_displayNotification is false!");
            return;
        }

        if (_receiverActivity == null) {
            Logger.getInstance().Error(Tag, "ReceiverActivity is null!");
            return;
        }

        Bitmap icon = BitmapFactory.decodeResource(_context.getResources(), R.drawable.coin_btc);
        _notificationController.CreateSimpleNotification(
                NotificationId, R.drawable.coin_btc, _receiverActivity, icon,
                "Coin value", String.format(Locale.getDefault(), "You have a value of: %s", AllCoinsValue()),
                true);
    }

    @Override
    public void CloseNotification() {
        _notificationController.CloseNotification(NotificationId);
    }

    @Override
    public void SetDisplayNotification(boolean displayNotification) {
        _displayNotification = displayNotification;

        if (!_displayNotification) {
            CloseNotification();
        } else {
            ShowNotification();
        }
    }

    @Override
    public boolean GetDisplayNotification() {
        return _displayNotification;
    }

    @Override
    public void SetReceiverActivity(@NonNull Class<?> receiverActivity) {
        _receiverActivity = receiverActivity;
    }

    @Override
    public Class<?> GetReceiverActivity() {
        return _receiverActivity;
    }

    @Override
    public void SetCoinHoursTrend(int coinHoursTrend) {
        if (coinHoursTrend < 0) {
            Logger.getInstance().Warning(Tag, String.format(Locale.getDefault(), "SetCoinHoursTrend: coinHoursTrend %d is invalid! Setting to 24!", coinHoursTrend));
            coinHoursTrend = 24;
        }

        SettingsController.getInstance().SetCoinHoursTrend(coinHoursTrend);
    }

    @Override
    public int GetCoinHoursTrend() {
        return SettingsController.getInstance().GetCoinHoursTrend();
    }

    private void saveCoinListToDatabase(@NonNull ArrayList<Coin> coinList) {
        for (int index = 0; index < coinList.size(); index++) {
            Coin coin = coinList.get(index);
            _databaseCoinList.AddEntry(coin);
        }
    }

    private ArrayList<Coin> notOnServerEntries() {
        try {
            return _databaseCoinList.GetList(String.format(Locale.getDefault(), "%s like %d", DatabaseCoinList.KeyIsOnServer, 0), null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerEntries().size() > 0;
    }
}
