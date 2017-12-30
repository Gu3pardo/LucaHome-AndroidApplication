package guepardoapps.lucahome.common.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.service.DownloadStorageService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadController {
    public enum DownloadType implements Serializable {
        Birthday, BirthdayAdd, BirthdayUpdate, BirthdayDelete,
        CoinConversion, Coin, CoinAdd, CoinUpdate, CoinDelete, CoinTrend,
        MapContent,
        ListedMenu, ListedMenuAdd, ListedMenuUpdate, ListedMenuDelete,
        Menu, MenuUpdate, MenuClear,
        MeterData, MeterDataAdd, MeterDataUpdate, MeterDataDelete,
        MoneyMeterData, MoneyMeterDataAdd, MoneyMeterDataUpdate, MoneyMeterDataDelete,
        Movie, MovieUpdate,
        Schedule, ScheduleSet, ScheduleAdd, ScheduleUpdate, ScheduleDelete,
        Security, SecurityCamera, SecurityCameraControl,
        ShoppingList, ShoppingListAdd, ShoppingListDelete, ShoppingListUpdate,
        Temperature,
        TimerAdd, TimerUpdate, TimerDelete,
        User,
        WirelessSocket, WirelessSocketSet, WirelessSocketAdd, WirelessSocketUpdate, WirelessSocketDelete,
        WirelessSwitch, WirelessSwitchToggle, WirelessSwitchAdd, WirelessSwitchUpdate, WirelessSwitchDelete
    }

    public enum DownloadState implements Serializable {
        Canceled, NoNetwork, NoHomeNetwork, InvalidUrl, Success
    }

    public static class DownloadFinishedBroadcastContent implements Serializable {
        public boolean Success;
        public DownloadType CurrentDownloadType;
        public DownloadState FinalDownloadState;
        public Serializable Additional;

        public DownloadFinishedBroadcastContent(boolean success, @NonNull DownloadType currentDownloadType, @NonNull DownloadState finalDownloadState, Serializable additional) {
            Success = success;
            CurrentDownloadType = currentDownloadType;
            FinalDownloadState = finalDownloadState;
            Additional = additional;
        }
    }

    public static final String DownloadFinishedBroadcast = "guepardoapps.lucahome.data.controller.download.finished";
    public static final String DownloadFinishedBundle = "DownloadFinishedBundle";

    private static final String TAG = DownloadController.class.getSimpleName();

    private BroadcastController _broadcastController;
    private NetworkController _networkController;
    private OkHttpClient _okHttpClient;

    public DownloadController(@NonNull Context context) {
        _broadcastController = new BroadcastController(context);
        _networkController = new NetworkController(context);
        _okHttpClient = new OkHttpClient();
    }

    public void SendCommandToWebsiteAsync(@NonNull String requestUrl, @NonNull DownloadType downloadType, boolean needsHomeNetwork, Serializable additional) {
        Logger.getInstance().Information(TAG, String.format(Locale.getDefault(), "SendCommandToWebsiteAsync: %s", requestUrl));

        if (!canSendAction(downloadType, needsHomeNetwork, requestUrl, additional)) {
            return;
        }

        String result = "";
        boolean downloadSuccess = false;
        try {
            Request request = new Request.Builder().url(requestUrl).build();
            Response response = _okHttpClient.newCall(request).execute();
            ResponseBody responseBody = response.body();

            if (responseBody != null) {
                result = responseBody.toString();
                Logger.getInstance().Debug(TAG, result);
                downloadSuccess = true;
            } else {
                Logger.getInstance().Error(TAG, "ResponseBody is null!");
            }
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.getMessage());
        } finally {
            byte[] byteArray = Tools.CompressStringToByteArray(result);
            DownloadStorageService.getInstance().PutDownloadResult(downloadType, byteArray);
            _broadcastController.SendSerializableBroadcast(
                    DownloadFinishedBroadcast,
                    DownloadFinishedBundle,
                    new DownloadFinishedBroadcastContent(downloadSuccess, downloadType, DownloadState.Success, additional));
        }
    }

    public void SendCommandToWebsiteAsync(@NonNull String requestUrl, @NonNull DownloadType downloadType, boolean needsHomeNetwork) {
        SendCommandToWebsiteAsync(requestUrl, downloadType, needsHomeNetwork, null);
    }

    private boolean canSendAction(@NonNull DownloadType downloadType, boolean needsHomeNetwork, @NonNull String requestUrl, Serializable additional) {
        if (!_networkController.IsNetworkAvailable()) {
            DownloadStorageService.getInstance().PutDownloadResult(downloadType, Tools.CompressStringToByteArray("No network!"));
            _broadcastController.SendSerializableBroadcast(
                    DownloadFinishedBroadcast,
                    DownloadFinishedBundle,
                    new DownloadFinishedBroadcastContent(false, downloadType, DownloadState.NoNetwork, additional));
            return false;
        }

        if (needsHomeNetwork && !_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            DownloadStorageService.getInstance().PutDownloadResult(downloadType, Tools.CompressStringToByteArray("No home network!"));
            _broadcastController.SendSerializableBroadcast(
                    DownloadFinishedBroadcast,
                    DownloadFinishedBundle,
                    new DownloadFinishedBroadcastContent(false, downloadType, DownloadState.NoHomeNetwork, additional));
            return false;
        }

        if (requestUrl.length() < 15) {
            Logger.getInstance().Error(TAG, "Invalid requestUrl length!");
            DownloadStorageService.getInstance().PutDownloadResult(downloadType, Tools.CompressStringToByteArray("ERROR: Invalid requestUrl length!"));
            _broadcastController.SendSerializableBroadcast(
                    DownloadFinishedBroadcast,
                    DownloadFinishedBundle,
                    new DownloadFinishedBroadcastContent(false, downloadType, DownloadState.InvalidUrl, additional));
            return false;
        }

        return true;
    }
}
