package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

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
import guepardoapps.lucahome.common.converter.JsonDataToCoinConversionConverter;
import guepardoapps.lucahome.common.converter.JsonDataToCoinConverter;
import guepardoapps.lucahome.common.database.DatabaseCoinList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class CoinService {
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

    private static final int TIMEOUT_MS = 30 * 60 * 1000;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_reloadListRunnable run");

            LoadCoinConversionList();
            LoadCoinList();

            _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);
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

            _broadcastController.SendSerializableBroadcast(
                    CoinConversionDownloadFinishedBroadcast,
                    CoinConversionDownloadFinishedBundle,
                    new CoinConversionDownloadFinishedContent(_coinConversionList, true, content.Response));

            LoadCoinList();
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
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<Coin> coinList = _jsonDataToCoinConverter.GetList(contentResponse, _coinConversionList);
            if (coinList == null) {
                _logger.Error("Converted CoinList is null!");
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

            LoadCoinList();
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

            LoadCoinList();
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

            LoadCoinList();
        }
    };

    private CoinService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static CoinService getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _databaseCoinList = new DatabaseCoinList(context);
        _databaseCoinList.Open();

        _receiverController.RegisterReceiver(_coinConversionDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _jsonDataToCoinConverter = new JsonDataToCoinConverter();
        _jsonDataToCoinConversionConverter = new JsonDataToCoinConversionConverter();

        _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);

        _isInitialized = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseCoinList.Close();
        _isInitialized = false;
    }

    public SerializableList<Coin> GetCoinList() {
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

    public SerializableList<Coin> FoundCoins(@NonNull String searchKey) {
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

    public void LoadCoinList() {
        _logger.Debug("LoadCoinList");

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _coinList = _databaseCoinList.GetCoinList();
            _filteredCoinList = new SerializableList<>();
            _broadcastController.SendSerializableBroadcast(
                    CoinDownloadFinishedBroadcast,
                    CoinDownloadFinishedBundle,
                    new CoinDownloadFinishedContent(_coinList, true, Tools.CompressStringToByteArray("Loaded from database!")));
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
                new CoinDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
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
}
