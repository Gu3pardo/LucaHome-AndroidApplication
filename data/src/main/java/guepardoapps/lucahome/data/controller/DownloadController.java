package guepardoapps.lucahome.data.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.data.service.broadcasts.content.ObjectChangeFinishedContent;

public class DownloadController {
    public enum DownloadType implements Serializable {
        Birthday, BirthdayAdd, BirthdayUpdate, BirthdayDelete,
        CoinConversion, Coin, CoinAdd, CoinUpdate, CoinDelete,
        MapContent,
        ListedMenu,
        Menu, MenuUpdate, MenuClear,
        Movie, MovieUpdate,
        Schedule, ScheduleSet, ScheduleAdd, ScheduleUpdate, ScheduleDelete,
        ShoppingList, ShoppingListAdd, ShoppingListDelete, ShoppingListUpdate,
        Temperature,
        TimerAdd, TimerUpdate, TimerDelete,
        User,
        WirelessSocket, WirelessSocketSet, WirelessSocketAdd, WirelessSocketUpdate, WirelessSocketDelete
    }

    public enum DownloadState implements Serializable {
        Canceled, NoNetwork, NoHomeNetwork, InvalidUrl, Success
    }

    public static class DownloadFinishedBroadcastContent extends ObjectChangeFinishedContent {
        public DownloadType CurrentDownloadType;
        public DownloadState FinalDownloadState;

        public DownloadFinishedBroadcastContent(@NonNull byte[] response, boolean success, @NonNull DownloadType currentDownloadType, @NonNull DownloadState finalDownloadState) {
            super(success, response);
            CurrentDownloadType = currentDownloadType;
            FinalDownloadState = finalDownloadState;
        }
    }

    public static final String DownloadFinishedBroadcast = "guepardoapps.lucahome.data.controller.download.finished";
    public static final String DownloadFinishedBundle = "DownloadFinishedBundle";

    private static final String TAG = DownloadController.class.getSimpleName();
    private Logger _logger;

    private static final int TIMEOUT_MS = 3000;

    private BroadcastController _broadcastController;
    private NetworkController _networkController;
    private SettingsController _settingsController;

    public DownloadController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _broadcastController = new BroadcastController(context);
        _networkController = new NetworkController(context);
        _settingsController = SettingsController.getInstance();
    }

    public void SendCommandToWebsiteAsync(@NonNull String requestUrl, @NonNull DownloadType downloadType, boolean needsHomeNetwork) {
        _logger.Debug("SendCommandToWebsiteAsync");

        if (!_networkController.IsNetworkAvailable()) {
            _broadcastController.SendSerializableBroadcast(
                    DownloadFinishedBroadcast,
                    DownloadFinishedBundle,
                    new DownloadFinishedBroadcastContent(Tools.CompressStringToByteArray("No network!"), false, downloadType, DownloadState.NoNetwork));
            return;
        }

        if (needsHomeNetwork && !_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _broadcastController.SendSerializableBroadcast(
                    DownloadFinishedBroadcast,
                    DownloadFinishedBundle,
                    new DownloadFinishedBroadcastContent(Tools.CompressStringToByteArray("No home network!"), false, downloadType, DownloadState.NoHomeNetwork));
            return;
        }

        if (requestUrl.length() < 15) {
            _logger.Error("Invalid requestUrl length!");
            _broadcastController.SendSerializableBroadcast(
                    DownloadFinishedBroadcast,
                    DownloadFinishedBundle,
                    new DownloadFinishedBroadcastContent(Tools.CompressStringToByteArray("ERROR: Invalid requestUrl length!"), false, downloadType, DownloadState.InvalidUrl));
            return;
        }

        SendActionTask task = new SendActionTask();
        task.DownloadSuccess = false;
        task.CurrentDownloadType = downloadType;
        task.execute(requestUrl);
    }

    private class SendActionTask extends AsyncTask<String, Void, String> {
        boolean DownloadSuccess;
        public DownloadType CurrentDownloadType;

        @Override
        protected String doInBackground(String... actions) {
            String result = "";
            for (String action : actions) {
                try {
                    _logger.Information("action: " + action);

                    URL url = new URL(action);
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(TIMEOUT_MS);
                    InputStream inputStream = connection.getInputStream();

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader reader = new BufferedReader(inputStreamReader);

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result += line;
                    }

                    reader.close();
                    inputStreamReader.close();
                    inputStream.close();

                    DownloadSuccess = true;
                } catch (IOException exception) {
                    DownloadSuccess = false;
                    _logger.Error(exception.getMessage());
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            _logger.Debug(String.format(Locale.getDefault(), "onPostExecute: Length of result is %d and result itself is %s", result.length(), result));

            byte[] byteArray = Tools.CompressStringToByteArray(result);
            _logger.Debug(String.format(Locale.getDefault(), "onPostExecute: Length of byteArray is %d and byteArray itself is %s", byteArray.length, byteArray));

            _broadcastController.SendSerializableBroadcast(
                    DownloadFinishedBroadcast,
                    DownloadFinishedBundle,
                    new DownloadFinishedBroadcastContent(byteArray, DownloadSuccess, CurrentDownloadType, DownloadState.Success));
        }

        @Override
        protected void onCancelled() {
            _broadcastController.SendSerializableBroadcast(
                    DownloadFinishedBroadcast,
                    DownloadFinishedBundle,
                    new DownloadFinishedBroadcastContent(Tools.CompressStringToByteArray("Canceled!"), false, CurrentDownloadType, DownloadState.Canceled));
        }
    }
}
