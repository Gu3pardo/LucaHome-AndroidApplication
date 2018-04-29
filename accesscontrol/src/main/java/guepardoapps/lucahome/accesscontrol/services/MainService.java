package guepardoapps.lucahome.accesscontrol.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.IBinder;
import android.app.Service;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.util.Locale;

import guepardoapps.lucahome.common.classes.MediaServer;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.IDownloadController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.enums.AccessControlAlarmState;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.enums.MediaServerActionType;
import guepardoapps.lucahome.common.server.ServerThread;
import guepardoapps.lucahome.common.server.handler.AccessControlDataHandler;
import guepardoapps.lucahome.common.server.handler.IAccessControlDataHandler;
import guepardoapps.lucahome.common.server.handler.IDataHandler;
import guepardoapps.lucahome.common.services.DownloadStorageService;
import guepardoapps.lucahome.common.services.MediaServerClientService;
import guepardoapps.lucahome.common.utils.ByteStringHelper;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

import guepardoapps.lucahome.accesscontrol.common.Constants;

public class MainService extends Service {
    private static final String Tag = MainService.class.getSimpleName();

    private static final String CodeInvalid = "CodeInvalid";
    private static final String CodeValid = "CodeValid";
    private static final String AccessControlActive = "AccessControlActive";
    private static final String RequestCode = "RequestCode";
    private static final String AccessSuccessful = "AccessSuccessful";
    private static final String AccessFailed = "AccessFailed";
    private static final String AlarmActive = "AlarmActive";

    private MediaServerClientService _mediaServerClientService;

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private ReceiverController _receiverController;

    private IDataHandler _accessControlDataHandler;
    private ServerThread _serverThread;

    private boolean _isInitialized;

    private BroadcastReceiver _codeValidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String requestUrl = "http://"
                    + SettingsController.getInstance().GetServerIp()
                    + guepardoapps.lucahome.common.constants.Constants.ActionPath
                    + ByteStringHelper.ReadStringFromCharArray(Constants.UserName)
                    + "&password="
                    + ByteStringHelper.ReadStringFromCharArray(Constants.UserPassPhrase)
                    + "&action=" + LucaServerActionTypes.STOP_ACCESS_ALARM.toString();
            _downloadController.SendCommandToWebsiteAsync(requestUrl, IDownloadController.DownloadType.AccessAlarm, true);

            for (MediaServer mediaServer : _mediaServerClientService.GetDataList()) {
                _mediaServerClientService.SetActiveMediaServer(mediaServer);
                _mediaServerClientService.SendCommand(MediaServerActionType.STOP_ACCESS_ALARM.toString(), "");
            }
        }
    };

    private BroadcastReceiver _downloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            if (content.CurrentDownloadType != DownloadController.DownloadType.AccessAlarm) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));
            try {
                String responseAnswer = convertStringResponseToAnswer(contentResponse);
                handleResponseAnswer(responseAnswer);
            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.toString());
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (!_isInitialized) {
            SettingsController.getInstance().Initialize(this);

            _mediaServerClientService = MediaServerClientService.getInstance();
            _mediaServerClientService.Initialize(this, true, SettingsController.getInstance().GetReloadMediaServerTimeout(), false, null);

            _broadcastController = new BroadcastController(this);
            _downloadController = new DownloadController(this);
            _receiverController = new ReceiverController(this);
            _receiverController.RegisterReceiver(_codeValidReceiver, new String[]{IAccessControlDataHandler.BroadcastEnteredCodeValid});
            _receiverController.RegisterReceiver(_downloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

            _accessControlDataHandler = new AccessControlDataHandler();
            _accessControlDataHandler.Initialize(this);
            _serverThread = new ServerThread();
            _serverThread.Start(Constants.ServerPort, _accessControlDataHandler);

            _isInitialized = true;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _mediaServerClientService.Dispose();
        _receiverController.Dispose();
        _accessControlDataHandler.Dispose();
        _serverThread.Dispose();
        _isInitialized = false;
    }

    private String convertStringResponseToAnswer(@NonNull String contentResponse) throws Exception {
        JSONObject jsonObject = new JSONObject(contentResponse);
        JSONObject jsonObjectData = jsonObject.getJSONObject("Data");
        return jsonObjectData.getString("Response");
    }

    private void handleResponseAnswer(String responseAnswer) {
        if (responseAnswer == null) {
            Logger.getInstance().Error(Tag, "ResponseAnswer is null!");
            return;
        }

        switch (responseAnswer) {
            case CodeValid:
                _broadcastController.SendSimpleBroadcast(IAccessControlDataHandler.BroadcastEnteredCodeValid);
                break;
            case CodeInvalid:
                _broadcastController.SendSimpleBroadcast(IAccessControlDataHandler.BroadcastEnteredCodeInvalid);
                break;
            case AccessControlActive:
                _broadcastController.SendSerializableBroadcast(IAccessControlDataHandler.BundleAlarmState, IAccessControlDataHandler.BundleAlarmState, AccessControlAlarmState.AccessControlActive);
                break;
            case RequestCode:
                _broadcastController.SendSerializableBroadcast(IAccessControlDataHandler.BundleAlarmState, IAccessControlDataHandler.BundleAlarmState, AccessControlAlarmState.RequestCode);
                break;
            case AccessSuccessful:
                _broadcastController.SendSerializableBroadcast(IAccessControlDataHandler.BundleAlarmState, IAccessControlDataHandler.BundleAlarmState, AccessControlAlarmState.AccessSuccessful);
                break;
            case AccessFailed:
                _broadcastController.SendSerializableBroadcast(IAccessControlDataHandler.BundleAlarmState, IAccessControlDataHandler.BundleAlarmState, AccessControlAlarmState.AccessFailed);
                break;
            case AlarmActive:
                _broadcastController.SendSerializableBroadcast(IAccessControlDataHandler.BundleAlarmState, IAccessControlDataHandler.BundleAlarmState, AccessControlAlarmState.AlarmActive);
                break;
            default:
                Logger.getInstance().Error(Tag, String.format(Locale.getDefault(), "No support for responseAnswer %s", responseAnswer));
                break;
        }
    }
}