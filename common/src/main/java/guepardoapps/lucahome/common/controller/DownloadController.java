package guepardoapps.lucahome.common.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.lucahome.common.services.DownloadStorageService;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@SuppressWarnings({"WeakerAccess"})
public class DownloadController implements IDownloadController {
    private static final String Tag = DownloadController.class.getSimpleName();

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

    private BroadcastController _broadcastController;
    private NetworkController _networkController;
    private OkHttpClient _okHttpClient;

    public DownloadController(@NonNull Context context) {
        _broadcastController = new BroadcastController(context);
        _networkController = new NetworkController(context);
        _okHttpClient = new OkHttpClient();
    }

    @Override
    public void SendCommandToWebsiteAsync(@NonNull String requestUrl, @NonNull DownloadType downloadType, boolean needsHomeNetwork, Serializable additional) {
        if (!canSendAction(downloadType, needsHomeNetwork, requestUrl, additional)) {
            return;
        }

        SendActionTask sendActionTask = new SendActionTask();
        sendActionTask.CurrentDownloadType = downloadType;
        sendActionTask.Additional = additional;
        sendActionTask.execute(requestUrl);
    }

    @Override
    public void SendCommandToWebsiteAsync(@NonNull String requestUrl, @NonNull DownloadType downloadType, boolean needsHomeNetwork) {
        SendCommandToWebsiteAsync(requestUrl, downloadType, needsHomeNetwork, null);
    }

    private boolean canSendAction(@NonNull DownloadType downloadType, boolean needsHomeNetwork, @NonNull String requestUrl, Serializable additional) {
        if (!_networkController.IsNetworkAvailable()) {
            downloadFailedAction(downloadType, "No network!", DownloadState.NoNetwork, additional);
            return false;
        }

        if (needsHomeNetwork && !_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            downloadFailedAction(downloadType, "No home network!", DownloadState.NoHomeNetwork, additional);
            return false;
        }

        if (requestUrl.length() < 15) {
            downloadFailedAction(downloadType, "Invalid requestUrl length!", DownloadState.InvalidUrl, additional);
            return false;
        }

        return true;
    }

    private void downloadFailedAction(@NonNull DownloadType downloadType, @NonNull String message, @NonNull DownloadState downloadState, Serializable additional) {
        Logger.getInstance().Error(Tag, message);
        DownloadStorageService.getInstance().PutDownloadResult(downloadType, Tools.CompressStringToByteArray(message));
        _broadcastController.SendSerializableBroadcast(
                DownloadFinishedBroadcast,
                DownloadFinishedBundle,
                new DownloadFinishedBroadcastContent(false, downloadType, downloadState, additional));
    }

    private class SendActionTask extends AsyncTask<String, Void, String> {
        public DownloadType CurrentDownloadType;
        public Serializable Additional;

        @Override
        protected String doInBackground(String... requestUrls) {
            for (String requestUrl : requestUrls) {
                String result = "";
                boolean downloadSuccess = false;

                try {
                    Request request = new Request.Builder().url(requestUrl).build();
                    Response response = _okHttpClient.newCall(request).execute();
                    ResponseBody responseBody = response.body();

                    if (responseBody != null) {
                        result = responseBody.string();
                        Logger.getInstance().Debug(Tag, result);
                        downloadSuccess = true;
                    } else {
                        Logger.getInstance().Error(Tag, "ResponseBody is null!");
                    }
                } catch (Exception exception) {
                    Logger.getInstance().Error(Tag, exception.toString());
                } finally {
                    byte[] byteArray = Tools.CompressStringToByteArray(result);
                    DownloadStorageService.getInstance().PutDownloadResult(CurrentDownloadType, byteArray);
                    _broadcastController.SendSerializableBroadcast(
                            DownloadFinishedBroadcast,
                            DownloadFinishedBundle,
                            new DownloadFinishedBroadcastContent(downloadSuccess, CurrentDownloadType, DownloadState.Success, Additional));
                }
            }

            return "";
        }
    }
}
