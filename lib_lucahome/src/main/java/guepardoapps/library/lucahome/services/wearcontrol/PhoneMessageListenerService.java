package guepardoapps.library.lucahome.services.wearcontrol;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import android.os.Bundle;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.enums.MainServiceAction;
import guepardoapps.library.lucahome.common.enums.ServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.MediaMirrorController;
import guepardoapps.library.lucahome.controller.ScheduleController;
import guepardoapps.library.lucahome.controller.SocketController;

import guepardoapps.library.toolset.controller.BroadcastController;

public class PhoneMessageListenerService extends WearableListenerService
        implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = PhoneMessageListenerService.class.getSimpleName();
    private LucaHomeLogger _logger;

    private BroadcastController _broadcastController;
    private MediaMirrorController _mediaMirrorController;
    private ScheduleController _scheduleController;
    private SocketController _socketController;

    private static final String PHONE_MESSAGE_PATH = "/phone_message";
    private GoogleApiClient _apiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        _broadcastController = new BroadcastController(this);
        _mediaMirrorController = new MediaMirrorController(this);
        _mediaMirrorController.Initialize();
        _scheduleController = new ScheduleController(this);
        _socketController = new SocketController(this);

        initGoogleApiClient();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");

        if (_apiClient != null) {
            _apiClient.unregisterConnectionCallbacks(this);
            Wearable.MessageApi.removeListener(_apiClient, this);
            if (_apiClient.isConnected()) {
                _apiClient.disconnect();
            }
        }

        _mediaMirrorController.Dispose();

        super.onDestroy();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        _logger.Debug("onMessageReceived");
        if (messageEvent.getPath().equalsIgnoreCase(PHONE_MESSAGE_PATH)) {
            String message = new String(messageEvent.getData());
            _logger.Debug("message: " + message);

            String[] data = message.split("\\:");
            if (data[0] == null) {
                _logger.Warn("data[0] is null!");
                return;
            }

            if (data[0].contains("ACTION")) {
                if (data[1] == null) {
                    _logger.Warn("data[1] is null!");
                    return;
                }

                if (data[1].contains("GET")) {
                    if (data[2] == null) {
                        _logger.Warn("data[2] is null!");
                        return;
                    }

                    if (data[2].contains("BIRTHDAYS")) {
                        _broadcastController.SendSerializableBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
                                Bundles.MAIN_SERVICE_ACTION, MainServiceAction.GET_BIRTHDAYS);
                    } else if (data[2].contains("MENU")) {
                        _broadcastController.SendSerializableBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
                                Bundles.MAIN_SERVICE_ACTION, MainServiceAction.GET_MENU);
                    } else if (data[2].contains("MOVIES")) {
                        _broadcastController.SendSerializableBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
                                Bundles.MAIN_SERVICE_ACTION, MainServiceAction.GET_MOVIES);
                    } else if (data[2].contains("MEDIAMIRROR")) {
                        _broadcastController.SendSerializableBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
                                Bundles.MAIN_SERVICE_ACTION, MainServiceAction.GET_MEDIAMIRROR);
                    } else if (data[2].contains("SCHEDULES")) {
                        _broadcastController.SendSerializableBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
                                Bundles.MAIN_SERVICE_ACTION, MainServiceAction.GET_SCHEDULES);
                    } else if (data[2].contains("SOCKETS")) {
                        _broadcastController.SendSerializableBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
                                Bundles.MAIN_SERVICE_ACTION, MainServiceAction.GET_SOCKETS);
                    } else if (data[2].contains("SHOPPING_LIST")) {
                        _broadcastController.SendSerializableBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
                                Bundles.MAIN_SERVICE_ACTION, MainServiceAction.GET_SHOPPING_LIST);
                    } else if (data[2].contains("TIMER")) {
                        _broadcastController.SendSerializableBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
                                Bundles.MAIN_SERVICE_ACTION, MainServiceAction.GET_TIMER);
                    } else {
                        _logger.Warn("data[2] not supported: " + data[2]);
                    }
                } else if (data[1].contains("SET")) {
                    if (data[2] == null) {
                        _logger.Warn("data[2] is null!");
                        return;
                    }

                    if (data[2].contains("SCHEDULE")) {
                        if (data[3] != null && data[4] != null) {
                            _scheduleController.SetSchedule(data[3], data[4].contains("1"));
                        } else {
                            _logger.Warn("data[3] or data[4] is null!");
                        }
                    } else if (data[2].contains("SOCKET")) {
                        if (data[3] != null && data[4] != null) {
                            _socketController.SetSocket(data[3], data[4].contains("1"));
                        } else {
                            _logger.Warn("data[3] or data[4] is null!");
                        }
                    } else if (data[2].contains("SHOPPING")) {
                        if (data[3] != null && data[4] != null && data[5] != null) {
                            if (data[3].contains("BOUGHT")) {
                                _broadcastController.SendStringBroadcast(Broadcasts.UPDATE_BOUGHT_SHOPPING_LIST,
                                        Bundles.SHOPPING_LIST, data[4] + ":" + data[5]);
                            } else {
                                _logger.Error(String.format("Unknown command %s for shopping!", data[3]));
                            }
                        } else {
                            _logger.Warn("data[3] or data[4] or data[5] is null!");
                        }
                    } else {
                        _logger.Warn("data[2] not supported: " + data[2]);
                    }
                } else if (data[1].contains("MEDIAMIRROR")) {
                    if (data.length == 5) {
                        String serverIp = data[2];
                        String action = data[3];
                        String command = data[4];

                        if (action.contains("YOUTUBE")) {
                            if (command.contains("PLAY")) {
                                _mediaMirrorController.SendCommand(serverIp,
                                        ServerAction.PLAY_YOUTUBE_VIDEO.toString(), "");
                                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_MEDIAMIRROR);
                            } else if (command.contains("PAUSE")) {
                                _mediaMirrorController.SendCommand(serverIp,
                                        ServerAction.PAUSE_YOUTUBE_VIDEO.toString(), "");
                                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_MEDIAMIRROR);
                            } else if (command.contains("STOP")) {
                                _mediaMirrorController.SendCommand(serverIp,
                                        ServerAction.STOP_YOUTUBE_VIDEO.toString(), "");
                                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_MEDIAMIRROR);
                            } else {
                                _logger.Warn("command not supported for mediamirror youtube: " + command);
                            }
                        } else if (action.contains("VOLUME")) {
                            if (command.contains("INCREASE")) {
                                _mediaMirrorController.SendCommand(serverIp,
                                        ServerAction.INCREASE_VOLUME.toString(), "");
                                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_MEDIAMIRROR);
                            } else if (command.contains("DECREASE")) {
                                _mediaMirrorController.SendCommand(serverIp,
                                        ServerAction.DECREASE_VOLUME.toString(), "");
                                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_MEDIAMIRROR);
                            } else {
                                _logger.Warn("command not supported for mediamirror volume: " + command);
                            }
                        } else {
                            _logger.Warn("action not supported for mediamirror: " + action);
                        }
                    } else {
                        _logger.Error("Invalid length of data for mediaMirror: " + String.valueOf(data.length));
                    }
                } else {
                    _logger.Warn("data[1] not supported: " + data[1]);
                }
            } else {
                _logger.Warn("data[0] not supported: " + data[0]);
            }
        } else {
            _logger.Warn("Path is not " + PHONE_MESSAGE_PATH);
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        _logger.Debug("onConnected");
        Wearable.MessageApi.addListener(_apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        _logger.Debug("onConnectionSuspended");
    }

    private void initGoogleApiClient() {
        _logger.Debug("initGoogleApiClient");
        _apiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(this).build();

        if (!(_apiClient.isConnected() || _apiClient.isConnecting())) {
            _logger.Debug("_apiClient.connect");
            _apiClient.connect();
        }
    }
}
