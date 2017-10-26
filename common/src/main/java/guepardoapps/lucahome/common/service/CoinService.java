package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializablePair;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.Coin;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.converter.JsonDataToCoinTrendConverter;
import guepardoapps.lucahome.common.converter.JsonDataToCoinConversionConverter;
import guepardoapps.lucahome.common.converter.JsonDataToCoinConverter;
import guepardoapps.lucahome.common.database.DatabaseCoinList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.interfaces.services.IDataService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class CoinService implements IDataService {
    public static class CoinConversionDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<SerializablePair<String, Double>> CoinConversionList;

        public CoinConversionDownloadFinishedContent(SerializableList<SerializablePair<String, Double>> coinConversionList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            CoinConversionList = coinConversionList;
        }
    }

    public static class CoinDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<Coin> CoinList;

        public CoinDownloadFinishedContent(SerializableList<Coin> coinList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            CoinList = coinList;
        }
    }

    public static final String CoinIntent = "CoinIntent";

    public static final String CoinConversionDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.coinconversion.download.finished";
    public static final String CoinConversionDownloadFinishedBundle = "CoinConversionDownloadFinishedBundle";

    public static final String CoinDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.coin.download.finished";
    public static final String CoinDownloadFinishedBundle = "CoinDownloadFinishedBundle";

    public static final String CoinAddFinishedBroadcast = "guepardoapps.lucahome.data.service.coin.add.finished";
    public static final String CoinAddFinishedBundle = "CoinAddFinishedBundle";

    public static final String CoinUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.coin.update.finished";
    public static final String CoinUpdateFinishedBundle = "CoinUpdateFinishedBundle";

    public static final String CoinDeleteFinishedBroadcast = "guepardoapps.lucahome.data.service.coin.delete.finished";
    public static final String CoinDeleteFinishedBundle = "CoinDeleteFinishedBundle";

    private static final CoinService SINGLETON = new CoinService();
    private boolean _isInitialized;

    private static final String TAG = CoinService.class.getSimpleName();
    private Logger _logger;

    private static final int MIN_TIMEOUT_MIN = 30;
    private static final int MAX_TIMEOUT_MIN = 24 * 60;

    private boolean _reloadEnabled;
    private int _reloadTimeout;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_reloadListRunnable run");

            LoadCoinConversionList();
            LoadData();

            if (_reloadEnabled && _networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private DatabaseCoinList _databaseCoinList;

    private JsonDataToCoinConverter _jsonDataToCoinConverter;
    private JsonDataToCoinConversionConverter _jsonDataToCoinConversionConverter;

    private SerializableList<Coin> _coinList = new SerializableList<>();
    private SerializableList<Coin> _filteredCoinList = new SerializableList<>();
    private SerializableList<SerializablePair<String, Double>> _coinConversionList = new SerializableList<>();

    private BroadcastReceiver _coinConversionDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_coinConversionDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.CoinConversion) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            SerializableList<SerializablePair<String, Double>> coinConversionList = _jsonDataToCoinConversionConverter.GetList(contentResponse);
            if (coinConversionList == null) {
                _logger.Error("Converted coinConversionList is null!");

                _broadcastController.SendSerializableBroadcast(
                        CoinConversionDownloadFinishedBroadcast,
                        CoinConversionDownloadFinishedBundle,
                        new CoinConversionDownloadFinishedContent(_coinConversionList, false, content.Response));

                return;
            }

            _coinConversionList = coinConversionList;

            mergeCoinConversionWithCoinList();

            _broadcastController.SendSerializableBroadcast(
                    CoinConversionDownloadFinishedBroadcast,
                    CoinConversionDownloadFinishedBundle,
                    new CoinConversionDownloadFinishedContent(_coinConversionList, true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _coinAggregateDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_coinAggregateEurDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.CoinAggregate) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            Coin coinDto = (Coin) content.Additional;
            try {
                coinDto = JsonDataToCoinTrendConverter.UpdateTrend(coinDto, contentResponse, "EUR");

                for (int index = 0; index < _coinList.getSize(); index++) {
                    if (_coinList.getValue(index).GetId() == coinDto.GetId()) {
                        _coinList.setValue(index, coinDto);
                        _logger.Debug(String.format(Locale.getDefault(), "Updated CoinDto is %s", coinDto));

                        _broadcastController.SendSerializableBroadcast(
                                CoinConversionDownloadFinishedBroadcast,
                                CoinConversionDownloadFinishedBundle,
                                new CoinConversionDownloadFinishedContent(_coinConversionList, false, content.Response));
                        break;
                    }
                }
            } catch (JSONException jsonException) {
                _logger.Error(jsonException.getMessage());
            }
        }
    };

    private BroadcastReceiver _coinDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_coinDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Coin) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                _coinList = _databaseCoinList.GetCoinList();
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                _coinList = _databaseCoinList.GetCoinList();
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<Coin> coinList = _jsonDataToCoinConverter.GetList(contentResponse, _coinConversionList);
            if (coinList == null) {
                _logger.Error("Converted CoinList is null!");
                _coinList = _databaseCoinList.GetCoinList();
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _coinList = coinList;
            _filteredCoinList = new SerializableList<>();

            clearCoinListFromDatabase();
            saveCoinListToDatabase();

            _broadcastController.SendSerializableBroadcast(
                    CoinDownloadFinishedBroadcast,
                    CoinDownloadFinishedBundle,
                    new CoinDownloadFinishedContent(_coinList, true, content.Response));

            for (int index = 0; index < _coinList.getSize(); index++) {
                LoadCoinTrend(_coinList.getValue(index));
            }
        }
    };

    private BroadcastReceiver _coinAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_coinAddFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.CoinAdd) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedAddBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedAddBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    CoinAddFinishedBroadcast,
                    CoinAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _coinUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_coinUpdateFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.CoinUpdate) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedUpdateBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedUpdateBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    CoinUpdateFinishedBroadcast,
                    CoinUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _coinDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_coinDeleteFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.CoinDelete) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedDeleteBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedDeleteBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    CoinDeleteFinishedBroadcast,
                    CoinDeleteFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _homeNetworkAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_homeNetworkAvailableReceiver onReceive");
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            if (_reloadEnabled && _networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private BroadcastReceiver _homeNetworkNotAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_homeNetworkNotAvailableReceiver onReceive");
            _reloadHandler.removeCallbacks(_reloadListRunnable);
        }
    };

    private CoinService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static CoinService getInstance() {
        return SINGLETON;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _reloadEnabled = reloadEnabled;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _databaseCoinList = new DatabaseCoinList(context);
        _databaseCoinList.Open();

        _receiverController.RegisterReceiver(_coinAggregateDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinConversionDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        _jsonDataToCoinConverter = new JsonDataToCoinConverter();
        _jsonDataToCoinConversionConverter = new JsonDataToCoinConversionConverter();

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseCoinList.Close();
        _isInitialized = false;
    }

    @Override
    public SerializableList<Coin> GetDataList() {
        return _coinList;
    }

    public ArrayList<String> GetTypeList() {
        ArrayList<String> typeList = new ArrayList<>();

        for (int index = 0; index < _coinConversionList.getSize(); index++) {
            typeList.add(_coinConversionList.getValue(index).GetKey());
        }

        return typeList;
    }

    public Coin GetById(int id) {
        for (int index = 0; index < _coinList.getSize(); index++) {
            Coin entry = _coinList.getValue(index);

            if (entry.GetId() == id) {
                return entry;
            }
        }

        return null;
    }

    @Override
    public SerializableList<Coin> SearchDataList(@NonNull String searchKey) {
        _filteredCoinList = new SerializableList<>();

        for (int index = 0; index < _coinList.getSize(); index++) {
            Coin entry = _coinList.getValue(index);

            if (String.valueOf(entry.GetId()).contains(searchKey)
                    || entry.GetType().contains(searchKey)
                    || entry.GetUser().contains(searchKey)
                    || String.valueOf(entry.GetAmount()).contains(searchKey)
                    || entry.GetValueString().contains(searchKey)
                    || String.valueOf(entry.GetValue()).contains(searchKey)
                    || String.valueOf(entry.GetCurrentConversion()).contains(searchKey)) {
                _filteredCoinList.addValue(entry);
            }
        }

        return _filteredCoinList;
    }

    public String AllCoinsValue() {
        double value = 0;

        for (int index = 0; index < _coinList.getSize(); index++) {
            Coin entry = _coinList.getValue(index);
            value += entry.GetValue();
        }

        return String.format(Locale.getDefault(), "Sum: %.2f €", value);
    }

    public String FilteredCoinsValue() {
        double value = 0;

        for (int index = 0; index < _filteredCoinList.getSize(); index++) {
            Coin entry = _filteredCoinList.getValue(index);
            value += entry.GetAmount();
        }

        return String.format(Locale.getDefault(), "Sum: %.2f €", value);
    }

    public void LoadCoinConversionList() {
        _logger.Debug("LoadCoinConversionList");

        String requestUrl = "https://min-api.cryptocompare.com/data/pricemulti?fsyms=BCH,BTC,DASH,ETC,ETH,LTC,XMR,ZEC&tsyms=EUR";
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.CoinConversion, false);
    }

    public void LoadCoinTrend(@NonNull Coin coin) {
        _logger.Debug(String.format(Locale.getDefault(), "LoadCoinTrend for %s of the last %d hours.", coin, SettingsController.getInstance().GetCoinHoursTrend()));

        String requestUrlEur = String.format(Locale.getDefault(), "https://min-api.cryptocompare.com/data/histohour?fsym=%s&tsym=%s&limit=%d&aggregate=3&e=CCCAGG", coin.GetType(), "EUR", SettingsController.getInstance().GetCoinHoursTrend());
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrlEur is: %s", requestUrlEur));

        _downloadController.SendCommandToWebsiteAsync(requestUrlEur, DownloadController.DownloadType.CoinAggregate, false, coin);
    }

    @Override
    public void LoadData() {
        _logger.Debug("LoadData");

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _coinList = _databaseCoinList.GetCoinList();
            _filteredCoinList = new SerializableList<>();

            mergeCoinConversionWithCoinList();

            _broadcastController.SendSerializableBroadcast(
                    CoinDownloadFinishedBroadcast,
                    CoinDownloadFinishedBundle,
                    new CoinDownloadFinishedContent(_coinList, true, Tools.CompressStringToByteArray("Loaded from database!")));

            for (int index = 0; index < _coinList.getSize(); index++) {
                LoadCoinTrend(_coinList.getValue(index));
            }

            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast("No user");
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_COINS_USER.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Coin, true);
    }

    public void AddCoin(Coin entry) {
        _logger.Debug(String.format(Locale.getDefault(), "AddCoin: Adding new entry %s", entry));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedAddBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.CoinAdd, true);
    }

    public void UpdateCoin(Coin entry) {
        _logger.Debug(String.format(Locale.getDefault(), "UpdateCoin: Updating entry %s", entry));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedUpdateBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.CoinUpdate, true);
    }

    public void DeleteCoin(Coin entry) {
        _logger.Debug(String.format(Locale.getDefault(), "DeleteCoin: Deleting entry %s", entry));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDeleteBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.CoinDelete, true);
    }

    @Override
    public boolean GetReloadEnabled() {
        return _reloadEnabled;
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
    public int GetReloadTimeout() {
        return _reloadTimeout;
    }

    @Override
    public void SetReloadTimeout(int reloadTimeout) {
        if (reloadTimeout < MIN_TIMEOUT_MIN) {
            _logger.Warning(String.format(Locale.getDefault(), "reloadTimeout %d is lower then MIN_TIMEOUT_MIN %d! Setting to MIN_TIMEOUT_MIN!", reloadTimeout, MIN_TIMEOUT_MIN));
            reloadTimeout = MIN_TIMEOUT_MIN;
        }
        if (reloadTimeout > MAX_TIMEOUT_MIN) {
            _logger.Warning(String.format(Locale.getDefault(), "reloadTimeout %d is higher then MAX_TIMEOUT_MS %d! Setting to MAX_TIMEOUT_MIN!", reloadTimeout, MAX_TIMEOUT_MIN));
            reloadTimeout = MAX_TIMEOUT_MIN;
        }

        _reloadTimeout = reloadTimeout * 60 * 1000;
        if (_reloadEnabled) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
        }
    }

    public void SetCoinHoursTrend(int coinHoursTrend) {
        if (coinHoursTrend < 24) {
            _logger.Warning(String.format(Locale.getDefault(), "SetCoinHoursTrend: coinHoursTrend %d is invalid! Setting to 24!", coinHoursTrend));
            coinHoursTrend = 24;
        }

        _settingsController.SetCoinHoursTrend(coinHoursTrend);
    }

    private void clearCoinListFromDatabase() {
        _logger.Debug("clearCoinListFromDatabase");

        SerializableList<Coin> coinList = _databaseCoinList.GetCoinList();
        for (int index = 0; index < coinList.getSize(); index++) {
            Coin coin = coinList.getValue(index);
            _databaseCoinList.Delete(coin);
        }
    }

    private void saveCoinListToDatabase() {
        _logger.Debug("saveCoinListToDatabase");

        for (int index = 0; index < _coinList.getSize(); index++) {
            Coin coin = _coinList.getValue(index);
            _databaseCoinList.CreateEntry(coin);
        }
    }

    private void sendFailedDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                CoinDownloadFinishedBroadcast,
                CoinDownloadFinishedBundle,
                new CoinDownloadFinishedContent(_coinList, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedAddBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                CoinAddFinishedBroadcast,
                CoinAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedUpdateBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                CoinUpdateFinishedBroadcast,
                CoinUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedDeleteBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                CoinDeleteFinishedBroadcast,
                CoinDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void mergeCoinConversionWithCoinList() {
        if (_coinList == null) {
            _logger.Error("_coinList is null!");
            return;
        }

        if (_coinConversionList == null) {
            _logger.Error("_coinConversionList is null!");
            return;
        }

        for (int index = 0; index < _coinList.getSize(); index++) {
            Coin entry = _coinList.getValue(index);

            for (int conversionIndex = 0; conversionIndex < _coinConversionList.getSize(); conversionIndex++) {
                SerializablePair<String, Double> conversionEntry = _coinConversionList.getValue(conversionIndex);

                if (entry.GetType().contains(conversionEntry.GetKey())) {
                    entry.SetCurrentConversion(conversionEntry.GetValue());
                    break;
                }
            }
        }
    }
}
